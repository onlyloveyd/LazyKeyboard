plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "cn.onlyloveyd.lazybear"
    compileSdk = 36

    defaultConfig {
        applicationId = "cn.onlyloveyd.lazybear"
        minSdk = 23
        targetSdk = 36
        versionCode = 2
        versionName = "1.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(project(":lazykeyboard"))
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
}
