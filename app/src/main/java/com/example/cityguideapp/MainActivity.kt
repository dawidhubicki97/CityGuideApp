package com.example.cityguideapp

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Switch
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.cityguideapp.fragments.MyMapFragment
import com.example.cityguideapp.fragments.RoutesFragment
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener {


    private var drawerlayout:DrawerLayout?=null
    private var toolbar:Toolbar?=null
    private var drawerswitch:Switch?=null
    private var toggle:ActionBarDrawerToggle ?=null
    private var fragment:Fragment?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setPermission()
        setContentView(R.layout.activity_main)
        drawerlayout = findViewById(R.id.drawer)
        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        getSupportActionBar()!!.setDisplayShowTitleEnabled(false)

        toggle= ActionBarDrawerToggle(this,drawerlayout,toolbar,R.string.drawerOpen,R.string.drawerClose)
        drawerlayout!!.addDrawerListener(toggle!!)
        toggle!!.syncState()

        navigationView.setNavigationItemSelectedListener(this)
        navigationView.setCheckedItem(R.id.switch_item)
        navigationView.menu.performIdentifierAction(R.id.switch_item,0)


        fragment=supportFragmentManager.findFragmentByTag("myfragment")

        if(savedInstanceState==null)
            replaceFragment(MyMapFragment())
        }



    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
       fragment=supportFragmentManager.getFragment(savedInstanceState!!,"myfragment")
    }
    private fun setPermission(){
        val permission=Manifest.permission.ACCESS_FINE_LOCATION
        if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
            Log.d("permission","denied")

        }
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,permission)){
            Log.d("permission","granted")
        }
        else{
            makeRequest()
        }

    }
    private fun makeRequest(){
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            1->{
                if(grantResults.isEmpty()||grantResults[0]!=PackageManager.PERMISSION_DENIED)
                {
                    Log.d("persmission","permission denied by user")
                }
                else{
                    Log.d("persmission","permission granted by user")
                }
            }

        }
    }
    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        Log.d("klik","cos")
        when(p0.itemId){
            R.id.mapmenuitem ->{
                replaceFragment(MyMapFragment())
            }
            R.id.routes->{
                replaceFragment(RoutesFragment())
            }
            R.id.switch_item->{
                val mypreference=MyPreference(this)
                var followed=mypreference.getFollowUser()

                drawerswitch=p0.actionView.findViewById(R.id.followswitch)
                if(followed) drawerswitch?.isChecked=true
                else drawerswitch?.isChecked=false
                drawerswitch!!.setOnCheckedChangeListener { compoundButton, b ->
                    if(b)
                        mypreference.setFollowUser(true)
                    else
                        mypreference.setFollowUser(false)
                }
            }
        }
        return true
    }

    fun replaceFragment(fragment: Fragment,routeid:Int?=null) {
        this.fragment=fragment
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        if(routeid!=null){
           var bundle= Bundle()
            bundle.putInt("routeid",routeid)
            fragment.arguments=bundle

        }
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.commit()
    }
}





