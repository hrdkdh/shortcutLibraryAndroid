package com.example.shortcutlibrary

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout


class MainActivity : AppCompatActivity() {

    var tabLayout: TabLayout? = null
    var viewPager: ViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        viewPager = findViewById<ViewPager>(R.id.viewPager)

        val adapter = MyAdapter(this, supportFragmentManager, tabLayout!!.tabCount)
        viewPager!!.adapter = adapter

        viewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        //첫번째 탭은 이미 선택되어 있으므로 아이콘 색 하얗게~
        tabLayout!!.getTabAt(0)!!.icon!!.setTint(ResourcesCompat.getColor(resources, R.color.colorTabIconSelected, null))

        tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                //탭 아이콘 색 바꾸기_selected
                tab.icon!!.setTint(ResourcesCompat.getColor(resources, R.color.colorTabIconSelected, null))
                
                //탭 포지션 넘버로 뷰페이저 연결해서 띄워줌
                viewPager!!.currentItem = tab.position
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {
                //탭 아이콘 색 바꾸기_orginal
                tab.icon!!.setTint(ResourcesCompat.getColor(resources, R.color.colorTabIcon, null))
            }
            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })
    }
}