package com.example.cityguideapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            return
        }
        val geofenceTransition = geofencingEvent.geofenceTransition
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            geofencingEvent.triggeringGeofences.forEach {
                val intent=Intent("googlegeofence")
                intent.putExtra("geofencename",it.requestId)
                intent.putExtra("action",1)
                LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
            }
        }
        if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT){
            geofencingEvent.triggeringGeofences.forEach {
                val intent=Intent("googlegeofence")
                intent.putExtra("geofencename",it.requestId)
                intent.putExtra("action",0)
                LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
            }
        }
        else {


        }
    }
}