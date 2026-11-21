import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.File

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.materialIconsExtended)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.activity.compose)
            implementation("androidx.fragment:fragment-ktx:1.8.5")
            implementation(libs.mlkit.text.recognition)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.apache.pdfbox)
        }
    }
}

android {
    namespace = "io.github.ilievi187.recurra"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "io.github.ilievi187.recurra"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 7
        versionName = "1.4.1"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    val keystorePath = System.getenv("RECURRA_KEYSTORE_PATH")
    val keystorePassword = System.getenv("RECURRA_KEYSTORE_PASSWORD")
    val keyAliasName = System.getenv("RECURRA_KEY_ALIAS")
    val keyPasswordValue = System.getenv("RECURRA_KEY_PASSWORD")

    signingConfigs {
        if (!keystorePath.isNullOrBlank() && File(keystorePath).exists() &&
            !keystorePassword.isNullOrBlank() && !keyAliasName.isNullOrBlank() && !keyPasswordValue.isNullOrBlank()
        ) {
            create("release") {
                storeFile = File(keystorePath)
                storePassword = keystorePassword
                keyAlias = keyAliasName
                keyPassword = keyPasswordValue
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val releaseConfig = signingConfigs.findByName("release")
            signingConfig = releaseConfig ?: signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

compose.desktop {
    application {
        mainClass = "io.github.ilievi187.recurra.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Deb, TargetFormat.Rpm, TargetFormat.Msi, TargetFormat.Exe)
            packageName = "Recurra"
            packageVersion = "1.4.1"
            description = "Privacy-first, offline-first subscription, recurring bill, and warranty tracker."
            vendor = "ilievi187"
            copyright = "Copyright (c) 2026 ilievi187. Licensed under MIT."
            licenseFile.set(rootProject.file("LICENSE"))

            modules("java.base", "java.desktop", "java.logging", "java.naming", "jdk.crypto.ec")

            linux {
                packageName = "recurra"
                debMaintainer = "ilievi187 <ilievi187@users.noreply.github.com>"
                menuGroup = "Office;Finance"
                appCategory = "Finance"
                iconFile.set(rootProject.file("packaging/icons/recurra.png"))
            }

            windows {
                menuGroup = "Recurra"
                perUserInstall = true
                dirChooser = true
                shortcut = true
                upgradeUuid = "b94c60b0-8c3e-4d91-9e1a-22a1b43e7c10"
                iconFile.set(rootProject.file("packaging/icons/recurra.ico"))
            }
        }
    }
}
