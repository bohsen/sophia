import org.jetbrains.compose.desktop.application.dsl.TargetFormat

buildscript {
    dependencies {
        classpath(libs.molecule.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinx.serialization)
}

group = "dk.rn.plugins"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation(compose.materialIconsExtended)
    implementation(libs.coroutines.swing)
    implementation(libs.okio)
    implementation(libs.molecule.runtime)
    implementation(libs.bundles.log4j2)
    implementation(libs.log4j2.kotlin)
    implementation(libs.bundles.multiplatform.settings)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.pdfbox)
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.multiplatform.settings.test)
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "sophia"
            packageVersion = "1.0.0"
            description = "Automatically upload sequencer results to SOPHiA Genetics Cloud analytics service"
            copyright = "© 2023 Region Nordjylland, Denmark. All rights reserved."
            vendor = "Thomas Bohsen Schmidt"

            windows {
                menu = true
                // see https://wixtoolset.org/documentation/manual/v3/howtos/general/generate_guids.html
                upgradeUuid = "61DAB35E-17CB-43B0-81D5-B30E1C0830FA"
            }
        }
    }
}
