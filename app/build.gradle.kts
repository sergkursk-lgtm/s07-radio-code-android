import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Реквизиты подписи лежат в local.properties: файл перечислен в .gitignore,
// поэтому ключ и пароли не попадают в публичный репозиторий.
// Без них release-сборка получается неподписанной — это видно в apksigner.
val signingProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val releaseStoreFile = signingProps.getProperty("RELEASE_STORE_FILE")
val releaseStorePassword = signingProps.getProperty("RELEASE_STORE_PASSWORD")
val releaseKeyAlias = signingProps.getProperty("RELEASE_KEY_ALIAS")
val releaseKeyPassword = signingProps.getProperty("RELEASE_KEY_PASSWORD") ?: releaseStorePassword
val releaseKeyFile = releaseStoreFile?.let { rootProject.file(it) }

android {
    namespace = "com.soueast.s07code"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.soueast.s07code"
        minSdk = 26
        targetSdk = 34
        versionCode = 9
        versionName = "1.9"
    }

    signingConfigs {
        if (releaseKeyFile?.exists() == true && releaseStorePassword != null) {
            create("release") {
                storeFile = releaseKeyFile
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias ?: "s07radio"
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.7"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    testImplementation("junit:junit:4.13.2")
}
