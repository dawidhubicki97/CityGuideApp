package com.example.cityguideapp.models

data class RouteItem(var description:String,
                     var name:String,var image:String,var id:Int) {
    constructor():this("","","",id=0)


}