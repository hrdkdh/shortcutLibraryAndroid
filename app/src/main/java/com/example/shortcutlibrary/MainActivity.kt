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
import java.io.*


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

        getCsvFile()

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

    fun getCsvFile() {
        var fileReader: BufferedReader? = null

        try {
            val shortcut_from_csv_array = ArrayList<Array<Any>>()
            var line: String?

            val inputStream: InputStream = resources.openRawResource(R.raw.org_db_200407)
            fileReader = BufferedReader(InputStreamReader(inputStream));

            // Read the file line by line starting from the second line
            line = fileReader.readLine()
            while (line != null) {
                val tokens = line.split(",")
                if (tokens.isNotEmpty()) {
                    if (tokens[0] != "pk") {
                        var commandKeyStr = ""
                        for (i in 3..9) {
                            if (tokens[i] != "") {
                                if (commandKeyStr =="") {
                                    commandKeyStr = tokens[i]
                                } else {
                                    commandKeyStr = commandKeyStr + "+" + tokens[i]
                                }
                            }
                        }
                        val shortcut : Array<Any> = arrayOf(
                            tokens[0].toInt(),
                            tokens[1],
                            tokens[2],
                            tokens[3],
                            tokens[4],
                            tokens[5],
                            tokens[6],
                            tokens[7],
                            tokens[8],
                            tokens[9],
                            tokens[10],
                            tokens[11],
                            tokens[12].toInt(),
                            tokens[13].toInt(),
                            commandKeyStr
                        )
                        shortcut_from_csv_array.add(shortcut)
                    }
                    line = fileReader.readLine()
                }
            }
            setShortcutDB(shortcut_from_csv_array)
        } catch (e: Exception) {
            Log.d("error", "Reading CSV Error!")
            e.printStackTrace()
        } finally {
            try {
                fileReader?.close()
            } catch (e: IOException) {
                Log.d("error", "Closing fileReader Error!")
                e.printStackTrace()
            }
        }
    }

    fun setShortcutDB(dataArray : ArrayList<Array<Any>>) {
        for (items in dataArray) {
            for (item in items) {
                Log.d("item", item.toString())
            }
        }
        Realm.init(this)
        val realm= Realm.getDefaultInstance()
        val shortcut = Shortcut()
//        shortcut.pk = 1
//        shortcut.category = "powerpoint"
//        shortcut.category_hangul = "파워포인트"
//        shortcut.ctrl = ""
//        shortcut.alt = ""
//        shortcut.shift = ""
//        shortcut.key1 = ""
//        shortcut.key2 = ""
//        shortcut.key3 = ""
//        shortcut.key4 = ""
//        shortcut.commandString = "test"
//        shortcut.searchString = ""
//        shortcut.score = 0
//        shortcut.favorite = 0
//        shortcut.commandKeyStr = ""
//        realm.executeTransaction { realm ->
//            realm.copyToRealmOrUpdate(shortcut)
//        }
    }
}