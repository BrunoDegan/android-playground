import com.android.build.api.dsl.ApplicationExtension

plugins {
    id("build.logic")
}

extensions.configure<ApplicationExtension> {
    namespace = "com.brunodegan.androidplayground"
}

dependencies {
    // AndroidX dependencies
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.kotlinx.immutable.list)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.window.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.ui)

    // Coil dependencies
    implementation(libs.coil.kt)
    implementation(libs.coil.kt.compose)
    implementation(libs.coil.kt.svg)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3.android)

    // Test dependencies
    testImplementation(libs.mockk)
    testImplementation(libs.androidx.work.testing)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlinx.flow.test.tubine)
    testImplementation(testFixtures(project))

    testFixturesApi(libs.junit)
    testFixturesApi(libs.kotlinx.coroutines.test)
    testFixturesApi(libs.kotlinx.immutable.list)
    testFixturesImplementation(libs.gson)
    testFixturesImplementation(libs.kotlinx.serialization.json)
    testFixturesImplementation(platform(libs.androidx.compose.bom))
    testFixturesImplementation(libs.androidx.ui)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.uiautomator)

    // UI Test
    androidTestImplementation(project.dependencies.platform(libs.koin.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(testFixtures(project))
    androidTestImplementation(libs.androidx.work.testing)

    // Ktor
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.gson)

    // Navigation 3
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.navigation3.ui.android)

    // Koin
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.test)
    implementation(libs.koin.annotations)
    implementation(libs.koin.compose)
    implementation(libs.koin.androidx.workmanager)

    // WorkManager
    implementation(libs.androidx.work.runtime)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.koin.compose.viewmodel.navigation)

    // Room
    ksp(libs.room.compiler)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
}
