import com.android.build.gradle.tasks.ProcessApplicationManifest
import org.jose4j.base64url.Base64

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    kotlin("plugin.serialization") version "2.0.20"
}

android {
    namespace = "de.timklge.karoonotepad"
    compileSdk = 34

    defaultConfig {
        applicationId = "de.timklge.karoonotepad"
        minSdk = 26
        targetSdk = 34
        versionCode = 100 + (System.getenv("BUILD_NUMBER")?.toInt() ?: 1)
        versionName = System.getenv("RELEASE_VERSION") ?: "1.0"
    }

    signingConfigs {
        create("release") {
            val env: MutableMap<String, String> = System.getenv()
            keyAlias = env["KEY_ALIAS"]
            keyPassword = env["KEY_PASSWORD"]

            val base64keystore: String = env["KEYSTORE_BASE64"] ?: ""
            val keystoreFile: File = File.createTempFile("keystore", ".jks")
            keystoreFile.writeBytes(Base64.decode(base64keystore))
            storeFile = keystoreFile
            storePassword = env["KEYSTORE_PASSWORD"]
        }
    }
    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}


tasks.register("generateManifest") {
    description = "Generates manifest.json with current version information"
    group = "build"

    doLast {
        val baseUrl = System.getenv("BASE_URL") ?: "https://github.com/timklge/karoo-notepad/releases/latest/download"
        val manifestFile = file("$projectDir/manifest.json")
        val manifest = mapOf(
            "label" to "Notepad",
            "packageName" to "de.timklge.karoonotepad",
            "iconUrl" to "$baseUrl/karoo-notepad.png",
            "latestApkUrl" to "$baseUrl/app-release.apk",
            "latestVersion" to android.defaultConfig.versionName,
            "latestVersionCode" to android.defaultConfig.versionCode,
            "developer" to "github.com/timklge",
            "description" to "Open-source extension that provides a simple notepad",
            "releaseNotes" to "* Fix notepad description",
            "screenshotUrls" to listOf(
                "$baseUrl/menu.png",
                "$baseUrl/profile.png",
                "$baseUrl/datapage.png",
            )
        )

        val gson = groovy.json.JsonBuilder(manifest).toPrettyString()
        manifestFile.writeText(gson)
        println("Generated manifest.json with version ${android.defaultConfig.versionName} (${android.defaultConfig.versionCode})")

        if (System.getenv()["BASE_URL"] != null){
            val androidManifestFile = file("$projectDir/src/main/AndroidManifest.xml")
            var androidManifestContent = androidManifestFile.readText()
            androidManifestContent = androidManifestContent.replace("\$BASE_URL\$", baseUrl)
            androidManifestFile.writeText(androidManifestContent)
            println("Replaced \$BASE_URL$ in AndroidManifest.xml")
        }
    }
}

tasks.named("assemble") {
    dependsOn("generateManifest")
}

tasks.withType<ProcessApplicationManifest>().configureEach {
    if (name == "processDebugMainManifest" || name == "processReleaseMainManifest") {
        dependsOn(tasks.named("generateManifest"))
    }
}

dependencies {
    implementation(libs.hammerhead.karoo.ext)
    implementation(libs.androidx.core.ktx)
    implementation(libs.bundles.androidx.lifeycle)
    implementation(libs.androidx.activity.compose)
    implementation(libs.bundles.compose.ui)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.color)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.glance.appwidget)
}