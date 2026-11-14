#!/usr/bin/env bash
# =============================================================================
# db_password.sh —— Vexus 交易所数据库密码加解密工具（openssl 3DES 实现）
#
# 与 Java 端 kernel.util.Endecrypt / kernel.druid.LocalDruidDataSource 完全一致：
#   密钥   : MD5( KEY 的 GBK 字节 ) 16 字节 + 8 个 0x00 -> 24 字节
#   明文   : UTF-16LE 字节
#   加密   : DESede / ECB / PKCS5Padding（PKCS7，块 8 字节）
#   编码   : Base64 -> URL 百分号编码
#   解密   : URL 解码 -> Base64 解码 -> DESede 解密 -> UTF-16LE 解码
#
# 固定 KEY: Roj6#@08SDF87323FG00%jjsd
#
# 用法:
#   ./db_password.sh decrypt 'nY0NTjBcrC0nk6q9zp%2FJRCVu0GmCAI2kBNPDWppUOY3XuBTmq4d4gA%3D%3D'
#   ./db_password.sh encrypt 'MyStr0ng#Pass'
#   ./db_password.sh selftest      # 已知密文/已知明文双向自测
#   ./db_password.sh encrypt 'pwd' --key 'OtherKey'
#
# 依赖: openssl(含 des-ede3)、md5sum/md5、base64、iconv、xxd(自测用，可选)
# =============================================================================
set -euo pipefail

DEFAULT_KEY='Roj6#@08SDF87323FG00%jjsd'
KNOWN_CIPHER='nY0NTjBcrC0nk6q9zp%2FJRCVu0GmCAI2kBNPDWppUOY3XuBTmq4d4gA%3D%3D'
KNOWN_PLAIN='Err;2[eoGFUriwdgr'

die() { printf 'ERROR: %s\n' "$*" >&2; exit 1; }

need() { command -v "$1" >/dev/null 2>&1 || die "缺少依赖命令: $1"; }

# ---- MD5(KEY) -> 16 字节 hex，再补 16 个 0 凑 24 字节（48 个 hex 字符） --------
key_hex() {
  local key="$1" md5hex
  if command -v md5sum >/dev/null 2>&1; then
    md5hex=$(printf '%s' "$key" | md5sum | awk '{print $1}')
  elif command -v md5 >/dev/null 2>&1; then
    md5hex=$(printf '%s' "$key" | md5 -q)
  else
    die "需要 md5sum 或 md5 命令"
  fi
  printf '%s' "${md5hex}0000000000000000"
}

# ---- URL 编码（等价 Java URLEncoder.encode(s,"UTF-8")） ----------------------
# 本工具的输入始终是 Base64，可能出现需要转义的只有 + / = 三个字符。
url_encode() {
  printf '%s' "$1" | sed -e 's/%/%25/g' -e 's/+/%2B/g' -e 's|/|%2F|g' -e 's/=/ %3D/g' | tr -d ' '
}

# ---- URL 解码（%XX -> 原始字节，+ -> 空格） ---------------------------------
# 思路：先把 % 换成 \x，再交给 printf '%b' 解释转义（输入是 Base64，不含反斜杠）
url_decode_bytes() {
  local s="${1//+/ }"
  s="${s//%/\\x}"
  printf '%b' "$s"
}

# ---- base64 编码（无换行；GNU 用 -w0，BSD/macOS 退回 tr -d '\n'） -----------
b64_encode() {
  if base64 -w0 </dev/null >/dev/null 2>&1; then
    base64 -w0
  else
    base64 | tr -d '\n'
  fi
}

encrypt() {
  local plain="$1" key="$2" khex b64
  khex=$(key_hex "$key")
  b64=$(printf '%s' "$plain" \
    | iconv -f UTF-8 -t UTF-16LE \
    | openssl enc -des-ede3 -K "$khex" -nosalt 2>/dev/null \
    | base64 -w0 2>/dev/null || true)
  [ -n "$b64" ] || die "加密失败（检查 openssl 是否支持 des-ede3）"
  url_encode "$b64"
}

decrypt() {
  local cipher="$1" key="$2" khex plain
  khex=$(key_hex "$key")
  plain=$(url_decode_bytes "$cipher" \
    | base64 -d 2>/dev/null \
    | openssl enc -des-ede3 -d -K "$khex" -nosalt 2>/dev/null \
    | iconv -f UTF-16LE -t UTF-8 2>/dev/null || true)
  [ -n "$plain" ] || die "解密失败：密文非法、KEY 不对，或 openssl 不支持 des-ede3"
  printf '%s' "$plain"
}

selftest() {
  local rc=0
  need openssl; need iconv; need base64

  local pt
  pt=$(decrypt "$KNOWN_CIPHER" "$DEFAULT_KEY" || true)
  if [ "$pt" = "$KNOWN_PLAIN" ]; then
    printf '[1] known cipher decrypt : %s  OK\n' "$pt"
  else
    printf '[1] known cipher decrypt : %s  FAIL (expect %s)\n' "$pt" "$KNOWN_PLAIN"
    rc=1
  fi

  local ct
  ct=$(encrypt "$KNOWN_PLAIN" "$DEFAULT_KEY" || true)
  if [ "$ct" = "$KNOWN_CIPHER" ]; then
    printf '[2] known plain encrypt  : %s  OK\n' "$ct"
  else
    printf '[2] known plain encrypt  : %s  FAIL (expect %s)\n' "$ct" "$KNOWN_CIPHER"
    rc=1
  fi

  local sample='Test@123456' roundtrip
  ct=$(encrypt "$sample" "$DEFAULT_KEY")
  roundtrip=$(decrypt "$ct" "$DEFAULT_KEY")
  if [ "$roundtrip" = "$sample" ]; then
    printf '[3] round trip           : %s -> %s -> %s  OK\n' "$sample" "$ct" "$roundtrip"
  else
    printf '[3] round trip           : FAIL (%s != %s)\n' "$roundtrip" "$sample"
    rc=1
  fi

  if [ "$rc" -eq 0 ]; then
    printf 'selftest: PASS\n'
  else
    printf 'selftest: FAIL\n'
  fi
  return "$rc"
}

usage() {
  sed -n '3,26p' "$0"
  exit 2
}

main() {
  [ $# -ge 1 ] || usage
  local action="$1"; shift
  local key="$DEFAULT_KEY"
  local args=()
  while [ $# -gt 0 ]; do
    case "$1" in
      --key) key="${2:-}"; shift 2 ;;
      -h|--help|help) usage ;;
      *) args+=("$1"); shift ;;
    esac
  done

  case "$action" in
    selftest) need openssl; need iconv; need base64; selftest ;;
    encrypt)
      [ ${#args[@]} -ge 1 ] || die "encrypt 需要明文参数"
      need openssl; need iconv; need base64
      encrypt "${args[0]}" "$key"; printf '\n' ;;
    decrypt)
      [ ${#args[@]} -ge 1 ] || die "decrypt 需要密文参数"
      need openssl; need iconv; need base64
      decrypt "${args[0]}" "$key"; printf '\n' ;;
    *) usage ;;
  esac
}

main "$@"
