
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
        /*ID de la unico para uso en playstore*/
        applicationId = "com.example.midistribuidoraapp"
        /*version minima de android soportada*/
        minSdk = 23
        /*version objetivo para optimizacion*/
        targetSdk = 36
        /*codigo interno 1° version*/
        versionCode = 1
        /*nombre visible de la version*/
        versionName = "1.0"
        /*runner para pruebas*/
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
        /*compatibilidad con java 8*/
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        /*define version de JVM para kotlin*/
        jvmTarget = "1.8"
    }
    /*es buena practica segun Android usar ViewBindin para
    * aumentar la seguridad UI evitando asi el uso de findViewById*/
    buildFeatures {
        viewBinding = true
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
}