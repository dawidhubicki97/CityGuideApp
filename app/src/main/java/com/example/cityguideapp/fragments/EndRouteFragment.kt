package com.example.cityguideapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.cityguideapp.MainActivity
import com.example.cityguideapp.R
import kotlinx.android.synthetic.main.fragment_end_route.*

class EndRouteFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_end_route, container, false)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        buttonMap.setOnClickListener {
            (activity as MainActivity).replaceFragment(MyMapFragment())
        }
        buttonRoutes.setOnClickListener {
            (activity as MainActivity).replaceFragment(RoutesFragment())
        }
    }
}