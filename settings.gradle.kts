pluginManagement {
    repositories {
        google() // Agregamos google directamente para evitar restricciones innecesarias
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Tarea1_MartinezPerezJavier"
include(":app")
