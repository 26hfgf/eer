import java.net.URI

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.aibeautypro"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.aibeautypro"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources.excludes += setOf(
            "META-INF/DEPENDENCIES",
            "META-INF/LICENSE",
            "META-INF/LICENSE.txt",
            "META-INF/NOTICE",
            "META-INF/NOTICE.txt"
        )
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.activity:activity-ktx:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.exifinterface:exifinterface:1.4.2")
    implementation("com.google.android.material:material:1.13.0")

    val cameraX = "1.6.1"
    implementation("androidx.camera:camera-core:$cameraX")
    implementation("androidx.camera:camera-camera2:$cameraX")
    implementation("androidx.camera:camera-lifecycle:$cameraX")
    implementation("androidx.camera:camera-view:$cameraX")

    implementation("com.google.mediapipe:tasks-vision:0.10.35")

    val room = "2.8.4"
    implementation("androidx.room:room-runtime:$room")
    implementation("androidx.room:room-ktx:$room")
    kapt("androidx.room:room-compiler:$room")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    testImplementation("junit:junit:4.13.2")
}

val faceLandmarkerModel = layout.projectDirectory.file(
    "src/main/assets/face_landmarker.task"
).asFile

val downloadFaceLandmarkerModel by tasks.registering {
    group = "build setup"
    description = "Downloads the official MediaPipe Face Landmarker model when it is missing."
    outputs.file(faceLandmarkerModel)

    doLast {
        if (faceLandmarkerModel.exists() && faceLandmarkerModel.length() > 3_000_000L) {
            return@doLast
        }

        faceLandmarkerModel.parentFile.mkdirs()
        val temporaryFile = File(faceLandmarkerModel.parentFile, "face_landmarker.task.part")
        val modelUrl = URI(
            "https://storage.googleapis.com/mediapipe-models/face_landmarker/" +
                "face_landmarker/float16/latest/face_landmarker.task"
        ).toURL()

        println("Downloading MediaPipe Face Landmarker model...")
        modelUrl.openStream().buffered().use { input ->
            temporaryFile.outputStream().buffered().use { output ->
                input.copyTo(output)
            }
        }
        check(temporaryFile.length() > 3_000_000L) {
            "Downloaded Face Landmarker model is incomplete."
        }
        if (faceLandmarkerModel.exists()) faceLandmarkerModel.delete()
        check(temporaryFile.renameTo(faceLandmarkerModel)) {
            "Unable to move downloaded Face Landmarker model into assets."
        }
    }
}

tasks.named("preBuild").configure {
    dependsOn(downloadFaceLandmarkerModel)
}
