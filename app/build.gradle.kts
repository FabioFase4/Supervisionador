import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.kotlin.android)
}

// Lendo a chave do local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

val chatboxProperties = Properties().apply {
    rootProject.file("../chatbox/local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
fun chatConfig(name: String, fallback: String = ""): String =
    (System.getenv(name) ?: localProperties.getProperty(name)
        ?: chatboxProperties.getProperty(name) ?: fallback)
        .replace("\\", "\\\\").replace("\"", "\\\"")

android {
    namespace = "com.fabio.eagleeyes"
    compileSdk = 34

    buildFeatures {
        viewBinding = true
        buildConfig = true // Habilita o BuildConfig
    }

    defaultConfig {
        applicationId = "com.fabio.eagleeyes"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "3.2.1"
        multiDexEnabled = true
        buildConfigField("String", "OPENROUTER_API_KEY", "\"${chatConfig("OPENROUTER_API_KEY")}\"")
        buildConfigField("String", "OPENROUTER_MODEL", "\"${chatConfig("OPENROUTER_MODEL", "dots-studio/dots-3-note-preview:free")}\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        resourceConfigurations += listOf("en", "pt-rBR")

        // Define a variável GEMINI_API_KEY no BuildConfig
        buildConfigField("String", "GEMINI_API_KEY", "\"${localProperties.getProperty("GEMINI_API_KEY") ?: ""}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
}
