package com.example.cityguideapp.models

data class Point(var latitude:Double,
                     var longitude:Double) {
    constructor():this(0.0,0.0)


}


data class Place(var latitude:Double,
                 var longitude:Double
,var name:String) {
    constructor():this(0.0,0.0,"")


}