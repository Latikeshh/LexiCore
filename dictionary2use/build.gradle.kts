plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
}

android {
    namespace = "com.dictionary.use"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation("androidx.annotation:annotation:1.9.1")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = "com.github.Latikeshh"
                artifactId = "dictionary2use"
                version = "2026.10.18"

                pom {
                    name.set("Dictionary2Use")
                    description.set("Lightweight, offline-first Android dictionary and spellchecking library.")
                    url.set("https://github.com/Latikeshh/dictionary2use")
                    licenses {
                        license {
                            name.set("The Apache Software License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("Latikeshh")
                            name.set("Latikesh")
                        }
                    }
                    scm {
                        connection.set("scm:git:github.com/Latikeshh/dictionary2use.git")
                        developerConnection.set("scm:git:ssh://github.com/Latikeshh/dictionary2use.git")
                        url.set("https://github.com/Latikeshh/dictionary2use")
                    }
                }
            }
        }
    }
}
