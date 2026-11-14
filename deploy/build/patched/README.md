# deploy/build/patched —— 构建期强制覆盖的补丁 class

本目录存放**必须覆盖进 WAR** 的补丁编译产物，构建脚本会发现缺失时直接报错退出，避免静默产出会挂的包。

## kernel/web/RmiProxyFactoryBean*.class（4 个文件，缺一不可）

| 项 | 值 |
|---|---|
| 覆盖目标 | 三个 WAR 的 `WEB-INF/classes/kernel/web/`：`RmiProxyFactoryBean.class` + `RmiProxyFactoryBean$1.class` + `RmiProxyFactoryBean$DynamicContextMap.class` + `RmiProxyFactoryBean$DeterministicPortMap.class` |
| 补丁源码 | `deploy/build/patches/kernel/web/RmiProxyFactoryBean.java` |
| 产物来源 | local-verify 实测环境中编译并经"三应用同 Tomcat 启动"验证的产物（源码相同，编译带完整调试信息，外层 class 5558B） |
| 编译方式 | JDK 8 `javac -source 8 -target 8 -encoding UTF-8` |
| 外层 class SHA256 | `46AE20C97A9BBD4F648456A53263D18DD6E1B8BADBD05D464E6E04065FDD6E0A`（5558B） |
| 内部类 SHA256 | `$1`=`931C7345...C8606B18`（227B）、`$DynamicContextMap`=`A801208C...6609D2AE`（1484B）、`$DeterministicPortMap`=`6226D18E...204D22D9`（1095B） |

> ⚠️ 只拷外层 class 会运行期 `NoClassDefFoundError`：外层类引用了上面 3 个内部类。
> 构建脚本用通配 `RmiProxyFactoryBean*.class` 复制全部 4 个，并对内部类做存在性断言。

### 为什么必须打这个补丁

原始 `kernel/web/RmiProxyFactoryBean.java` 有两个致命问题，三个应用部署在**同一个 Tomcat** 时必现：

1. **RMI 端口按 HashMap 迭代顺序分配**：静态块用
   `standardContext.getParent().findChildren()` 枚举 context，按枚举顺序从 20001 起分配端口。
   实测 admin/api 先启动后，data 又算出 20002 → `LocateRegistry.createRegistry(20002)` 二次创建，
   抛 `java.rmi.server.ExportException: internal error: ObjID already in use`，
   `Context[/data]` 启动失败（第 3 个应用必挂）。
2. **context 表只快照一次**：`servletContextMaps` 在类初始化时快照，先启动的应用看不到后启动的
   context，跨应用 RMI 调用会找不到 Bean（`NOT_FOUND_CONTEXT` / `NOT_IMPLEMENTED_INTERFACE`）。

### 补丁逻辑

- 端口：`20001 + |contextPath.hashCode()| % 800`，**与启动顺序、context 数量无关**；
- `servletContextMaps`：`DynamicContextMap`，每次 `containsKey/get/entrySet` 前重新读取 Tomcat children；
- ROOT（contextPath 为空）：固定端口 20999，不再 NPE。

### 重新编译（补丁 class 丢失时）

在项目根（含 `源代码` 的目录）执行：

```bash
# Linux / macOS（JDK8）
javac -source 8 -target 8 -encoding UTF-8 \
  -cp "源代码/admin/target/classes:源代码/admin/target/dependency/*" \
  -d deploy/build/patched \
  deploy/build/patches/kernel/web/RmiProxyFactoryBean.java
```

```powershell
# Windows（JDK8；路径分隔符用 ;）
javac -source 8 -target 8 -encoding UTF-8 `
  -cp "源代码\admin\target\classes;源代码\admin\target\dependency\*" `
  -d deploy\build\patched `
  deploy\build\patches\kernel\web\RmiProxyFactoryBean.java
```

编译后确认：`deploy/build/patched/kernel/web/RmiProxyFactoryBean*.class` 共 **4 个文件**存在
（外层 + `$1` + `$DynamicContextMap` + `$DeterministicPortMap`，javac 会自动生成内部类）。

> ⚠️ 若你**不用本交付的 WAR**，而是用 Maven 从原始源码重新构建，必须手动把该补丁 class
> 覆盖到 3 个应用的 `WEB-INF/classes/kernel/web/` 下，否则第 3 个应用启动必失败。
