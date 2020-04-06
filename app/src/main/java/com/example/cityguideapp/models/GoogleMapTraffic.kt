package com.example.cityguideapp.models
data class GoogleMapTraffic(
    val geocoded_waypoints : List<Geocoded_waypoints>,
    val routes : List<Routes>,
    val status:String
)
data class Geocoded_waypoints (

    val geocoder_status : String,
    val place_id : String,
    val types : List<String>
)
data class Routes (

    val legs : List<Legs>
)

data class Legs (
    val duration_in_traffic : Duration_in_traffic

)
data class Duration_in_traffic (

    val text : String,
    val value : Int
)