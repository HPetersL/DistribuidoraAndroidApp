package com.example.midistribuidoraapp

/*esta clase recibe ka informacion completa de la API
* y la separa en tramos o routeleg*/
data class RoutesResponse(
    val routes: List<RouteLeg>
)
/*esta toma la routeleg que nos interesa y la expresa en metros*/
data class RouteLeg(
    val distanceMeters: Int
)