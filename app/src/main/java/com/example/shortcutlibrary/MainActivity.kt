package com.example.shortcutlibrary

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.kotlin.where


open class Shortcut(
    @PrimaryKey
    var pk : Int = 0,
    var category : String = "",
    var category_hangul : String = "",
    var ctrl : String = "",
    var alt : String = "",
    var shift : String = "",
    var key1 : String = "",
    var key2 : String = "",
    var key3 : String = "",
    var key4 : String = "",
    var commandString : String = "",
    var searchString : String = "",
    var score : Int = 0,
    var favorite : Int = 0,
    var commandKeyStr : String = ""
) : RealmObject()

//open class Setting : RealmObject() {
//    //아래에 변수가 추가될 경우 setting 클래스에서도 변수 리스트에 추가해 줘야 함!!!
//    var powerpoint : Boolean = true,
//    var excel : Boolean = true,
//    var word : Boolean = true,
//    var hangul : Boolean = true,
//    var chrome : Boolean = true,
//    var windows : Boolean = true
//}

class MainActivity : AppCompatActivity() {

    var tabLayout: TabLayout? = null
    var viewPager: ViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Realm.init(this)
        val realm= Realm.getDefaultInstance()
        val shortcut = Shortcut()
        shortcut.pk = 1
        shortcut.category = "powerpoint"
        shortcut.category_hangul = "파워포인트"
        shortcut.ctrl = ""
        shortcut.alt = ""
        shortcut.shift = ""
        shortcut.key1 = ""
        shortcut.key2 = ""
        shortcut.key3 = ""
        shortcut.key4 = ""
        shortcut.commandString = "test"
        shortcut.searchString = ""
        shortcut.score = 0
        shortcut.favorite = 0
        shortcut.commandKeyStr = ""
        realm.executeTransaction { realm ->
            realm.copyToRealmOrUpdate(shortcut)
        }

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