package com.example.cityguideapp.models

data class GoogleMapRoute (
    val snappedPoints : List<SnappedPoints>
)
data class SnappedPoints (

     val location : Location,
     val originalIndex : Int,
     val placeId : String
)
data class Location (

    val latitude : Double,
    val longitude : Double
)