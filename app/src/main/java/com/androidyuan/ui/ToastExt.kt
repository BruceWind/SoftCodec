package com.androidyuan.ui

import android.content.Context
import android.widget.Toast
import com.androidyuan.softcodec.R

fun Context.shortToast(str:String){
    Toast.makeText(
        this, str, Toast.LENGTH_SHORT
    ).show()
}
fun Context.longToast(str:String){
    Toast.makeText(
        this, str, Toast.LENGTH_SHORT
    ).show()
}