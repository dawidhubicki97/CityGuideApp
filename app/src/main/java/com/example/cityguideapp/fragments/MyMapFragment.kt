package com.example.cityguideapp.fragments

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.*
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.cityguideapp.*
import com.example.cityguideapp.R
import com.example.cityguideapp.models.*
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.Geofence.NEVER_EXPIRE
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.maps.android.PolyUtil
import kotlinx.android.synthetic.main.fragment_map.*
import okhttp3.*
import java.lang.Exception
import kotlin.math.roundToInt

class MyMapFragment: Fragment(),OnMapReadyCallback {




    var googleMap: GoogleMap?=null
    var points: MutableList<LatLng> = ArrayList()
    var offRoutePoints: MutableList<LatLng> = ArrayList()
    var routePoints: MutableList<LatLng> = ArrayList()
    var places: MutableList<Place> = ArrayList()
    private var trafficTime:Int?=null
    private var markers:MutableList<Marker> = ArrayList()
    private var routeLine: Polyline?=null
    private var line: Polyline?=null
    private var offRouteLine: Polyline?=null
    var geofenceList:MutableList<Geofence> = ArrayList()
    lateinit var geofencingClient: GeofencingClient
    lateinit var googleReciever:BroadcastReceiver

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(activity, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_map, container, false)
        val mapview=root!!.findViewById<MapView>(R.id.map)
        mapview.onCreate(savedInstanceState)
        mapview.onResume()
        mapview.getMapAsync(this)
        geofencingClient = LocationServices.getGeofencingClient(activity!!.applicationContext)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val routeid=arguments?.getInt("routeid")
        retainInstance=true

        if(routeid!=null && routePoints.isEmpty()){
            fetchPointsFromDatabase(routeid)
        }
        if(routeid==null){
            progressBarMap.visibility=View.INVISIBLE
        }

    }

    override fun onResume() {
        super.onResume()
        googleReciever=GoogleReciever()
        var broadcastmanager=LocalBroadcastManager.getInstance(context!!)
        broadcastmanager.registerReceiver(googleReciever, IntentFilter("googlegeofence"))
    }

    override fun onPause() {
        super.onPause()
        var broadcastmanager=LocalBroadcastManager.getInstance(context!!)
        broadcastmanager.unregisterReceiver(googleReciever)
    }

    fun finishRoute(){
        googleMap?.clear()
        if(activity!=null) {
            (activity as MainActivity).replaceFragment(EndRouteFragment())
        }
    }

    inner class GoogleReciever:BroadcastReceiver(){

        override fun onReceive(p0: Context?, p1: Intent?) {

            val name= p1?.getStringExtra("geofencename")
            val action=p1?.getIntExtra("action",3)
            if(name.equals("finish")&& action==1){
                finishRoute()
                return
            }
            for (i in places){
                if(i.name==name && action==1){
                    val markeroptions=MarkerOptions().position(LatLng(i.latitude,i.longitude)).title(i.name).icon(BitmapDescriptorFactory.fromResource(R.drawable.star))
                    val marker=googleMap?.addMarker(markeroptions)
                    marker?.showInfoWindow()
                    markers.add(marker!!)
                }
                if(i.name==name && action==0){
                    markers.get(0).remove()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(p0: GoogleMap?) {
        googleMap= p0!!
        googleMap!!.isMyLocationEnabled=true
        if(routePoints.isEmpty())
            reverseButton.visibility=View.INVISIBLE

        if(routePoints.isNotEmpty()){
            showTrafficTime()
            drawRoutePolyline()
            reverseButton.visibility=View.VISIBLE
            if(geofenceList.isEmpty())
                populateGeofenceList()

            addGeoFencingToMap()
            googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(routePoints.get(0), 16f))
        }
        if(offRoutePoints.isNotEmpty()){
            drawOffRouteLine()
        }
        googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(50.037434, 22.004240) ,14f) )
        reverseButton.setOnClickListener {
            reverseRoute()
        }


    }
    private fun reverseRoute(){
        routePoints.reverse()
        googleMap?.clear()
        drawRoutePolyline()
        Toast.makeText(activity,"Trasa została odwrócona",Toast.LENGTH_SHORT).show()

    }

    private fun populateGeofenceList(){
        geofenceList.clear()

        for(i in places) {
            geofenceList.add(
                Geofence.Builder()
                    .setRequestId(i.name)
                    .setCircularRegion(
                        i.latitude,
                        i.longitude,
                        40f
                    )
                    .setExpirationDuration(NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build()
            )
        }
    }
    @SuppressLint("MissingPermission")
    private fun addGeoFencingToMap(){

        geofencingClient.addGeofences(getGeofencingRequest(), geofencePendingIntent)?.run {
            addOnSuccessListener {

            }
            addOnFailureListener {

            }
        }
    }

    private fun getGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList)
        }.build()
    }

    private fun showTrafficTime(){
        if(trafficTextView!=null)
            trafficTextView.text="Przewidywany czas w korku: "+trafficTime.toString()+"min"
    }
    private fun getTimeInTrafficUrl():String{
        var path="https://maps.googleapis.com/maps/api/directions/json?origin="
        path=path+routePoints.get(0).latitude.toString()+","+routePoints.get(0).longitude.toString()
        path=path+"&destination="
        path=path+routePoints.get(routePoints.size-1).latitude.toString()+","+routePoints.get(routePoints.size-1).longitude.toString()+"&waypoints="
        for (i in 0 until routePoints.size-1)
        {
            path=path+"via:"+routePoints.get(0).latitude.toString()+","+routePoints.get(0).longitude.toString()
            if(i+1<routePoints.size-1){
                path=path+"|"
            }
        }
        path=path+"&traffic_model=best_guess&departure_time=now&key="+resources.getString(R.string.my_google_api_key)+""
        return path
    }

    private fun fetchPointsFromDatabase(routeid:Int){
        var databaseref = FirebaseDatabase.getInstance().getReference("Routes/$routeid/points")
        databaseref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    val singleroutepoint=it.getValue(Point::class.java)
                    routePoints.add(LatLng(singleroutepoint!!.latitude,singleroutepoint.longitude))
                }
                val URL=getTimeInTrafficUrl()
                GetTrafficTime(URL).execute()
                googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(routePoints.get(0), 16f))

            }

        })
        databaseref=FirebaseDatabase.getInstance().getReference("Routes/$routeid/places")
        databaseref.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    val singleplace=it.getValue(Place::class.java)
                    if (singleplace != null) {
                        places.add(singleplace)
                    }

                }
                populateGeofenceList()
                addGeoFencingToMap()
            }
        })
    }

    private fun showDistance(location:LatLng){
        if(progressBarMap!=null)
            progressBarMap.visibility=View.INVISIBLE

        var distance=PolyUtil.distanceToLine(location,routePoints.get(0),routePoints.get(1))
        for (i in 0 until routePoints.size)
        {
            if(i+1<routePoints.size && PolyUtil.distanceToLine(location,routePoints.get(i),routePoints.get(i+1))<distance){
                distance=PolyUtil.distanceToLine(location,routePoints.get(i),routePoints.get(i+1))
            }
        }

        if(distanceTextView!=null) {
            if (distance > 20)
                distanceTextView.text = "Odleglosc od trasy: " + distance.roundToInt().toString()+"m"
            else
                distanceTextView.text = "Jestes na trasie"
        }
    }

    fun drawRoutePolyline() {
        if(routePoints.isNullOrEmpty()==false) {
            val options = PolylineOptions().width(5f).color(Color.BLUE).geodesic(true)

            for (i in 0 until routePoints.size) {
                val point = routePoints.get(i)
                options.add(point)
            }
            googleMap?.addMarker(
                MarkerOptions().position(
                    LatLng(
                        routePoints.get(0).latitude,
                        routePoints.get(0).longitude
                    )
                ).title("Początek").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )
            googleMap?.addMarker(
                MarkerOptions().position(
                    LatLng(
                        routePoints.get(routePoints.size - 1).latitude,
                        routePoints.get(routePoints.size - 1).longitude
                    )
                ).title("Koniec").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
            routeLine = googleMap!!.addPolyline(options)
        }
    }

    fun drawOffRouteLine(current:LatLng?=null) {
        offRouteLine?.remove()
        val options = PolylineOptions().width(5f).color(Color.BLUE).geodesic(true)
        if (current != null) {
            offRoutePoints.add(current)
        }
        for (i in offRoutePoints)
            options.add(i)
        offRouteLine = googleMap!!.addPolyline(options)
    }

    private fun redrawLine(current:LatLng) {
        line?.remove()
        line?.points?.clear()
        val options = PolylineOptions().width(10f).color(Color.RED).geodesic(true)
        val results=FloatArray(10)
        Location.distanceBetween(routePoints.get(0).latitude,routePoints.get(0).longitude,current.latitude,current.longitude,results)
        var smallest=results[0]
        var place=0
        for (i in 0 until routePoints.size)
        {
            Location.distanceBetween(routePoints.get(i).latitude,routePoints.get(i).longitude,current.latitude,current.longitude,results)
            if(results[0]<smallest){
                smallest=results[0]
                place=i
            }
        }

        for (i in 0 until place)
        {

            val point = routePoints.get(i)
            options.add(point)

        }
        options.add(current)
        line = googleMap!!.addPolyline(options)
    }

    fun showReverseButton(){
        reverseButton.visibility=View.VISIBLE
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(){
        var locationGps : Location?=null
        var locationNetwork : Location?=null
        var hasGps=false
        var hasNetwork=false
        val locationmanager=activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps=locationmanager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork=locationmanager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        val mypreference=MyPreference(activity!!)
        var followed=mypreference.getFollowUser()
        if(hasGps||hasNetwork){
            if(hasGps){
                locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,0F,object: LocationListener {
                    override fun onLocationChanged(location: Location?) {

                        if (location != null) {
                            locationGps = location
                            val temp=LatLng(locationGps!!.latitude,locationGps!!.longitude)
                            if (routePoints.isNullOrEmpty()==false && PolyUtil.isLocationOnPath(
                                    temp,
                                    routeLine?.points,
                                    false,
                                    20.0
                                )
                            ) {
                                points.add(temp)
                                redrawLine(temp)
                            }
                            if (routePoints.isNotEmpty()) {
                                showDistance(temp)
                            }
                            followed=mypreference.getFollowUser()
                            if(followed){
                                drawOffRouteLine(temp)
                            }
                            else offRouteLine?.remove()
                            Log.d("CodeAndroidLocation","GPS Latitude:"+locationGps!!.latitude)
                            Log.d("CodeAndroidLocation","GPS Longitude:"+locationGps!!.longitude)
                        }
                    }

                    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onProviderEnabled(p0: String?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onProviderDisabled(p0: String?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                })
                val localGpsLocation=locationmanager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if(localGpsLocation!=null){
                    locationGps=localGpsLocation
                }
                if(hasNetwork){
                    Log.d("CodeAndroidLocation","hasGps")
                    locationmanager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,5000,0F,object:
                        LocationListener {
                        override fun onLocationChanged(location: Location?) {
                            if(location!=null){
                                locationNetwork=location
                                val temp=LatLng(locationNetwork!!.latitude,locationNetwork!!.longitude)
                                if (routePoints!=null && PolyUtil.isLocationOnPath(
                                        temp,
                                        routeLine?.points,
                                        false,
                                        20.0
                                    )
                                ) {
                                    points.add(temp)
                                    redrawLine(temp)
                                }
                                if (routePoints.isNotEmpty()&&temp!=null) {
                                    showDistance(temp)
                                }
                                followed=mypreference.getFollowUser()
                                if(followed){
                                    drawOffRouteLine(temp)
                                }
                                else offRouteLine?.remove()
                                Log.d("CodeAndroidLocation","Network Latitude:"+locationNetwork!!.latitude)
                                Log.d("CodeAndroidLocation","Network Latitude:"+locationNetwork!!.longitude)
                            }
                        }

                        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
                        }

                        override fun onProviderEnabled(p0: String?) {
                        }

                        override fun onProviderDisabled(p0: String?) {
                        }

                    })
                    val localNewtorkLocation=locationmanager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    if(localNewtorkLocation!=null){
                        locationNetwork=localNewtorkLocation
                    }
                    if(locationGps!=null && locationNetwork!=null){
                        if(locationGps!!.accuracy>locationNetwork!!.accuracy){
                            Log.d("CodeAndroidLocation","Network Latitude:"+locationNetwork!!.latitude)
                            Log.d("CodeAndroidLocation","Network Latitude:"+locationNetwork!!.longitude)

                        }
                        else{
                            Log.d("CodeAndroidLocation","GPS Latitude:"+locationGps!!.latitude)
                            Log.d("CodeAndroidLocation","GPS Latitude:"+locationGps!!.longitude)
                        }
                    }
                }
            }
        }
        else{
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    fun getRoutePoints(){
        var pointstostring=""
        for (i in 0 until routePoints.size){
            pointstostring=pointstostring+routePoints.get(i).latitude.toString()+","+routePoints.get(i).longitude
            if(i+1<routePoints.size)
                pointstostring=pointstostring+"|"
        }

        routePoints.clear()
        GetRoutePolyline("https://roads.googleapis.com/v1/snapToRoads?path=$pointstostring&interpolate=true&key="+resources.getString(R.string.my_google_api_key)+"").execute()

    }
    inner class GetRoutePolyline(val url: String) : AsyncTask<Void, Void, MutableList<LatLng>>() {


        override fun doInBackground(vararg p0: Void?): MutableList<LatLng> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body!!.string()

            try {
                val resObj = Gson().fromJson(data, GoogleMapRoute::class.java)
                for (i in 0..(resObj.snappedPoints.size - 1)) {
                    routePoints.add(
                        LatLng(
                            resObj.snappedPoints[i].location.latitude,
                            resObj.snappedPoints[i].location.longitude
                        )
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
            return routePoints
        }

        override fun onPostExecute(routepoints: MutableList<LatLng>?) {
            drawRoutePolyline()
            getLocation()
            showReverseButton()

        }


    }
    inner class GetTrafficTime(val url: String) : AsyncTask<Void, Void, Int>() {


        override fun doInBackground(vararg p0: Void?): Int {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body!!.string()
            var traffictime=0
            try {
                val resObj = Gson().fromJson(data, GoogleMapTraffic::class.java)
                traffictime=resObj.routes.get(0).legs.get(0).duration_in_traffic.value
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return traffictime/60
        }

        override fun onPostExecute(trafficTimetemp:Int) {
            if(trafficTimetemp!=null) {
                trafficTime=trafficTimetemp
                showTrafficTime()
            }
            getRoutePoints()
        }
    }
}
