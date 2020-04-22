package com.hrdkdh.shortcutlibrary

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

//뷰페이저 연결해 주는 어뎁터 설정
class MyAdapter(private val myContext: Context, fm: FragmentManager, internal var totalTabs: Int) : FragmentPagerAdapter(fm) {

    // this is for fragment tabs
    override fun getItem(position: Int): Fragment? {
        when (position) {
            0 -> {
                return Search()
            }
            1 -> {
                return Quiz()
            }
            2 -> {
                return MyFavorite()
            }
            3 -> {
                return Setting()
            }
            else -> return null
        }
    }

    // this counts total number of tabs
    override fun getCount(): Int {
        return totalTabs
    }
}