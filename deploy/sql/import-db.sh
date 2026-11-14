#!/usr/bin/env bash
# =============================================================================
# import-db.sh —— Vexus 交易所 MySQL 数据库导入 / 校验 / 备份 / 回滚脚本
#
# 默认行为：把「数据库/dkewl.sql」导入到 jiaoyipan 库，并做导入前后校验。
#
# 用法:
#   ./import-db.sh                        # 导入 + 校验（库已存在会提示，--force 覆盖）
#   ./import-db.sh --force                # 库已存在时先 DROP 再重建（危险）
#   ./import-db.sh --dry-run              # 只打印将要执行的命令
#   ./import-db.sh --verify-only          # 只做校验（不导入）
#   ./import-db.sh --backup  [/path/x.sql.gz]   # 备份当前 jiaoyipan
#   ./import-db.sh --restore /path/x.sql.gz     # 从备份回滚（会覆盖现有库）
#
# 可用环境变量覆盖（也可写在同目录的 import-db.env 里）:
#   DUMP_FILE      dkewl.sql 路径（默认自动在工程里找 数据库/dkewl.sql）
#   DB_HOST        默认 127.0.0.1
#   DB_PORT        默认 3306
#   DB_NAME        默认 jiaoyipan
#   DB_USER        默认 root
#   DB_PASS        默认空（建议用 MYSQL_PWD 或 ~/.my.cnf，避免写进命令历史）
#   MYSQL_BIN      默认 mysql
#   MYSQLDUMP_BIN  默认 mysqldump
#   CHARSET        默认 utf8（与 dump 内 SET NAMES utf8 一致）
#   EXPECTED_TABLES 默认 129
#   BACKUP_DIR     默认 ./backup
#
# 说明:
#   * 导入使用 `mysql --default-character-set=utf8`，不要用 utf8mb4，否则中文注释/数据可能乱码。
#   * 密码通过 MYSQL_PWD 传给客户端，不出现在 ps 进程列表里。
#   * 大文件导入建议同时调整服务端 max_allowed_packet（见 README）。
# =============================================================================
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
# shellcheck disable=SC1091
[ -f "$SCRIPT_DIR/import-db.env" ] && . "$SCRIPT_DIR/import-db.env"

# 工程根目录（deploy/sql -> 上两级）
PROJECT_ROOT=$(cd "$SCRIPT_DIR/../.." && pwd)

DUMP_FILE=${DUMP_FILE:-}
DB_HOST=${DB_HOST:-127.0.0.1}
DB_PORT=${DB_PORT:-3306}
DB_NAME=${DB_NAME:-jiaoyipan}
DB_USER=${DB_USER:-root}
DB_PASS=${DB_PASS:-}
MYSQL_BIN=${MYSQL_BIN:-mysql}
MYSQLDUMP_BIN=${MYSQLDUMP_BIN:-mysqldump}
CHARSET=${CHARSET:-utf8}
EXPECTED_TABLES=${EXPECTED_TABLES:-129}
BACKUP_DIR=${BACKUP_DIR:-$SCRIPT_DIR/backup}

MODE=import
FORCE=0
DO_VERIFY=1
DRY_RUN=0
BACKUP_TARGET=""

log()  { printf '[%s] %s\n' "$(date '+%H:%M:%S')" "$*"; }
warn() { printf '[%s] WARN: %s\n' "$(date '+%H:%M:%S')" "$*" >&2; }
die()  { printf '[%s] ERROR: %s\n' "$(date '+%H:%M:%S')" "$*" >&2; exit 1; }

usage() {
  sed -n '5,30p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'
  exit 0
}

# ---------------------------- 参数解析 --------------------------------------
while [ $# -gt 0 ]; do
  case "$1" in
    --force)       FORCE=1; shift ;;
    --dry-run)     DRY_RUN=1; shift ;;
    --verify-only) MODE=verify; shift ;;
    --no-verify)   DO_VERIFY=0; shift ;;
    --import)      MODE=import; shift ;;
    -h|--help)     usage ;;
    --backup)      MODE=backup
                   if [ $# -ge 2 ] && [ "${2#-}" = "$2" ]; then BACKUP_TARGET="$2"; shift; fi
                   shift ;;
    --restore)     MODE=restore
                   [ $# -ge 2 ] || die "--restore 需要备份文件路径"
                   BACKUP_TARGET="$2"; shift 2 ;;
    *) die "未知参数: $1（用 --help 查看用法）" ;;
  esac
done

# ---------------------------- 定位 dump -------------------------------------
find_dump() {
  if [ -n "$DUMP_FILE" ]; then printf '%s' "$DUMP_FILE"; return; fi
  local candidates=(
    "$PROJECT_ROOT/数据库/dkewl.sql"
    "$PROJECT_ROOT/Vexus虚拟货币交易所源码 Java多语言交易所现货合约闪兑/数据库/dkewl.sql"
    "$PROJECT_ROOT/../Vexus虚拟货币交易所源码 Java多语言交易所现货合约闪兑/数据库/dkewl.sql"
  )
  local c
  for c in "${candidates[@]}"; do
    [ -f "$c" ] && { printf '%s' "$c"; return; }
  done
  # 兜底：找工程内任意 dkewl.sql
  c=$(find "$PROJECT_ROOT" -maxdepth 4 -name 'dkewl.sql' -type f 2>/dev/null | head -n 1 || true)
  [ -n "$c" ] && { printf '%s' "$c"; return; }
  printf ''
}

# ---------------------------- MySQL 客户端封装 -------------------------------
mysql_exec() {
  # $1 = 额外参数（可为空）；stdin 传 SQL
  if [ -n "$DB_PASS" ]; then
    MYSQL_PWD="$DB_PASS" "$MYSQL_BIN" -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" --default-character-set=utf8 "$@"
  else
    "$MYSQL_BIN" -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" --default-character-set=utf8 "$@"
  fi
}

query_val() {
  # 执行 SQL 并只取第一行第一列（去掉表头）
  mysql_exec -N -B -e "$1" 2>/dev/null | head -n 1
}

run_or_echo() {
  if [ "$DRY_RUN" = "1" ]; then
    printf '  [dry-run] %s\n' "$*"
  else
    "$@"
  fi
}

# ---------------------------- 预检 ------------------------------------------
preflight_client() {
  if ! command -v "$MYSQL_BIN" >/dev/null 2>&1; then
    if [ "$DRY_RUN" = "1" ]; then
      warn "dry-run：本机未安装 mysql 客户端（$MYSQL_BIN），跳过连接性检查"
      return 0
    fi
    die "找不到 mysql 客户端（$MYSQL_BIN）。请先安装 MySQL 客户端，或设置 MYSQL_BIN=/path/mysql"
  fi
  local out
  out=$(query_val "SELECT VERSION();") || true
  [ -n "$out" ] || die "无法连接 MySQL（$DB_USER@$DB_HOST:$DB_PORT）。请检查服务、账号密码、bind-address"
  log "MySQL 版本: $out"
  local pkt
  pkt=$(query_val "SELECT @@max_allowed_packet;") || pkt=0
  log "max_allowed_packet = $pkt 字节（$(awk -v v="$pkt" 'BEGIN{printf "%.0f", v/1024/1024}') MB）"
  if [ "${pkt:-0}" -lt 67108864 ] 2>/dev/null; then
    warn "max_allowed_packet < 64MB，131MB 的 dump 可能导入失败。"
    warn "建议在 my.cnf 的 [mysqld] 增加 max_allowed_packet=256M 后重启，或临时执行："
    warn "  SET GLOBAL max_allowed_packet=268435456;"
  fi
}

preflight_dump() {
  local dump
  dump=$(find_dump)
  [ -n "$dump" ] || die "找不到 dkewl.sql。请用 DUMP_FILE=/path/dkewl.sql 指定"
  [ -f "$dump" ] || die "dump 文件不存在: $dump"
  local size
  size=$(wc -c < "$dump" | tr -d ' ')
  [ "$size" -gt 1000000 ] || warn "dump 只有 $size 字节，可能不是完整文件（正常约 131,302,296 字节）"
  log "dump 文件: $dump（$size 字节 / $(awk -v v="$size" 'BEGIN{printf "%.1f", v/1024/1024}') MiB）"
  DUMP_FILE="$dump"
}

preflight_disk() {
  local dir
  dir=$(mktemp -d 2>/dev/null || printf '%s' "$SCRIPT_DIR")
  rmdir "$dir" 2>/dev/null || true
  log "注意：导入后数据目录约需 400MB~800MB（含索引与 binlog），请确认磁盘充足"
}

# ---------------------------- 导入 ------------------------------------------
do_import() {
  preflight_client
  preflight_dump
  preflight_disk

  if [ "$DRY_RUN" = "1" ]; then
    log "[dry-run] 目标库: $DB_USER@$DB_HOST:$DB_PORT/$DB_NAME，字符集 $CHARSET"
    printf '  [dry-run] %s\n' "mysql -h$DB_HOST -P$DB_PORT -u$DB_USER --default-character-set=utf8 -e 'CREATE DATABASE ...'"
    printf '  [dry-run] %s\n' "{ SET SESSION ...; USE \`$DB_NAME\`; cat $DUMP_FILE; } | mysql --max-allowed-packet=268435456"
    return 0
  fi

  local exists
  exists=$(query_val "SELECT COUNT(*) FROM information_schema.SCHEMATA WHERE SCHEMA_NAME='$DB_NAME';")
  if [ "${exists:-0}" != "0" ]; then
    if [ "$FORCE" = "1" ]; then
      warn "数据库 $DB_NAME 已存在，--force 生效：将 DROP 后重建"
      run_or_echo mysql_exec -e "DROP DATABASE IF EXISTS \`$DB_NAME\`;"
    else
      die "数据库 $DB_NAME 已存在。确认覆盖请加 --force（会先 DROP，数据不可恢复！）"
    fi
  fi

  log "创建数据库 $DB_NAME（CHARACTER SET utf8 COLLATE utf8_general_ci）"
  run_or_echo mysql_exec -e "CREATE DATABASE IF NOT EXISTS \`$DB_NAME\` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;"

  log "开始导入（131MB dump，通常 3~15 分钟，取决于磁盘 IO；请勿中断）"
  local start end
  start=$(date +%s)
  if [ "$DRY_RUN" = "1" ]; then
    printf '  [dry-run] %s\n' "mysql --default-character-set=utf8 ... $DB_NAME < $DUMP_FILE"
  else
    # 关闭自动提交、放大网络/包参数，仅对当前会话生效（dump 内部有 SET NAMES utf8）
    set +e
    {
      printf 'SET SESSION wait_timeout=600;\n'
      printf 'SET SESSION net_read_timeout=600;\n'
      printf 'SET SESSION net_write_timeout=600;\n'
      printf 'SET SESSION foreign_key_checks=0;\n'
      printf 'SET SESSION unique_checks=0;\n'
      printf 'SET SESSION sql_mode="NO_AUTO_VALUE_ON_ZERO";\n'
      printf 'USE `%s`;\n' "$DB_NAME"
      cat "$DUMP_FILE"
    } | mysql_exec --max-allowed-packet=268435456
    local rc=$?
    set -e
    end=$(date +%s)
    if [ "$rc" -ne 0 ]; then
      warn "导入失败（mysql 退出码 $rc，耗时 $((end-start)) 秒）。排查建议："
      warn "  1) max_allowed_packet 是否 >= 64M；2) 磁盘是否写满；3) 是否重复建表/字符集错误；"
      warn "  4) 是否导入到错误库；5) 看 mysql 错误日志。失败后先 DROP DATABASE 再重试。"
      exit "$rc"
    fi
  fi
  end=${end:-$(date +%s)}
  log "导入完成，耗时 $((end-start)) 秒"

  if [ "$DO_VERIFY" = "1" ]; then
    do_verify
  fi
}

# ---------------------------- 校验 ------------------------------------------
do_verify() {
  if [ "$DRY_RUN" = "1" ]; then
    log "[dry-run] 跳过校验（需要可用的 MySQL 连接）"
    return 0
  fi
  log "================ 导入校验 ================"
  local tables
  tables=$(query_val "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA='$DB_NAME';")
  log "表数量: $tables（期望 $EXPECTED_TABLES）"
  [ "${tables:-0}" -eq "$EXPECTED_TABLES" ] 2>/dev/null || warn "表数量与期望不一致，请检查导入是否完整"

  log "---- 关键表行数（dump 基线，实际运行后会增长）----"
  local check_list=(
    "t_realtime:647082" "t_kline:271375" "t_contract_order:6310" "t_user:783"
    "t_syspara:207" "t_cms:164" "t_item:55" "t_banner:41"
    "t_exchange_apply_order:20" "t_item_leverage:17" "t_wallet_log:15"
    "sct_user:6" "sct_role:13" "sct_user_role:14" "pat_party:12" "t_wallet:12"
    "t_futures_order:5" "t_withdraw_order:6" "t_news:9"
  )
  local item name expect actual flag
  for item in "${check_list[@]}"; do
    name=${item%%:*}; expect=${item##*:}
    actual=$(query_val "SELECT COUNT(*) FROM \`$DB_NAME\`.\`$name\`;" 2>/dev/null || echo "ERR")
    flag="OK"; [ "$actual" != "$expect" ] && flag="DIFF(期望 $expect)"
    printf '  %-28s %-10s %s\n' "$name" "$actual" "$flag"
  done

  log "---- 账号抽查 ----"
  mysql_exec -t -e "SELECT UUID,USERNAME,ENABLED,GOOGLE_AUTH_BIND FROM \`$DB_NAME\`.sct_user ORDER BY USERNAME;" 2>/dev/null || warn "sct_user 查询失败"
  log "---- 管理员 root 记录 ----"
  local root_row
  root_row=$(query_val "SELECT CONCAT(USERNAME,'|',ENABLED,'|',GOOGLE_AUTH_BIND) FROM \`$DB_NAME\`.sct_user WHERE USERNAME='root';")
  if [ -n "$root_row" ]; then log "root 存在: $root_row"; else warn "未找到 root 管理员账号！"; fi

  log "---- t_syspara 关键开关 ----"
  mysql_exec -t -e "SELECT CODE,SVALUE FROM \`$DB_NAME\`.t_syspara WHERE CODE IN ('open_google_auth_code','project_type','customer_service_url','exchange_order_open','c2c_open','user_uid_sequence');" 2>/dev/null || warn "t_syspara 查询失败"

  log "---- 字符集抽查（应为 utf8/utf8_general_ci）----"
  mysql_exec -t -e "SELECT TABLE_COLLATION,COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA='$DB_NAME' GROUP BY TABLE_COLLATION;" 2>/dev/null || true

  log "---- 中文是否乱码抽查（pat_party 无中文则看 t_cms）----"
  mysql_exec -t -e "SELECT UUID,LEFT(TITLE,40) FROM \`$DB_NAME\`.t_cms LIMIT 3;" 2>/dev/null || true

  log "校验结束"
}

# ---------------------------- 备份 / 回滚 -----------------------------------
backup_dir_ensure() {
  [ -d "$(dirname "$1")" ] || mkdir -p "$(dirname "$1")"
}

do_backup() {
  if ! command -v "$MYSQLDUMP_BIN" >/dev/null 2>&1; then
    [ "$DRY_RUN" = "1" ] && warn "dry-run：本机未安装 mysqldump（$MYSQLDUMP_BIN），仅打印命令" || \
      die "找不到 mysqldump（$MYSQLDUMP_BIN）"
  fi
  local target=${BACKUP_TARGET:-}
  if [ -z "$target" ]; then
    mkdir -p "$BACKUP_DIR"
    target="$BACKUP_DIR/jiaoyipan-$(date +%Y%m%d-%H%M%S).sql.gz"
  fi
  backup_dir_ensure "$target"
  log "备份 $DB_NAME -> $target"
  if [ "$DRY_RUN" = "1" ]; then
    printf '  [dry-run] %s\n' "$MYSQLDUMP_BIN --single-transaction ... $DB_NAME | gzip > $target"
    return
  fi
  local start end
  start=$(date +%s)
  if [ -n "$DB_PASS" ]; then
    MYSQL_PWD="$DB_PASS" "$MYSQLDUMP_BIN" -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" \
      --single-transaction --quick --routines --triggers --events \
      --default-character-set=utf8 --databases "$DB_NAME" | gzip -6 > "$target"
  else
    "$MYSQLDUMP_BIN" -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" \
      --single-transaction --quick --routines --triggers --events \
      --default-character-set=utf8 --databases "$DB_NAME" | gzip -6 > "$target"
  fi
  end=$(date +%s)
  [ -s "$target" ] || die "备份文件为空: $target"
  log "备份完成: $target（$(du -h "$target" | awk '{print $1}')，耗时 $((end-start)) 秒）"
}

do_restore() {
  [ -n "$BACKUP_TARGET" ] || die "--restore 需要备份文件路径"
  [ -f "$BACKUP_TARGET" ] || die "备份文件不存在: $BACKUP_TARGET"
  preflight_client
  if [ "$FORCE" != "1" ]; then
    die "回滚会覆盖 $DB_NAME 现有数据，确认请加 --force"
  fi
  log "回滚: $BACKUP_TARGET -> $DB_NAME"
  if [ "$DRY_RUN" = "1" ]; then
    printf '  [dry-run] gunzip -c %s | mysql ... %s\n' "$BACKUP_TARGET" "$DB_NAME"
    return
  fi
  run_or_echo mysql_exec -e "DROP DATABASE IF EXISTS \`$DB_NAME\`; CREATE DATABASE \`$DB_NAME\` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;"
  gunzip -c "$BACKUP_TARGET" | mysql_exec --max-allowed-packet=268435456
  log "回滚完成"
  do_verify
}

# ---------------------------- main ------------------------------------------
case "$MODE" in
  import) do_import ;;
  verify) preflight_client; do_verify ;;
  backup) do_backup ;;
  restore) do_restore ;;
  *) die "未知模式 $MODE" ;;
esac
