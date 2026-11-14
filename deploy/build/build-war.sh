#!/usr/bin/env bash
# =============================================================================
# Vexus Java 交易所（「源代码」旧版）三应用 WAR 构建脚本 —— Linux 版
# -----------------------------------------------------------------------------
# 用途：在 Linux 服务器/构建机上组装 admin / api / data 三个 exploded webapp 并打 WAR。
#       不需要 JDK / Maven / Tomcat，只需要 bash、cp、find、zip。
#       Windows 环境请使用同目录的 build-war.ps1（逻辑与产物一致）。
#
# 输入（只读，不修改原始文件）：
#   <项目根>/源代码/<app>/target/classes        编译产物 -> WAR 内 WEB-INF/classes
#   <项目根>/源代码/<app>/target/dependency     依赖 jar -> WAR 内 WEB-INF/lib（排除容器 jar）
#   <项目根>/源代码/<app>/src/main/webapp       JSP/静态资源 -> WAR 根目录（web.xml 单独取）
#   <项目根>/web3_project/admin_u/normal        仅 admin：旧版 JSP 主题静态资源
#
# 输出：
#   <deploy>/war/admin.war  api.war  data.war
#   exploded 临时目录默认 ${TMPDIR:-/tmp}/vexus-war-build（保留，可 -k 参数决定）
#
# 用法：
#   bash build-war.sh                          # 自动探测项目根，构建 3 个 WAR
#   bash build-war.sh /path/to/project/root    # 显式指定项目根
#   PROJECT_ROOT=/path/root OUT_DIR=/data/war bash build-war.sh
#   APP_LIST="admin data" bash build-war.sh    # 只构建部分应用
#   SKIP_THEME=1 bash build-war.sh             # admin 不补 normal 主题
#   KEEP_WORK=1 bash build-war.sh              # 保留 exploded 目录
#
# 说明（构建前已核实，与 build-war.ps1 保持一致）：
#   1. 旧版持久层是 Spring JDBC（JdbcTemplate + kernel.bo.RecordObjectMapper），实体类只用 JPA
#      注解（@Table/@Column）作列元数据，没有 Hibernate SessionFactory，因此【不需要、也不复制】
#      *.hbm.xml；web3_project 里的 hbm 属于新版 Dubbo 包，混入只会带来版本风险。
#   2. target/dependency 是全量拷贝（含 provided 的容器 jar）。WAR 内 WEB-INF/lib 必须排除：
#      tomcat-*.jar、javax.servlet-api-*.jar、javax.websocket-api-*.jar，
#      否则旧版 kernel.web.RmiProxyFactoryBean 反射访问 Tomcat 内部类时会拿到 webapp 类加载器
#      加载的副本而失败。其余 jar 全部保留。
#   3. WAR 内路径层级：根为 JSP / CSS / JS / IMG / WEB-INF，不能再多一层应用名目录。
#   4. 垃圾文件排除：.DS_Store、Thumbs.db、desktop.ini、~$*、*.log、*.tmp、*.bak 等。
#   5. 构建 admin 时会给 include/basePath.jsp 等 6 个 JSP 打「Nginx 反代兼容补丁」：原版
#      basePath = scheme://serverName:serverPort/admin/，在 Nginx HTTPS 反代后会出现
#      http://域名:8080/... 的错误地址；补丁改为优先读取 X-Forwarded-Proto / X-Forwarded-Host /
#      X-Forwarded-Port，回退还原本地值。补丁幂等，不修改原始源码目录；Nginx 必须传 X-Forwarded-* 头。
#   6. 部署形态：admin 用 context /admin；api 控制器里写死了 /api/xxx.action 前缀，因此 api.war
#      必须部署在非空 context /web（Tomcat 端 URL 是 /web/api/xxx.action），由 Nginx 把外部
#      /api/ 反代到 /web/api/；data 用 context /data。三者必须在同一个 Tomcat。
#   7. 【RMI 端口修复，随构建强制执行】原始 kernel/web/RmiProxyFactoryBean.class 按 Tomcat
#      findChildren() 的 HashMap 顺序分配 20001+ RMI 端口，3 个应用同容器时第 3 个会端口冲突
#      （ExportException: ObjID already in use），且 context 表只快照一次。
#      构建时用 deploy/build/patched/kernel/web/RmiProxyFactoryBean.class 覆盖 3 个应用的
#      WEB-INF/classes/kernel/web/；该文件不存在则直接报错退出，绝不静默生成会挂的包。
#      补丁源码: deploy/build/patches/kernel/web/RmiProxyFactoryBean.java
#      若用 Maven 从原始源码重建，必须手动覆盖该补丁类，否则第 3 个应用启动必失败。
#
# 依赖：bash >= 4、zip（Debian/Ubuntu: apt install zip；CentOS: yum install zip）。
# 幂等：重复执行会重建临时目录并覆盖 WAR；原始源码目录全程只读。
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEPLOY_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
WORKSPACE="$(cd "${DEPLOY_DIR}/.." && pwd)"

# ---------------------------------------------------------------------------
# 路径与参数
# ---------------------------------------------------------------------------
resolve_project_root() {
  if [[ -n "${1:-}" ]]; then
    echo "$(cd "$1" && pwd)"
    return 0
  fi
  if [[ -n "${PROJECT_ROOT:-}" ]]; then
    echo "$(cd "${PROJECT_ROOT}" && pwd)"
    return 0
  fi
  local found=()
  local d
  for d in "${WORKSPACE}"/*/; do
    [[ -d "${d}" ]] || continue
    if [[ -d "${d}源代码/admin/target/classes" ]]; then
      found+=("${d%/}")
    fi
  done
  if [[ ${#found[@]} -eq 1 ]]; then
    echo "${found[0]}"
    return 0
  fi
  if [[ ${#found[@]} -eq 0 ]]; then
    echo "错误：无法自动定位项目根，请用第一个参数或 PROJECT_ROOT 环境变量指定包含『源代码』的项目根。" >&2
  else
    echo "错误：工作区下发现多个候选项目根，请显式指定：" >&2
    printf '  %s\n' "${found[@]}" >&2
  fi
  return 1
}

PROJECT_ROOT="$(resolve_project_root "${1:-}")"
SOURCE_ROOT="${PROJECT_ROOT}/源代码"
THEME_SOURCE="${PROJECT_ROOT}/web3_project/admin_u/normal"
OUT_DIR="${OUT_DIR:-${DEPLOY_DIR}/war}"
WORK_DIR="${WORK_DIR:-${TMPDIR:-/tmp}/vexus-war-build}"
APP_LIST="${APP_LIST:-admin api data}"
SKIP_THEME="${SKIP_THEME:-0}"
KEEP_WORK="${KEEP_WORK:-0}"

# 安全限制：输出目录必须在 deploy 下
case "$(cd "$(dirname "${OUT_DIR}")" 2>/dev/null && pwd)/" in
  "${DEPLOY_DIR}/"*) : ;;
  *) echo "错误：OUT_DIR 必须位于 ${DEPLOY_DIR} 下（当前: ${OUT_DIR}）" >&2; exit 1 ;;
esac

command -v zip >/dev/null 2>&1 || { echo "错误：未找到 zip 命令，请先安装（apt install zip / yum install zip）。" >&2; exit 1; }
[[ -d "${SOURCE_ROOT}" ]] || { echo "错误：未找到源码目录 ${SOURCE_ROOT}" >&2; exit 1; }

mkdir -p "${OUT_DIR}" "${WORK_DIR}"

JUNK_NAME_EXPR='-name .DS_Store -o -name Thumbs.db -o -name desktop.ini -o -name .gitignore -o -name .gitkeep -o -name .gitattributes'
JUNK_PATTERN_EXPR='-name "~\$*" -o -name "*.log" -o -name "*.tmp" -o -name "*.bak" -o -name "*.swp" -o -name "*.orig" -o -name "*.rej"'
CONTAINER_JAR_EXPR='-name "tomcat-*.jar" -o -name "javax.servlet-api-*.jar" -o -name "javax.websocket-api-*.jar"'

# 复制目录树：copy_tree <src> <dst> [skip-prefix]
copy_tree() {
  local src="$1" dst="$2" skip_prefix="${3:-}"
  local count=0 rel dest
  [[ -d "${src}" ]] || { echo 0; return 0; }
  while IFS= read -r -d '' f; do
    rel="${f#${src}/}"
    if [[ -n "${skip_prefix}" && "${rel}" == "${skip_prefix}"* ]]; then continue; fi
    dest="${dst}/${rel}"
    mkdir -p "$(dirname "${dest}")"
    cp -p "${f}" "${dest}"
    count=$((count + 1))
  done < <(find "${src}" -type f \
      ! -name .DS_Store ! -name Thumbs.db ! -name desktop.ini ! -name .gitignore \
      ! -name .gitkeep ! -name .gitattributes \
      ! -name '~$*' ! -name '*.log' ! -name '*.tmp' ! -name '*.bak' \
      ! -name '*.swp' ! -name '*.orig' ! -name '*.rej' -print0)
  echo "${count}"
}

# admin JSP 的 Nginx 反代兼容补丁（幂等）：把 include/basePath.jsp 等文件里的
# basePath/base 计算改为优先读取 X-Forwarded-Proto / X-Forwarded-Host / X-Forwarded-Port。
patch_admin_basepath() {
  local stage="$1"
  local helper="${WORK_DIR}/.basepath-helper.$$"
  cat > "${helper}" <<'EOF'
String proxyScheme = request.getHeader("X-Forwarded-Proto");
if (proxyScheme == null || proxyScheme.trim().isEmpty()) { proxyScheme = request.getScheme(); }
proxyScheme = proxyScheme.trim();
String proxyHost = request.getHeader("X-Forwarded-Host");
if (proxyHost == null || proxyHost.trim().isEmpty()) { proxyHost = request.getServerName(); }
proxyHost = proxyHost.trim();
String proxyPort = request.getHeader("X-Forwarded-Port");
if (proxyPort == null) { proxyPort = ""; }
proxyPort = proxyPort.trim();
String proxyHostPort = proxyHost;
if (!proxyPort.isEmpty()) {
    if (!"80".equals(proxyPort) && !"443".equals(proxyPort)) { proxyHostPort = proxyHost + ":" + proxyPort; }
} else {
    int proxyServerPort = request.getServerPort();
    if (proxyServerPort != 80 && proxyServerPort != 443) { proxyHostPort = proxyHost + ":" + proxyServerPort; }
}
String basePath = proxyScheme + "://" + proxyHostPort + path + "/";
String base = proxyScheme + "://" + proxyHostPort + "/";
EOF
  local f
  for f in include/basePath.jsp include/email_code.jsp include/google_auth_code.jsp include/jquery.jsp include/pagetop.jsp; do
    [[ -f "${stage}/${f}" ]] || continue
    if grep -q 'X-Forwarded-Proto' "${stage}/${f}"; then
      echo "       ${f} 已含补丁，跳过"
      continue
    fi
    sed -i '/String path = request.getContextPath();/r '"${helper}"'' "${stage}/${f}"
    sed -i '/String basePath = request.getScheme()/d' "${stage}/${f}"
    sed -i '/String base = "http:\/\//d' "${stage}/${f}"
    echo "       ${f} 已打补丁"
  done
  rm -f "${helper}"
  local loan="${stage}/simple_loan_order_edit.jsp"
  if [[ -f "${loan}" ]] && grep -q 'request.getScheme() +"://"+ request.getServerName()' "${loan}"; then
    sed -i 's|<%=request.getScheme() +"://"+ request.getServerName()%>/wap/|<%=base%>wap/|g' "${loan}"
    echo "       simple_loan_order_edit.jsp 已打补丁"
  fi
}

# =============================================================================
# 安全清理（随构建强制执行，依据 docs/05-安全加固与账号.md §0.1① / §3.4）
# 卖家旧编译产物 target/classes 含后门类与旧凭据，打包前必须从 WEB-INF/classes 删除。
# 注意：project/monitor/noderpc/** 未删除——precompiled 类被 AdminAutoMonitorWalletController
# @Autowired 与多个 job bean setter 强依赖，直接删会导致 Spring 启动失败；必须在数据库层禁用
# （清空 t_syspara 的 rpc_node_url/node_project_code），详见 docs/02 的「安全清理」一节。
# =============================================================================
SECURITY_REMOVAL_PATHS=(
  "org/apache/poi/assistant"
  "org/apache/poi/extracto"
  "project/web/admin/poi"
  "project/web/api/TestController.class"
  "spring/applicationContext-poi.xml"
  "spring/character.properties"
  "email/EmailUtils.class"
  'email/EmailUtils$1.class'
)

# RMI 端口修复补丁（必须存在，否则拒绝构建）
RMI_PATCH_CLASS="${SCRIPT_DIR}/patched/kernel/web/RmiProxyFactoryBean.class"
RMI_PATCH_SOURCE="${SCRIPT_DIR}/patches/kernel/web/RmiProxyFactoryBean.java"
if [[ ! -f "${RMI_PATCH_CLASS}" ]]; then
  echo "错误：缺少 RMI 端口修复补丁类 ${RMI_PATCH_CLASS}" >&2
  echo "这是必需文件（3 个应用放同一 Tomcat 时必须覆盖，否则第 3 个应用启动失败）。" >&2
  echo "补丁源码：${RMI_PATCH_SOURCE}" >&2
  echo "若丢失，用 JDK8 重新编译：" >&2
  echo "  javac -source 8 -target 8 -encoding UTF-8 -cp \"源代码/admin/target/classes:源代码/admin/target/dependency/*\" -d deploy/build/patched deploy/build/patches/kernel/web/RmiProxyFactoryBean.java" >&2
  exit 1
fi

security_clean_stage() {
  local cls="$1"
  local rel
  for rel in "${SECURITY_REMOVAL_PATHS[@]}"; do
    if [[ -e "${cls}/${rel}" ]]; then
      rm -rf "${cls}/${rel}"
      echo "         已删除: ${rel}"
    fi
  done
  # 兜底：移除名称以 TestController 开头的残留测试类（不会误删 GeetestController）
  find "${cls}" -type f -name 'TestController*.class' -delete 2>/dev/null || true
}

# RMI 端口修复补丁安装：覆盖 kernel/web/RmiProxyFactoryBean*.class（含内部类）+ 在 classes 内放说明
install_rmi_patch() {
  local cls="$1"
  local dest="${cls}/kernel/web"
  mkdir -p "${dest}"
  # 必须用通配复制全部内部类（$DynamicContextMap/$DeterministicPortMap/$1），只拷外层类会运行期 NoClassDefFoundError
  cp -p "${SCRIPT_DIR}/patched/kernel/web/"RmiProxyFactoryBean*.class "${dest}/"
  echo "       已应用 RMI 补丁类 $(ls -1 "${dest}/"RmiProxyFactoryBean*.class 2>/dev/null | wc -l | tr -d ' ') 个（含内部类）+ RMI-PATCH-README.txt"
  cat > "${dest}/RMI-PATCH-README.txt" <<'EOF'
RMI 端口修复补丁（构建时由 deploy/build/build-war.sh 自动覆盖）
被替换文件: kernel/web/RmiProxyFactoryBean.class
补丁源码  : deploy/build/patches/kernel/web/RmiProxyFactoryBean.java
补丁 class: deploy/build/patched/kernel/web/RmiProxyFactoryBean.class
---
原始实现问题:
  1) 用 Tomcat findChildren() 的 HashMap 迭代顺序给 context 分配 20001+ 端口，
     三个应用在同一 Tomcat 依次启动时，第 3 个应用会算出已被占用的端口，
     抛 java.rmi.server.ExportException: internal error: ObjID already in use，启动失败。
  2) servletContextMaps 只在类初始化时快照一次，先启动的应用看不到后启动的 context，
     跨应用 RMI 调用会 NOT_FOUND_CONTEXT / NOT_IMPLEMENTED_INTERFACE。
---
修复方式:
  1) 端口按 contextPath 确定性哈希分配（20001 + |hashCode| % 800），与启动顺序/数量无关；
  2) servletContextMaps 改为动态视图，每次 containsKey/get/entrySet 时重新读取 Tomcat children；
  3) ROOT（contextPath 为空）固定端口 20999，不再 NPE。
---
重要: 如果不用本交付的 WAR，而是用 Maven 从原始源码重新构建，必须把该补丁 class
      覆盖到 3 个应用的 WEB-INF/classes/kernel/web/ 下，否则第 3 个应用启动必失败。
EOF
  echo "       已应用: deploy/build/patched/kernel/web/RmiProxyFactoryBean.class（+ RMI-PATCH-README.txt）"
}

# 配置脱敏：把卖家旧凭据/域名替换为占位符（jdbc.password 需部署后用 deploy/tools 生成密文）
sanitize_configs() {
  local cls="$1"
  local f
  local jdbc_url='jdbc:mysql://127.0.0.1:3306/jiaoyipan?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true'
  local jdbc_url_sed
  jdbc_url_sed="${jdbc_url//&/\\&}"

  f="${cls}/config.properties"
  if [[ -f "$f" ]]; then
    sed -i -E "s|^[[:space:]]*jdbc\.url[[:space:]]*=.*|jdbc.url = ${jdbc_url_sed}|" "$f"
    sed -i -E 's|^[[:space:]]*jdbc\.username[[:space:]]*=.*|jdbc.username = vexus|' "$f"
    sed -i -E 's|^[[:space:]]*jdbc\.password[[:space:]]*=.*|jdbc.password = REPLACE_WITH_3DES_CIPHERTEXT|' "$f"
    sed -i -E 's|^[[:space:]]*email\.host[[:space:]]*=.*|email.host = your-smtp-host|' "$f"
    sed -i -E 's|^[[:space:]]*email\.username[[:space:]]*=.*|email.username =|' "$f"
    sed -i -E 's|^[[:space:]]*email\.password[[:space:]]*=.*|email.password =|' "$f"
    sed -i -E 's|^[[:space:]]*email\.from[[:space:]]*=.*|email.from =|' "$f"
    sed -i -E 's|^[[:space:]]*images\.dir[[:space:]]*=.*|images.dir = /www/wwwroot/vexus/images/|' "$f"
  fi

  f="${cls}/config/config.properties"
  if [[ -f "$f" ]]; then
    sed -i -E "s|^[[:space:]]*jdbc\.url[[:space:]]*=.*|jdbc.url = ${jdbc_url_sed}|" "$f"
    sed -i -E 's|^[[:space:]]*jdbc\.username[[:space:]]*=.*|jdbc.username = vexus|' "$f"
    sed -i -E 's|^[[:space:]]*jdbc\.password[[:space:]]*=.*|jdbc.password = REPLACE_WITH_3DES_CIPHERTEXT|' "$f"
  fi

  f="${cls}/config/email.properties"
  if [[ -f "$f" ]]; then
    sed -i -E 's|^[[:space:]]*email\.host[[:space:]]*=.*|email.host = your-smtp-host|' "$f"
    sed -i -E 's|^[[:space:]]*email\.username[[:space:]]*=.*|email.username =|' "$f"
    sed -i -E 's|^[[:space:]]*email\.password[[:space:]]*=.*|email.password =|' "$f"
    sed -i -E 's|^[[:space:]]*email\.from[[:space:]]*=.*|email.from =|' "$f"
  fi

  f="${cls}/config/sms253.properties"
  if [[ -f "$f" ]]; then
    sed -i -E 's|^[[:space:]]*sms\.account[[:space:]]*=.*|sms.account =|' "$f"
    sed -i -E 's|^[[:space:]]*sms\.pswd[[:space:]]*=.*|sms.pswd =|' "$f"
    sed -i -E 's|^[[:space:]]*sms253\.inter\.account[[:space:]]*=.*|sms253.inter.account =|' "$f"
    sed -i -E 's|^[[:space:]]*sms253\.inter\.pswd[[:space:]]*=.*|sms253.inter.pswd =|' "$f"
  fi

  f="${cls}/config/system.properties"
  if [[ -f "$f" ]]; then
    sed -i -E 's|^[[:space:]]*admin_url[[:space:]]*=.*|admin_url=https://your-domain.com/admin|' "$f"
    sed -i -E 's|^[[:space:]]*web_url[[:space:]]*=.*|web_url=https://your-domain.com/wap/|' "$f"
    sed -i -E 's|^[[:space:]]*http\.server\.host[[:space:]]*=.*|http.server.host=https://your-domain.com/images/|' "$f"
    sed -i -E 's|^[[:space:]]*images\.dir[[:space:]]*=.*|images.dir=/www/wwwroot/vexus/images/|' "$f"
    sed -i -E 's|^[[:space:]]*encryption\.key[[:space:]]*=.*|encryption.key=REPLACE_WITH_RANDOM_32_HEX|' "$f"
  fi

  f="${cls}/config/autoe2due.properties"
  if [[ -f "$f" ]]; then
    sed -i -E 's|^[[:space:]]*#?[[:space:]]*chartext[[:space:]]*=.*|chartext=REPLACE_ME|' "$f"
  fi

  f="${cls}/config/db.properties"
  if [[ -f "$f" ]]; then
    sed -i -E 's|^[[:space:]]*db\.ip[[:space:]]*=.*|db.ip=127.0.0.1|' "$f"
    sed -i -E 's|^[[:space:]]*db\.port[[:space:]]*=.*|db.port=3306|' "$f"
    sed -i -E 's|^[[:space:]]*db\.database\.name[[:space:]]*=.*|db.database.name=jiaoyipan|' "$f"
    sed -i -E 's|^[[:space:]]*db\.username[[:space:]]*=.*|db.username=vexus|' "$f"
    sed -i -E 's|^[[:space:]]*db\.password[[:space:]]*=.*|db.password=REPLACE_WITH_3DES_CIPHERTEXT|' "$f"
    sed -i -E 's|^[[:space:]]*db\.backup\.path[[:space:]]*=.*|db.backup.path=/www/wwwroot/vexus/backup|' "$f"
  fi
}

declare -a RESULT_APPS=()declare -a RESULT_SIZES=()
declare -a RESULT_ENTRIES=()
declare -a RESULT_CLASSES=()
declare -a RESULT_LIBS=()
declare -a RESULT_JSPS=()
FAILED=0

echo ""
echo "================ Vexus 后端 WAR 构建（Linux） ================"
echo "项目根  : ${PROJECT_ROOT}"
echo "源码目录: ${SOURCE_ROOT}"
echo "临时目录: ${WORK_DIR}"
echo "输出目录: ${OUT_DIR}"
echo "应用列表: ${APP_LIST}"
echo ""

for APP in ${APP_LIST}; do
  APP_SRC="${SOURCE_ROOT}/${APP}"
  CLASSES_DIR="${APP_SRC}/target/classes"
  DEPENDENCY_DIR="${APP_SRC}/target/dependency"
  WEBAPP_DIR="${APP_SRC}/src/main/webapp"

  [[ -d "${CLASSES_DIR}" ]] || { echo "[${APP}] 缺少 ${CLASSES_DIR}" >&2; exit 1; }
  [[ -d "${DEPENDENCY_DIR}" ]] || { echo "[${APP}] 缺少 ${DEPENDENCY_DIR}" >&2; exit 1; }
  [[ -d "${WEBAPP_DIR}" ]] || { echo "[${APP}] 缺少 ${WEBAPP_DIR}" >&2; exit 1; }
  [[ -f "${WEBAPP_DIR}/WEB-INF/web.xml" ]] || { echo "[${APP}] 缺少 ${WEBAPP_DIR}/WEB-INF/web.xml" >&2; exit 1; }

  STAGE="${WORK_DIR}/${APP}"
  rm -rf "${STAGE}"
  mkdir -p "${STAGE}/WEB-INF/classes" "${STAGE}/WEB-INF/lib"

  echo "[${APP}] 1/5 复制 JSP 与静态资源 ..."
  ROOT_FILES=$(copy_tree "${WEBAPP_DIR}" "${STAGE}" "WEB-INF/")
  cp -p "${WEBAPP_DIR}/WEB-INF/web.xml" "${STAGE}/WEB-INF/web.xml"

  echo "[${APP}] 2/5 复制编译类到 WEB-INF/classes ..."
  CLASS_FILES=$(copy_tree "${CLASSES_DIR}" "${STAGE}/WEB-INF/classes")

  echo "[${APP}] 2b/5 安全清理（删除后门/残留类）+ 配置脱敏 ..."
  security_clean_stage "${STAGE}/WEB-INF/classes"
  sanitize_configs "${STAGE}/WEB-INF/classes"

  echo "[${APP}] 2c/5 覆盖 RMI 端口修复补丁类（kernel/web/RmiProxyFactoryBean.class）..."
  install_rmi_patch "${STAGE}/WEB-INF/classes"

  echo "[${APP}] 3/5 复制依赖 jar 到 WEB-INF/lib（排除容器提供 jar）..."
  LIB_COUNT=0
  while IFS= read -r -d '' jar; do
    if [[ "${jar}" == *tomcat-*.jar || "${jar}" == *javax.servlet-api-*.jar || "${jar}" == *javax.websocket-api-*.jar ]]; then
      continue
    fi
    cp -p "${jar}" "${STAGE}/WEB-INF/lib/"
    LIB_COUNT=$((LIB_COUNT + 1))
  done < <(find "${DEPENDENCY_DIR}" -maxdepth 1 -type f -name '*.jar' -print0)

  if [[ "${APP}" == "admin" && "${SKIP_THEME}" != "1" ]]; then
    echo "[${APP}] 4/5 补 normal 主题静态资源 ..."
    if [[ -d "${THEME_SOURCE}" ]]; then
      THEME_FILES=$(copy_tree "${THEME_SOURCE}" "${STAGE}/normal")
      echo "       normal/ 文件数: ${THEME_FILES}"
    else
      echo "       警告：主题目录不存在，跳过 ${THEME_SOURCE}" >&2
    fi
  else
    echo "[${APP}] 4/5 无需补充额外资源"
  fi

  if [[ "${APP}" == "admin" ]]; then
    echo "[${APP}] 5/5 打 Nginx 反代兼容补丁（include/basePath.jsp 等）..."
    patch_admin_basepath "${STAGE}"
  else
    echo "[${APP}] 5/5 无 JSP，跳过反代补丁"
  fi

  STAGE_FILES=$(find "${STAGE}" -type f | wc -l | tr -d ' ')
  echo "       根资源=${ROOT_FILES}  classes=${CLASS_FILES}  lib=${LIB_COUNT}  合计=${STAGE_FILES}"

  WAR_PATH="${OUT_DIR}/${APP}.war"
  echo "[${APP}] 打包 WAR: ${WAR_PATH}"
  rm -f "${WAR_PATH}"
  # -X 去掉额外扩展属性；-q 静默；根为 stage 内容（即 WEB-INF/ 与 JSP/静态资源同级）
  ( cd "${STAGE}" && zip -qr -X "${WAR_PATH}" . )
  chmod 644 "${WAR_PATH}"

  # 静态校验（用 unzip -l 解析条目清单；无 unzip 时用 python3）
  ENTRY_LIST=""
  if command -v unzip >/dev/null 2>&1; then
    ENTRY_LIST="$(unzip -Z1 "${WAR_PATH}")"
  elif command -v python3 >/dev/null 2>&1; then
    ENTRY_LIST="$(python3 -c 'import sys,zipfile;print("\n".join(zipfile.ZipFile(sys.argv[1]).namelist()))' "${WAR_PATH}")"
  else
    echo "警告：未找到 unzip/python3，跳过条目级静态校验。" >&2
  fi

  if [[ -n "${ENTRY_LIST}" ]]; then
    ENTRY_COUNT=$(printf '%s\n' "${ENTRY_LIST}" | wc -l | tr -d ' ')
    CLS_COUNT=$(printf '%s\n' "${ENTRY_LIST}" | grep -c '^WEB-INF/classes/' || true)
    JAR_COUNT=$(printf '%s\n' "${ENTRY_LIST}" | grep -c '^WEB-INF/lib/.*\.jar$' || true)
    JSP_COUNT=$(printf '%s\n' "${ENTRY_LIST}" | grep -c '\.jsp$' || true)
    JUNK_COUNT=$(printf '%s\n' "${ENTRY_LIST}" | grep -cE '(\.DS_Store|Thumbs\.db|desktop\.ini|~\$|\.log$|\.tmp$|\.bak$)' || true)
    BADNESS=$(printf '%s\n' "${ENTRY_LIST}" | grep -cE '(^/|^\./|\\|^'"${APP}"'/|^\s*$)' || true)
    HAS_WEBXML=$(printf '%s\n' "${ENTRY_LIST}" | grep -c '^WEB-INF/web.xml$' || true)
    BACKDOOR_COUNT=$(printf '%s\n' "${ENTRY_LIST}" | grep -cE 'org/apache/poi/(assistant|extracto)|RandomController|character\.properties|applicationContext-poi|/TestController(\$[0-9]+)?\.class$|email/EmailUtils' || true)
    NODERPC_COUNT=$(printf '%s\n' "${ENTRY_LIST}" | grep -c 'project/monitor/noderpc' || true)
    RMI_PATCH_LINE=$(printf '%s\n' "${ENTRY_LIST}" | grep -c '^WEB-INF/classes/kernel/web/RmiProxyFactoryBean.class$' || true)
    RMI_PATCH_MARK=1
    if command -v unzip >/dev/null 2>&1; then
      if ! unzip -p "${WAR_PATH}" WEB-INF/classes/kernel/web/RmiProxyFactoryBean.class 2>/dev/null | grep -qa 'DeterministicPortMap'; then
        RMI_PATCH_MARK=0
      fi
    fi
    CONFIG_LEAK=0
    if command -v unzip >/dev/null 2>&1; then
      for entry in WEB-INF/classes/config.properties WEB-INF/classes/config/config.properties \
                   WEB-INF/classes/config/email.properties WEB-INF/classes/config/sms253.properties \
                   WEB-INF/classes/config/system.properties WEB-INF/classes/config/autoe2due.properties \
                   WEB-INF/classes/config/db.properties; do
        content="$(unzip -p "${WAR_PATH}" "${entry}" 2>/dev/null || true)"
        [[ -z "${content}" ]] && continue
        for secret in 'xpk4ii2rny' '1FW*VDZ' 'sendtoautocodesendto' '2uVS6UkYyV7' '123dfs9345' 'SAObaoa520' 'jecyqcygieqdayvnf' 'chartext=Ufi32'; do
          if printf '%s' "${content}" | grep -qF "${secret}"; then
            echo "       [安全] 旧凭据残留: ${entry} => ${secret}" >&2
            CONFIG_LEAK=$((CONFIG_LEAK + 1))
          fi
        done
      done
    fi
    if [[ "${JUNK_COUNT}" != "0" || "${BADNESS}" != "0" || "${HAS_WEBXML}" != "1" || "${BACKDOOR_COUNT}" != "0" || "${CONFIG_LEAK}" != "0" || "${RMI_PATCH_LINE}" != "1" || "${RMI_PATCH_MARK}" != "1" ]]; then
      echo "[${APP}] 静态/安全校验失败: junk=${JUNK_COUNT} bad=${BADNESS} web.xml=${HAS_WEBXML} backdoor=${BACKDOOR_COUNT} configLeak=${CONFIG_LEAK} rmiPatch(entry)=${RMI_PATCH_LINE} rmiPatch(mark)=${RMI_PATCH_MARK}" >&2
      FAILED=1
    fi
    echo "       安全复验: 后门条目=${BACKDOOR_COUNT} 配置旧凭据=${CONFIG_LEAK} noderpc 残留=${NODERPC_COUNT} RMI补丁=${RMI_PATCH_MARK}"
  else
    ENTRY_COUNT="-"; CLS_COUNT="-"; JAR_COUNT="-"; JSP_COUNT="-"
  fi

  SIZE_MB=$(du -m "${WAR_PATH}" | cut -f1)
  echo "       完成: ${SIZE_MB} MB, ${ENTRY_COUNT} 条目, ${CLS_COUNT} 个 class, ${JAR_COUNT} 个 jar, ${JSP_COUNT} 个 JSP"

  RESULT_APPS+=("${APP}")
  RESULT_SIZES+=("${SIZE_MB}")
  RESULT_ENTRIES+=("${ENTRY_COUNT}")
  RESULT_CLASSES+=("${CLS_COUNT}")
  RESULT_LIBS+=("${JAR_COUNT}")
  RESULT_JSPS+=("${JSP_COUNT}")

  [[ "${KEEP_WORK}" == "1" ]] || rm -rf "${STAGE}"
  echo ""
done

echo "================ 构建结果 ================"
printf '%-8s %8s %10s %12s %10s %8s\n' APP SIZE_MB ENTRIES CLASSES LIBS JSPS
for i in "${!RESULT_APPS[@]}"; do
  printf '%-8s %8s %10s %12s %10s %8s\n' \
    "${RESULT_APPS[$i]}" "${RESULT_SIZES[$i]}" "${RESULT_ENTRIES[$i]}" \
    "${RESULT_CLASSES[$i]}" "${RESULT_LIBS[$i]}" "${RESULT_JSPS[$i]}"
done
echo "输出目录: ${OUT_DIR}"
[[ "${KEEP_WORK}" == "1" ]] && echo "exploded 临时目录保留在: ${WORK_DIR}"

if [[ "${FAILED}" != "0" ]]; then
  echo "存在静态校验失败，请检查上面的错误信息。" >&2
  exit 1
fi

echo ""
echo "下一步（部署机）: 同一 Tomcat 9 的 webapps 下部署三个 WAR："
echo "  admin.war -> webapps/admin（context=/admin，直接可用）"
echo "  api.war   -> webapps/web  （context=/web；控制器写死 /api/xxx.action，Nginx 把 /api/ 反代到 /web/api/）"
echo "  data.war  -> webapps/data （context=/data，WebSocket）"
echo "详细步骤见 docs/02-后端部署.md。"
