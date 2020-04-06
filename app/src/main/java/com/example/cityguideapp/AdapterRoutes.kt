package com.example.cityguideapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.cityguideapp.fragments.MyMapFragment
import com.example.cityguideapp.models.RouteItem

class AdapterRoutes(): PagerAdapter() {
    private var items: List<RouteItem> = ArrayList()


constructor(items:List<RouteItem>) : this() {
    this.items=items
}
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view.equals(`object`)
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
       val layoutinflater=LayoutInflater.from(container.context)
        val view=layoutinflater.inflate(R.layout.layout_route_list_item,container,false)
        var imageivew=view.findViewById<ImageView>(R.id.imageViewCard)
        var titleview=view.findViewById<TextView>(R.id.titleViewCard)
        var descview=view.findViewById<TextView>(R.id.descriptionCardView)
        val requestOptions= RequestOptions().placeholder(R.drawable.ic_launcher_background).error(R.drawable.ic_launcher_background).override(400,300).centerCrop()
        Glide.with(view.context).applyDefaultRequestOptions(requestOptions).load(items.get(position).image).into(imageivew)
        titleview.text=items.get(position).name
        descview.text=items.get(position).description
        container.addView(view,0)
        view.setOnClickListener {
            val zmienna=it.context as MainActivity
            zmienna.replaceFragment(MyMapFragment(),items.get(position).id)
        }

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

}