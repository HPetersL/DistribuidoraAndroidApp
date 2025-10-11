package com.example.midistribuidoraapp

/*esta clase recibe ka informacion completa de la API
* y la separa en tramos o routeleg*/
data class RoutesResponse(
    val routes: List<RouteLeg>
)
/*esta toma la routeleg que nos interesa y la expresa en metros*/
/*FASE 3: agregamos el valor para la polilinea del nuevo mapa*/
data class RouteLeg(
    val distanceMeters: Int,
    val polyline: EncodedPolyline?
)

data class EncodedPolyline(
    val encodedPolyline: String
)