import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

group = "com.xmbet"
version = "1.0.0"
val appName by extra("EasyADB")
val appVersion by extra("1.0.0")

repositories {
    mavenLocal()
    maven("https://maven.aliyun.com/repository/public/")
    maven("https://maven.aliyun.com/repository/central")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    mavenCentral()
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation("com.alibaba.fastjson2:fastjson2-kotlin:${properties["fastjson2.version"]}")
    implementation("com.android.tools.ddms:ddmlib:${properties["ddmlib.version"]}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${properties["kotlin.version"]}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${properties["kotlin.version"]}")
    implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:${properties["lifecycle.viewmodel.compose.version"]}")
    implementation(project(":ddmlib"))
    testImplementation(kotlin("test"))
}

compose.desktop {
    application {
        mainClass = "MainKt"

        buildTypes.release.proguard {
            isEnabled.set(true)
            configurationFiles.from(project.file("compose-desktop.pro"))
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
            }
        }
    }
}
