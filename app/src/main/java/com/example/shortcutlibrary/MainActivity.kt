package com.example.shortcutlibrary

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption


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

open class FilterSetting(
    //아래에 변수가 추가될 경우 setting 클래스에서도 변수 리스트에 추가해 줘야 함!!!
    @PrimaryKey
    var pk : Int = 0,
    var powerpoint : Boolean = true,
    var excel : Boolean = true,
    var word : Boolean = true,
    var hangul : Boolean = true,
    var chrome : Boolean = true,
    var windows : Boolean = true
) : RealmObject()


class SplashActivity : AppCompatActivity() {

    val SPLASH_VIEW_TIME: Long = 1200 //스플래시 화면을 보여줌 (ms)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Handler().postDelayed({ //delay를 위한 handler
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, SPLASH_VIEW_TIME)
    }
}

class MainActivity : AppCompatActivity() {
    private val dBVersion = "200407"
    private val dBVersionFileName = "$dBVersion.txt"

    var tabLayout: TabLayout? = null
    var viewPager: ViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initDB()

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

    fun initDB() {
        //최신 DB파일인지 체크
        val checkDB: Boolean = checkFileExist(dBVersionFileName)

        //최신 DB파일이라면
        if (checkDB) {
            Log.d("DB체크", "최신 DB입니다.")
        } else { //최신 DB파일이 아니라면
            Log.d("DB체크", "최신 DB가 아닙니다.")
            Log.d("DB생성", "DB파일을 새롭게 생성합니다.")

            //즐겨찾기 정보 복원  -- 추후 작업필요!!!
            fun recoverFavoriteData() {

            }

            //필터세팅 정보 복원 -- 추후 작업필요!!!
            fun recoverFilterSettingData() {

            }

            //CSV파일에서 DB로 데이터 이전
            initDataFromCsvFile()

            //버전 관리를 위한 파일 생성
            val path = applicationContext.filesDir.toString() + "/" + dBVersionFileName
            val text = dBVersion
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    Files.write(Paths.get(path), text.toByteArray(), StandardOpenOption.CREATE)
                } catch (e: IOException) {
                    Log.d("오류", "버전 관리파일 생성 실패 (API Ver.26 over)")
                }
            } else {
                //API Level 26 미만에서는 예전 방식으로 진행해야...
                var fop: FileOutputStream? = null
                val file: File

                try {
                    file = File(path)
                    fop = FileOutputStream(file)

                    // if file doesnt exists, then create it
                    if (!file.exists()) {
                        file.createNewFile()
                    }

                    // get the content in bytes
                    val contentInBytes = text.toByteArray()
                    fop.write(contentInBytes)
                    fop.flush()
                    fop.close()
                } catch (e: IOException) {
                    Log.d("오류", "버전 관리파일 생성 실패 (API Ver.26 under)")
                    e.printStackTrace()
                } finally {
                    try {
                        fop?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            Log.d("DB생성결과", "DB파일 생성완료")
        }
    }

    fun initDataFromCsvFile() {
        var fileReader: BufferedReader? = null
        try {
            val shortcut_from_csv_array = ArrayList<Array<Any>>()
            var line: String?

            val inputStream: InputStream = resources.openRawResource(R.raw.org_db)
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
                        val thisShortcut : Array<Any> = arrayOf(
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
                        shortcut_from_csv_array.add(thisShortcut)
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
        Realm.init(this)
        val realm= Realm.getDefaultInstance()
        val shortcut = Shortcut()
        val filterSetting = FilterSetting()
        //단축키 정보 DB 입력
        for (items in dataArray) {
            shortcut.pk = items[0].toString().toInt()
            shortcut.category = items[1].toString()
            shortcut.category_hangul = items[2].toString()
            shortcut.ctrl = items[3].toString()
            shortcut.alt = items[4].toString()
            shortcut.shift = items[5].toString()
            shortcut.key1 = items[6].toString()
            shortcut.key2 = items[7].toString()
            shortcut.key3 = items[8].toString()
            shortcut.key4 = items[9].toString()
            shortcut.commandString = items[10].toString()
            shortcut.searchString = items[11].toString()
            shortcut.score = items[12].toString().toInt()
            shortcut.favorite = items[13].toString().toInt()
            shortcut.commandKeyStr = items[14].toString()
            realm.executeTransaction { realm ->
                realm.copyToRealmOrUpdate(shortcut)
            }
        }
        //핕러 설정 정보 DB 입력
        filterSetting.pk = 1
        filterSetting.powerpoint = false
        filterSetting.excel = false
        filterSetting.word = false
        filterSetting.hangul = false
        filterSetting.chrome = false
        filterSetting.windows = false
        realm.executeTransaction { realm ->
            realm.copyToRealmOrUpdate(filterSetting)
        }
    }

    fun checkFileExist(fileName: String): Boolean {
        //버전관리 파일이 존재하는가? 체크
        val fileList = applicationContext.filesDir.list()
        var fileExistCheck : Boolean = false
        for (file in fileList) {
            if (file == fileName) {
                fileExistCheck = true
                break
            }
        }
        return fileExistCheck
    }

}