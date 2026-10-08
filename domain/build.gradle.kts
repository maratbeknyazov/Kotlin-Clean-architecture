plugins {
    id("org.jetbrains.kotlin.jvm")
}

// Здесь только Kotlin. У domain нет доступа к Android и к модулю app.
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
