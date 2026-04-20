import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

group = "me.xmbet"
version = "1.0.3"
val appName by extra("EasyADB")
val appVersion by extra("1.0.3")

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.aliyun.com/repository/public/")
    maven("https://maven.aliyun.com/repository/central")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.compose.material3:material3:${properties["material3.version"]}") // Material 3 组件
    implementation("org.jetbrains.compose.material:material-icons-extended:${properties["material.icons.extended.version"]}") // 扩展图标
    implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:${properties["lifecycle.viewmodel.compose.version"]}") // viewModule
    implementation("org.jetbrains.androidx.navigation3:navigation3-ui:${properties["navigation3.version"]}") // Navigation3

    implementation("com.google.code.gson:gson:${properties["gson.version"]}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${properties["kotlin.version"]}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${properties["kotlin.version"]}")

    // 文件选择
    implementation("io.github.vinceglb:filekit-core:0.10.0")
    implementation("io.github.vinceglb:filekit-dialogs:0.10.0")
    // adb封装
    implementation(project(":ddmlib"))
    implementation("com.android.tools.ddms:ddmlib:${properties["ddmlib.version"]}")
    implementation("com.android.tools:common:${properties["ddmlib.version"]}")
    // 数据持久化
    implementation("com.russhwolf:multiplatform-settings:${properties["multiplatform.settings.version"]}")
    testImplementation(kotlin("test"))
}

compose.desktop {
    application {
        mainClass = "MainKt"

        buildTypes.release{
            jvmArgs += listOf(
                "-Denv=release",
                "-Dbuild=${SimpleDateFormat("yyMMddHHmmss").format(Date())}",
                "-Dbuild_version=${version}"
            )
            proguard{
                isEnabled.set(true)
                configurationFiles.from(project.file("compose-desktop.pro"))
            }
        }

        nativeDistributions {
            modules("java.compiler", "java.instrument", "java.management", "jdk.unsupported")
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = appName
            packageVersion = appVersion

            windows {
                packageVersion = appVersion
                msiPackageVersion = appVersion
                exePackageVersion = appVersion
                iconFile.set(project.file("launcher/logo.ico"))
                menu = true
                shortcut = true
            }

            macOS {
                packageVersion = appVersion
                dmgPackageVersion = appVersion
                pkgPackageVersion = appVersion
                // 显示在菜单栏、“关于”菜单项、停靠栏等中的应用程序名称
                dockName = appName
                packageBuildVersion = appVersion
                dmgPackageBuildVersion = appVersion
                pkgPackageBuildVersion = appVersion
                iconFile.set(project.file("launcher/logo.icns"))
            }

            linux {
                installationPath = "/opt/easyadb"
                shortcut = true
                iconFile.set(project.file("launcher/logo.png"))
            }

        }
    }
}
