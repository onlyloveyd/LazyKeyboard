plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
}

android {
    namespace = "com.gs.keyboard"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
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

    testOptions {
        unitTests {
            // Robolectric 测试需要访问 res/xml 键盘布局等资源
            isIncludeAndroidResources = true
        }
    }

    publishing {
        singleVariant("release")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.github.onlyloveyd"
            artifactId = "LazyKeyboard"
            version = "v1.6"

            afterEvaluate { from(components["release"]) }
        }
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}
