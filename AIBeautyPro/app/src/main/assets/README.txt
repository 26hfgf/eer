Face Landmarker 模型目标路径：
  app/src/main/assets/face_landmarker.task

首次构建时，app/build.gradle.kts 中的 downloadFaceLandmarkerModel 任务会从 Google 官方模型仓库自动下载该文件。
如需离线构建，请预先手动放入官方 face_landmarker.task。
