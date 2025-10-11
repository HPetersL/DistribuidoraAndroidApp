
import java.util.Properties
import java.io.FileInputStream

/*plugins a utilizar: */
plugins {
    /*plugin para aplicaciones android*/
    alias(libs.plugins.android.application)
    /*plugin para soporte de Kotlin en android*/
    alias(libs.plugins.kotlin.android)
    /*plugin para integracion de servicios de google en este caso Firebase*/
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.midistribuidoraapp" // nombre del paquete de la app
    compileSdk = 36 /*version del SDK para compilar*/

    defaultConfig {
        applicationId = "com.example.midistribuidoraapp"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Cargar API Key desde local.properties
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
        }

        val mapsApiKey = localProperties.getProperty("MAPS_API_KEY") ?: ""

        // Inyectar en el Manifest
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey

        // Opcional: También como BuildConfig si lo necesitas en código
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    /*librerias basicas de android*/
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    /*servicios de ubicacion de google*/
    implementation("com.google.android.gms:play-services-location:21.3.0")

    /*firebase BOM para gestion de versiones*/
    implementation(platform(libs.firebase.bom))
    /*autenticacion de firebase*/
    implementation(libs.firebase.auth)

    /*login de google y credencial manager*/
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    /*dependencias para relizar tests*/
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    /*dependencia de firestore*/
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)

    /*dependencias de retrofit*/
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)

    /*dependencias de maps*/
    implementation(libs.play.services.maps)
    implementation(libs.android.maps.utils)
}