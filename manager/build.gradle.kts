import com.android.build.api.dsl.ApplicationDefaultConfig
import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.api.AndroidBasePlugin
import java.io.ByteArrayOutputStream

plugins {
    alias(libs.plugins.agp.app) apply false
    alias(libs.plugins.agp.lib) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.lsplugin.cmaker)
}

cmaker {
    default {
        arguments.addAll(
            arrayOf(
                "-DANDROID_STL=none",
            )
        )
        abiFilters("arm64-v8a", "x86_64")
    }
    buildTypes {
        if (it.name == "release") {
            arguments += "-DDEBUG_SYMBOLS_PATH=${layout.buildDirectory.asFile.get().absolutePath}/symbols"
        }
    }
}

val androidMinSdkVersion = 26
val androidTargetSdkVersion = 36
val androidCompileSdkVersion = 36
val androidBuildToolsVersion = "36.1.0"
val androidCompileNdkVersion by extra(libs.versions.ndk.get())
val androidSourceCompatibility = JavaVersion.VERSION_21
val androidTargetCompatibility = JavaVersion.VERSION_21
// Upstream numbering: shared with ksud and the kernel driver (userspace/ksud/build.rs).
// Keep it around so features that pair the manager with the bundled LKM still line up.
val managerBaseVersionCode by extra(getUpstreamVersionCode())

// This fork deliberately outranks upstream. The in-app updater compares the raw
// version code against upstream's release assets, and the package manager refuses
// to install a lower code over a higher one, so a large offset means an upstream
// APK can never present itself as an update to a build made from this tree.
// Override with -PmanagerVersionOffset=... / -PmanagerVersionMajor=... when needed.
val managerVersionOffset = (findProperty("managerVersionOffset") as String?)?.toInt() ?: 900_000
val managerVersionMajor = (findProperty("managerVersionMajor") as String?)?.toInt() ?: 9

val managerVersionCode by extra(getUpstreamVersionCode() + managerVersionOffset)
val managerVersionName by extra(getVersionName())

fun getGitCommitCount(): Int {
    val process = Runtime.getRuntime().exec(arrayOf("git", "rev-list", "--count", "HEAD"))
    return process.inputStream.bufferedReader().use { it.readText().trim().toInt() }
}

fun getGitDescribe(): String {
    val process = Runtime.getRuntime().exec(arrayOf("git", "describe", "--tags", "--always"))
    return process.inputStream.bufferedReader().use { it.readText().trim() }
}

fun getUpstreamVersionCode(): Int {
    val commitCount = getGitCommitCount()
    val major = 1
    return major * 30000 + commitCount
}

fun getGitShortSha(): String {
    val process = Runtime.getRuntime().exec(arrayOf("git", "rev-parse", "--short", "HEAD"))
    return process.inputStream.bufferedReader().use { it.readText().trim() }
}

// Fork-major leading the name so it also reads as newer than upstream at a glance;
// the short sha keeps every build traceable back to a commit.
fun getVersionName(): String {
    val sha = getGitShortSha().ifBlank { getGitDescribe() }
    return "v$managerVersionMajor.0.0-$sha"
}

subprojects {
    plugins.withType(AndroidBasePlugin::class.java) {
        extensions.configure(CommonExtension::class.java) {
            compileSdk = androidCompileSdkVersion
            ndkVersion = androidCompileNdkVersion

            defaultConfig {
                minSdk = androidMinSdkVersion
                if (this is ApplicationDefaultConfig) {
                    targetSdk = androidTargetSdkVersion
                    versionCode = managerVersionCode
                    versionName = managerVersionName
                }
                ndk {
                    abiFilters += listOf("arm64-v8a", "x86_64")
                }
            }

            lint {
                abortOnError = true
                checkReleaseBuilds = false
            }

            compileOptions {
                sourceCompatibility = androidSourceCompatibility
                targetCompatibility = androidTargetCompatibility
            }
        }
    }
}
