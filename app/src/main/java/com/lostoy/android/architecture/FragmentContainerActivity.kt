package com.lostoy.android.architecture

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

class FragmentContainerActivity : FragmentActivity() {

    companion object {

        var counter = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_container)

        replaceFragment(TestFragment.instance(counter++))

        findViewById<View>(R.id.openNewFragment).setOnClickListener {
            replaceFragment(TestFragment.instance(counter++))
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commitAllowingStateLoss()
    }

    override fun onDestroy() {
        super.onDestroy()
        counter = 0
    }
}