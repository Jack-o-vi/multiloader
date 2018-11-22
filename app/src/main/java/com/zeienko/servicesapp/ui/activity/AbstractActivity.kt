package com.zeienko.servicesapp.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.zeienko.servicesapp.R

abstract class AbstractActivity : AppCompatActivity(){

    protected abstract fun createFragment(): Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val fm: FragmentManager = supportFragmentManager
        var fragment = fm.findFragmentById(R.id.flContainer)
        if (fragment == null) {
            fragment = createFragment()
            fm.beginTransaction()
                .add(R.id.flContainer, fragment)
                .commit()
        }
    }
}