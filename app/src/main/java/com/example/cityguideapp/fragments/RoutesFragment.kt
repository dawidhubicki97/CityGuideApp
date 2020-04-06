package com.example.cityguideapp.fragments

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.example.cityguideapp.AdapterRoutes
import com.example.cityguideapp.R
import com.example.cityguideapp.models.RouteItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class RoutesFragment: Fragment() {
    internal var root: View? = null
    private lateinit var routeAdapter:AdapterRoutes
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.fragment_routes, container, false)
        loadRoutes()
        return root
    }
    fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    fun loadRoutes() {

        if (isOnline(context!!)) {
            val viewpager = root!!.findViewById(R.id.viewPager) as ViewPager
            var list = ArrayList<RouteItem>()
            var databaseref = FirebaseDatabase.getInstance().getReference("Routes/")
            databaseref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    p0.children.forEach {
                        val routeit = it.getValue(RouteItem::class.java)
                        list.add(routeit!!)


                    }
                    routeAdapter = AdapterRoutes(list)
                    viewpager.adapter = routeAdapter
                }

            })
        }
        else
        {
            val toast = Toast.makeText(context, "Nie można połączyć z serwerem", Toast.LENGTH_LONG).show()
            val offlinetextview=root!!.findViewById(R.id.offlineTextView) as TextView
            offlinetextview.visibility=View.VISIBLE

        }
    }
}