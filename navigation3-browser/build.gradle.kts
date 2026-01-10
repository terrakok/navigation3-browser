plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    js { browser() }
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.navigationevent)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

    }

}

//Publishing your Kotlin Multiplatform library to Maven Central
//https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-publish-libraries.html
mavenPublishing {
    publishToMavenCentral()
    coordinates("com.github.terrakok", "navigation3-browser", "0.3.1")

    pom {
        name = "navigation3-browser"
        description = "Kotlin Multiplatform library"
        url = "https://github.com/terrakok/navigation3-browser"

        licenses {
            license {
                name = "MIT"
                url = "https://opensource.org/licenses/MIT"
            }
        }

        developers {
            developer {
                id = "terrakok"
                name = "Konstantin Tskhovrebov"
                email = "terrakok@gmail.com"
            }
        }

        scm {
            url = "https://github.com/terrakok/navigation3-browser"
        }
    }
    if (project.hasProperty("signing.keyId")) signAllPublications()
}
