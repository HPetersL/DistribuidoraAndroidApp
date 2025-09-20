package com.example.midistribuidoraapp
/*usamos math con * para importar todas las funciones math de kotlin
* a la vez y no tener que estar escribiendo cada una en el codigo*/
import kotlin.math.*

object LocationUtils {
    /*radio total de la tierra en kilometros*/
    private const val EARTH_RADIUS_KM = 6371.0

    /*declaracion de variables a utilizar*/
    fun calculateHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        // convertir grados a radianes
        val lat1Rad = Math.toRadians(lat1)/*latitud punto 1*/
        val lon1Rad = Math.toRadians(lon1)/*longitud del punto 1*/
        val lat2Rad = Math.toRadians(lat2)/*latitud del punto 2*/
        val lon2Rad = Math.toRadians(lon2)/*longitu del punto 2*/

        // diferencias de latitud y longitud
        val dLat = lat2Rad - lat1Rad
        val dLon = lon2Rad - lon1Rad

        // fórmula Haversine completa
        val a = sin(dLat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(dLon / 2).pow(2)

        // c = 2 · atan2(√a, √(1−a))
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        // d = R · c
        return EARTH_RADIUS_KM * c
    }
}