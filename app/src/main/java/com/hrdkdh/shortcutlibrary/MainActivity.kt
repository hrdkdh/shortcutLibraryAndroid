package com.hrdkdh.shortcutlibrary

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager.widget.ViewPager
import com.example.shortcutlibrary.R
import com.google.android.material.tabs.TabLayout
import io.realm.*
import io.realm.annotations.PrimaryKey
import io.realm.kotlin.where
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

//admob 앱 ID: ca-app-pub-4567650475621525~4565415172
//단축키 사전 배너광고 ID : ca-app-pub-4567650475621525/7738373422
//단축키 퀴즈 배너광고 ID : ca-app-pub-4567650475621525/6972086664 //전면 광고
//나의 단축키 배너광고 ID : ca-app-pub-4567650475621525/8668311713
//필터설정 배너광고 ID : ca-app-pub-4567650475621525/3224413347

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
    var commandKeyStr : String = "",
    var printOrder: Int = 0
) : RealmObject() {

    fun getCategoryNameArr(categoryLanguage: String) : Array<String> {
        if (categoryLanguage == "Eng") {
            return arrayOf("powerpoint", "excel", "word", "hangul", "chrome", "windows")
        } else {
            return arrayOf("파워포인트", "엑셀", "워드", "아래아한글", "크롬", "윈도우")
        }
    }
    //항목이 추가될 때를 대비!  fragment에서 파편화되지 않도록 한번에 관리하기 위해 여기에 관련 메쏘드와 변수를 등록함
    fun getCategoryNameHangul(shortCutCategoryEng : String) : String {
        when (shortCutCategoryEng) {
            "powerpoint" -> return "파워포인트"
            "excel" -> return "엑셀"
            "word" -> return "워드"
            "hangul" -> return "아래아한글"
            "chrome" -> return "크롬"
            "windows" -> return "윈도우"
            else -> return "none"
        }
    }
    fun getDrawableIcon(shortCutCategoryEng : String) : Int {
        when (shortCutCategoryEng) {
            "powerpoint" -> return R.drawable.ic_powerpoint
            "excel" -> return R.drawable.ic_excel
            "word" -> return R.drawable.ic_word
            "hangul" -> return R.drawable.ic_hangul
            "chrome" -> return R.drawable.ic_chrome
            "windows" -> return R.drawable.ic_windows
            else -> return R.drawable.ic_none
        }
    }
    fun getDrawableIconByHangul(shortCutCategoryHangul : String) : Int {
        when (shortCutCategoryHangul) {
            "파워포인트" -> return R.drawable.ic_powerpoint
            "엑셀" -> return R.drawable.ic_excel
            "워드" -> return R.drawable.ic_word
            "아래아한글" -> return R.drawable.ic_hangul
            "크롬" -> return R.drawable.ic_chrome
            "윈도우" -> return R.drawable.ic_windows
            else -> return R.drawable.ic_none
        }
    }
}

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
    private val dBVersion = "200410"
    private val dBVersionFileName = "$dBVersion.txt"
    private var favoriteSavedData = ArrayList<Int>()
    private var filterSavedData = mapOf<String, String>()

    var tabLayout: TabLayout? = null
    var viewPager: ViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initDB()

        tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        viewPager = findViewById<ViewPager>(R.id.viewPager)

        val adapter = MyAdapter(
            this,
            supportFragmentManager,
            tabLayout!!.tabCount
        )
        viewPager!!.adapter = adapter

        viewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        //첫번째 탭은 이미 선택되어 있으므로 아이콘 색 하얗게~
        tabLayout!!.getTabAt(0)!!.icon!!.setTint(ResourcesCompat.getColor(resources,
            R.color.colorTabIconSelected, null))

        tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                //탭 아이콘 색 바꾸기_selected
                tab.icon!!.setTint(ResourcesCompat.getColor(resources,
                    R.color.colorTabIconSelected, null))
                
                //탭 포지션 넘버로 뷰페이저 연결해서 띄워줌
                viewPager!!.currentItem = tab.position
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {
                //탭 아이콘 색 바꾸기_orginal
                tab.icon!!.setTint(ResourcesCompat.getColor(resources,
                    R.color.colorTabIcon, null))
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

            //나의 단축키와 필터 설정 정보 복원을 위해 미리 저장
            favoriteSavedData = recoverFavoriteData()
            filterSavedData = recoverFilterSettingData()

            //CSV파일에서 DB로 데이터 이전
            initDataFromCsvFile()

            //버전 관리를 위한 파일 생성
            val path = applicationContext.filesDir.toString() + "/" + dBVersionFileName
            val text = dBVersion
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    Files.write(Paths.get(path), text.toByteArray(), StandardOpenOption.CREATE)
                    Log.d("생성", "버전 관리파일 생성 성공 (API Ver.26 over)")
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
                    Log.d("생성", "버전 관리파일 생성 성공 (API Ver.26 under)")
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
                            commandKeyStr,
                            tokens[14].toInt()
                        )
                        shortcut_from_csv_array.add(thisShortcut)
                    }
                    line = fileReader.readLine()
                }
            }

            //DB업데이트
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
        try {
            Realm.init(this)
            val realm = Realm.getDefaultInstance()
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
                shortcut.printOrder = items[15].toString().toInt()
                realm.executeTransaction { realm ->
                    realm.copyToRealmOrUpdate(shortcut)
                }
            }
            //핕러 설정 정보 DB 입력
            filterSetting.pk = 1
            filterSetting.powerpoint = true
            filterSetting.excel = true
            filterSetting.word = true
            filterSetting.hangul = true
            filterSetting.chrome = true
            filterSetting.windows = true
            realm.executeTransaction { realm ->
                realm.copyToRealmOrUpdate(filterSetting)
            }
            Log.d("DB생성결과", "DB파일 생성완료")
        } catch(e : Exception) {
            Log.d("DB생성결과", "DB파일 생성실패!!!")
            e.printStackTrace()
        }

        try {
            //기존 정보 복원을 위한 세팅
            val realmConfig = RealmConfiguration.Builder().build()
            val realmDynamic = DynamicRealm.getInstance(realmConfig)

            if (favoriteSavedData.count()>0) {
                var shortcuts: RealmResults<DynamicRealmObject>
                //나의 단축키 정보 복원
                for (item in favoriteSavedData) {
                    shortcuts = realmDynamic.where("Shortcut")
                        .equalTo("pk", item)
                        .findAll()
                    realmDynamic.beginTransaction()
                    shortcuts.setInt("favorite", 1)
                    realmDynamic.commitTransaction()
                }
                Log.d("복원", "나의 단축키 기존 정보 업데이트 성공")
            }
            //필터 설정 정보 복원
            if (filterSavedData.count()>0) {
                var filterSettings: RealmResults<DynamicRealmObject> =
                    realmDynamic.where("FilterSetting").findAll()
                for ((key, value) in filterSavedData) {
                    var thisValue = false;
                    if (value == "true") {
                        thisValue = true
                    }
                    realmDynamic.beginTransaction()
                    filterSettings.setBoolean(key, thisValue)
                    realmDynamic.commitTransaction()
                }
                Log.d("복원", "필터설정 기존 정보 업데이트 성공")
            }
        } catch (e : Exception) {
            Log.d("error", "나의 단축키, 필터설정 기존 정보 업데이트 에러!")
            e.printStackTrace()
        }
    }

    private fun checkFileExist(fileName: String): Boolean {
        //버전관리 파일이 존재하는가? 체크
        var fileExistCheck : Boolean = false
        try {
            val fileList = applicationContext.filesDir.list()
            for (file in fileList!!) {
                if (file == fileName) {
                    fileExistCheck = true
                    break
                }
            }
        } catch(e: Exception) {
            Log.d("오류", "파일 탐색 실패")
        }
        return fileExistCheck
    }

    //즐겨찾기 정보 배열로 저장
    private fun recoverFavoriteData(): ArrayList<Int> {
        var favoriteArray = ArrayList<Int>()
        try {
            Realm.init(this)
            val realmConfig = RealmConfiguration.Builder().build()
            val realm = Realm.getInstance(realmConfig)
            val favorites = realm.where<Shortcut>().equalTo("favorite", 1.toInt()).findAll()
            for (favorite in favorites) {
                favoriteArray.add(favorite.pk)
            }
        } catch(e : Exception) {
            Log.d("에러", "나의 단축키 저장 정보 로드 실패")
            e.printStackTrace()
        }
        return favoriteArray
    }

    //필터세팅 정보 배열로 저장
    private fun recoverFilterSettingData(): Map<String, String> {
        var filterMap = hashMapOf<String, String>()
        try {
            Realm.init(this)
            val realmConfig = RealmConfiguration.Builder().build()
            val realm = DynamicRealm.getInstance(realmConfig)
            val filterData = realm.where("FilterSetting").findFirst()
            val categoryNames = Shortcut().getCategoryNameArr("Eng")
            for (item in categoryNames) {
                if (filterData!!.get(item)) {
                    filterMap[item] = "true"
                } else {
                    filterMap[item] = "false"
                }
            }
        } catch(e : Exception) {
            Log.d("에러", "필터 세팅 저장 정보 로드 실패")
            e.printStackTrace()
        }
        return filterMap
    }
}