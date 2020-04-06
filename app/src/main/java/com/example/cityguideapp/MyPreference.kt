package com.example.cityguideapp

import android.content.Context

class MyPreference(context: Context) {
    val preferenceName="PreferenceName"
    val preferenceFollowUser="PreferenceFollowUser"
    val preference=context.getSharedPreferences(preferenceName,Context.MODE_PRIVATE)


    fun getFollowUser():Boolean{
        return preference.getBoolean(preferenceFollowUser,false)
    }
    fun setFollowUser(follow:Boolean){
        val editor=preference.edit()
        editor.putBoolean(preferenceFollowUser,follow)
        editor.apply()

    }

}