# AIBeautyPro（AI颜值大师 Pro）

一个可直接导入 Android Studio 的完整 Android 示例工程，包含拍照/选图、MediaPipe 人脸关键点、五官比例分析、娱乐性评分、脸型判断、发型推荐、二维发型试戴、PDF 报告和 Room 历史记录。

## 环境

- Android Studio（支持 AGP 8.13）
- JDK 17
- Android SDK 36
- 最低 Android 6.0（API 23）
- 首次构建需要联网下载 Gradle、Maven 依赖和约 3.6 MB 的官方 Face Landmarker 模型

## 构建

1. 用 Android Studio 打开本目录。
2. 选择 JDK 17，安装 Android SDK 36。
3. 等待 Gradle Sync 完成。
4. 连接真机或启动模拟器并运行 `app`。

也可在项目根目录执行：

```bash
./gradlew assembleDebug
```

APK 输出位置：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 模型处理

官方模型文件名为：

```text
app/src/main/assets/face_landmarker.task
```

为了让 ZIP 保持轻量，模型二进制未直接打包；`app/build.gradle.kts` 中的 `downloadFaceLandmarkerModel` 会在首次 `preBuild` 前从 Google 官方地址下载。如果构建环境不能访问该地址，可手动下载 Face Landmarker 模型并放到上述路径，再离线构建。

## 已实现

- CameraX 前置优先拍照，后置相机回退
- 系统相册选图与 EXIF 方向修正
- MediaPipe Face Landmarker 单脸检测
- 五官比例、左右对称度、脸型与娱乐性分数
- 五种本地程序化发型模板与脸型推荐
- 基于关键点的发型缩放、旋转、定位和透明图层融合
- 生成图片保存到系统相册、私有目录分享
- Android 原生 `PdfDocument` 两页报告
- Room 历史记录、详情、删除、清空、打开 PDF、分享图片
- 浅色/深色主题资源与 ViewBinding UI

## 主要目录

```text
app/src/main/java/com/aibeautypro
├── MainActivity.kt
├── CameraActivity.kt
├── ResultActivity.kt
├── HairActivity.kt
├── HistoryActivity.kt
├── RecordDetailActivity.kt
├── ai/
├── adapter/
├── database/
├── model/
├── report/
└── utils/
```

## 产品与安全说明

- 分数只基于几何比例，是娱乐性结果，不代表客观审美或个人价值。
- 项目不进行身份识别、健康判断、种族推断或能力判断。
- 发型试戴是本地二维透明图层融合，不是云端生成式换脸；正脸、无遮挡照片效果更稳定。
- PDF 使用 Android 原生 `PdfDocument`，避免默认引入 iText 可能带来的额外许可与体积问题。

详见 `VALIDATION.md`。
