plugins {
    alias(libs.plugins.android.application) // Este ya incluye a com.android.application
    alias(libs.plugins.kotlin.android)      // Este ya incluye a org.jetbrains.kotlin.android
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")

    // Solo agrega el de Google Services porque este NO está en tus "alias"
    id("com.google.gms.google-services")
}
android {
    namespace = "com.starcode.locus"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.starcode.locus"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }

}

dependencies {
    // --- NÚCLEO DE ANDROID Y COMPOSE ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.ui)
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.compose.material:material-icons-extended")

    // --- FIREBASE (CORREGIDO Y UNIFICADO) ---
    // Usamos el BOM 33.1.2 que es compatible con la mayoría de proyectos de clase
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")

    // --- BASE DE DATOS LOCAL (ROOM) ---
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version")

    // --- RED Y API (RETROFIT Y OKHTTP) ---
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // --- MAPAS, IMÁGENES Y ANIMACIONES ---
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    implementation("com.airbnb.android:lottie-compose:6.1.0")
    implementation("io.coil-kt:coil-compose:2.6.0")

    // --- PRUEBAS (UNIT Y ANDROID) ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}