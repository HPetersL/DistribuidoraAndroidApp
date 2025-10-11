package com.example.midistribuidoraapp
/*usamos retrofit para consumir la API Rest*/
import retrofit2.Retrofit
/*usamos converter para pasar el Json a Kotlin*/
import retrofit2.converter.gson.GsonConverterFactory
/*usamos anotacion para definir la solicitud http*/
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

/*en esta clase definimos el cuerpo de la peticion
* POST donde incluimos las coordenadas de origen y
* destino, el modo de transporte*/
data class RouteRequest(
    val origin: Waypoint,
    val destination: Waypoint,
    val travelMode: String = "DRIVE"
)
data class Waypoint(
    val location: Location
)
/*cambia la coordenada a un objeto location necesario
* para la nueva API*/
data class Location(
    val latLng: LatLng
)

/*definimos origen o destino como coordenada*/
data class LatLng(
    val latitude: Double,
    val longitude: Double
)

/*esto define como se hace la solicitud POST */
interface GoogleMapsApiService {
    @Headers("Content-Type: application/json") /*el cuerpo debe ser json*/
    @POST("directions/v2:computeRoutes")
    suspend fun computeRoutes(
        @Header("X-Goog-Api-Key") apiKey: String, /*mi key de api de google cloud*/

        /* FASE 4: Ahora pedimos la distancia Y la polilínea */
        @Header("X-Goog-FieldMask") fieldMask: String = "routes.distanceMeters,routes.polyline.encodedPolyline",

        @Body requestBody: RouteRequest /*envia el objeto como json*/
    ): RoutesResponse /*espera una respuesta*/
}

/*nuevamente definimos un singletone para que no se duplique retrofit
* incluimos la URL nueva de la API routes*/
object RetrofitClient {
    private const val BASE_URL = "https://routes.googleapis.com/"

    /*transformamos el json a kotlin usando gson*/
    val instance: GoogleMapsApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(GoogleMapsApiService::class.java)
    }
}