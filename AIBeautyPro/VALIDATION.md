# 工程验证记录

验证日期：2026-08-03

## 已完成检查

- 24 个 Manifest/资源 XML 文件均可被 XML 解析器读取。
- 26 个 Kotlin 源文件均已纳入工程结构检查。
- Manifest 声明的 6 个 Activity 均存在对应 Kotlin 类。
- ViewBinding 代码引用与 8 个布局文件中的 ID 一致。
- 本地 drawable/layout/xml/color/string/style 资源引用未发现缺失。
- 核心比例分析、脸型分类、评分、发型推荐和 HairLibrary 已通过 JVM stub 编译及冒烟运行。
- HairRenderer 已做 Kotlin 语法/stub 编译检查。
- Gradle Wrapper 主类已按 Java 17 字节码（class major version 61）重新编译。
- Wrapper 已使用本地模拟 Gradle 发行包完成启动和参数转发测试。
- FileProvider 覆盖应用内部文件、缓存、外部应用目录及 Android 9 及以下的相册目录。
- Room Entity、DAO 查询字段与 Activity 写入/读取字段已做静态一致性检查。

## 当前环境未执行的检查

当前生成环境没有 Android SDK，也不能通过 shell 访问 Maven/Gradle 网络仓库，因此未实际运行：

```text
./gradlew assembleDebug
```

工程已包含 Wrapper 与完整源码，但“真实 Android 编译成功”仍需在装有 Android SDK 36、JDK 17 且可联网下载依赖的 Android Studio 环境中最终确认。

## 首次构建注意事项

`preBuild` 会自动下载官方 `face_landmarker.task` 到 `app/src/main/assets/`。无法联网时，需要手动放入该模型文件。
