package com.example.shortcutlibrary

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.sip.SipSession
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.*
import kotlin.collections.ArrayList


class search : Fragment() {

    var recyclerView: RecyclerView? = null
    var adapter: RecyclerView.Adapter<*>? = null
    var layoutManager: RecyclerView.LayoutManager? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val thisView = inflater.inflate(R.layout.fragment_search, container, false)
        recyclerView = thisView.findViewById<RecyclerView>(R.id.recycler_view)

        // 리사이클러뷰의 notify()처럼 데이터가 변했을 때 성능을 높일 때 사용한다.
        recyclerView!!.setHasFixedSize(true)

        layoutManager = LinearLayoutManager(context)
        recyclerView!!.layoutManager = layoutManager

        val searchTextArea = thisView.findViewById<EditText>(R.id.searchTextArea)
        var searchStr: String = searchTextArea.text.toString()
        searchTextArea.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                val imm = thisView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
        searchTextArea.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                searchStr = searchTextArea.text.toString()
                printShortCutList(searchStr)
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
        })

        printShortCutList(searchStr)

        // Inflate the layout for this fragment
        return thisView
    }

    fun printShortCutList(searchStr: String) {
        //데이터 로드
        Realm.init(context!!)
        val realmConfig = RealmConfiguration.Builder().build()
        val realm = DynamicRealm.getInstance(realmConfig)
        val query = realm.where("Shortcut")
        val filters = realm.where("FilterSetting").findAll().first()!!
        var filterCount: Int = 0
        val categoryNames = FilterSetting().javaClass.declaredFields

        query.beginGroup()
        categoryNames.forEach {
            if (it.name != "pk") {
                if (filters[it.name]) {
                    if (filterCount==0) {
                        query.equalTo("category", it.name)
                    } else {
                        query.or().equalTo("category", it.name)
                    }
                    filterCount += 1
                }
            }
        }
        query.endGroup()

        //데이터 변수에 저장
        val pkSet = ArrayList<Int>()
        val iconSet = ArrayList<Int>()
        val categoryHangulSet = ArrayList<String>()
        val commandKeyStrSet = ArrayList<String>()
        val commandStringSet = ArrayList<String>()
        val favoriteSet = ArrayList<Int>()

        if (filterCount>0) {
            //검색어 체크하여 쿼리 변경
            val shortcuts: RealmResults<DynamicRealmObject>
            if (searchStr != "") {
                val searchStrArr = searchStr.split(" ")
                if (searchStrArr.count()>1) {
                    val categoryNameArrEng: Array<String> = Shortcut().getCategoryNameArr("Eng")
                    val categoryNameArrKor: Array<String> = Shortcut().getCategoryNameArr("Kor")
                    if (searchStrArr[0] in categoryNameArrEng || searchStrArr[0] in categoryNameArrKor) {
                        query.beginGroup().equalTo("category", searchStrArr[0]).or().equalTo("category_hangul", searchStrArr[0]).endGroup()
                        query.beginGroup()
                        var index: Int = 0
                        for (searchStrByArr in searchStrArr) {
                            if (searchStrByArr != "") {
                                if (index > 0) {
                                    query.contains(
                                        "commandString",
                                        searchStrByArr,
                                        Case.INSENSITIVE
                                    )
                                        .or()
                                        .contains("searchString", searchStrByArr, Case.INSENSITIVE)
                                        .or()
                                        .contains("commandKeyStr", searchStrByArr, Case.INSENSITIVE)
                                }
                                index += 1
                            }
                        }
                        query.endGroup()
                    } else {
                        query.beginGroup()
                        for (searchStrByArr in searchStrArr) {
                            if (searchStrByArr != "") {
                                query
                                    .contains("category", searchStrByArr, Case.INSENSITIVE)
                                    .or()
                                    .contains("category_hangul", searchStrByArr, Case.INSENSITIVE)
                                    .or()
                                    .contains("commandString", searchStrByArr, Case.INSENSITIVE)
                                    .or().contains("searchString", searchStrByArr, Case.INSENSITIVE)
                                    .or()
                                    .contains("commandKeyStr", searchStrByArr, Case.INSENSITIVE)
                            }
                        }
                        query.endGroup()
                    }
                } else {
                    query.beginGroup()
                        .contains("category", searchStr, Case.INSENSITIVE)
                        .or().contains("category_hangul", searchStr, Case.INSENSITIVE)
                        .or().contains("commandString", searchStr, Case.INSENSITIVE)
                        .or().contains("searchString", searchStr, Case.INSENSITIVE)
                        .or().contains("commandKeyStr", searchStr, Case.INSENSITIVE)
                        .endGroup()
                }
                shortcuts = query.findAll()
            } else {
                shortcuts = query.findAll()
            }
            Log.d("단축키 Count", shortcuts.count().toString())
            for (item in shortcuts) {
                val thisIcon = Shortcut().getDrawableIcon(item["category"])
                pkSet.add(item["pk"])
                iconSet.add(thisIcon)
                categoryHangulSet.add(item["category_hangul"])
                commandKeyStrSet.add(item["commandKeyStr"])
                commandStringSet.add(item["commandString"])
                favoriteSet.add(item["favorite"])
            }
        } else {
            iconSet.add(R.drawable.ic_none)
            pkSet.add(0)
            categoryHangulSet.add("none")
            commandKeyStrSet.add("필터가 모두 Off 되었습니다.")
            commandStringSet.add("필터 스위치를 변경해 주세요.")
            favoriteSet.add(0)
        }

        // 어댑터 할당, 어댑터는 기본 어댑터를 확장한 커스텀 어댑터를 사용할 것이다.
        adapter = RecyclerViewAdapter(pkSet, iconSet, categoryHangulSet, commandKeyStrSet, commandStringSet, favoriteSet)
        recyclerView!!.adapter = adapter
    }
}

class RecyclerViewAdapter (
    private val pkSet: ArrayList<Int>,
    private val iconSet: ArrayList<Int>,
    private val categoryHangulSet: ArrayList<String>,
    private val commandKeyStrSet: ArrayList<String>,
    private val commandStringSet: ArrayList<String>,
    private val favoriteSet: ArrayList<Int>
    ) :
    RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    // 리사이클러뷰에 들어갈 뷰 홀더, 그리고 그 뷰 홀더에 들어갈 아이템들을 지정
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var pk: TextView = view.findViewById(R.id.shortcut_pk)
        var listItem: LinearLayout = view.findViewById(R.id.list_item)
        var favoriteIcon: ImageView = view.findViewById(R.id.shortcut_favoriteIcon)
        var imageView: ImageView = view.findViewById(R.id.shortcut_icon)
        var categoryHangulTextView : TextView = view.findViewById(R.id.shortcut_category_hangul)
        var commandKeyStrTextView: TextView = view.findViewById(R.id.shortcut_commandKeyStr)
        var commandStringTextView: TextView = view.findViewById(R.id.shortcut_commandString)
    }

    // 어댑터 클래스 상속시 구현해야할 함수 3가지 : onCreateViewHolder, onBindViewHolder, getItemCount
    // 리사이클러뷰에 들어갈 뷰 홀더를 할당하는 함수, 뷰 홀더는 실제 레이아웃 파일과 매핑되어야하며, extends의 Adater<>에서 <>안에들어가는 타입을 따른다.
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val holderView: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.search_list_view, viewGroup, false)
        return ViewHolder(holderView)
    }

    // 실제 각 뷰 홀더에 데이터를 연결해주는 함수
    override fun onBindViewHolder(ViewHolder: ViewHolder, i: Int) {
        ViewHolder.pk.text = pkSet[i].toString()
        ViewHolder.imageView.setBackgroundResource(iconSet[i])
        ViewHolder.categoryHangulTextView.text = categoryHangulSet[i]
        ViewHolder.commandKeyStrTextView.text = commandKeyStrSet[i]
        ViewHolder.commandStringTextView.text = commandStringSet[i]
        if (favoriteSet[i] > 0) {
            ViewHolder.favoriteIcon.setImageResource(R.drawable.ic_star_fill)
            ViewHolder.favoriteIcon.setColorFilter(Color.parseColor("#03A9F4"), PorterDuff.Mode.SRC_IN)
            ViewHolder.favoriteIcon.tag = "true"
        }

        //나의 단축키 클릭 이벤트 리스너
        ViewHolder.favoriteIcon.setOnClickListener(View.OnClickListener { view ->
            val thisImage = view.findViewById<ImageView>(R.id.shortcut_favoriteIcon)
//            val thisPk = view.findViewById<TextView>(R.id.shortcut_pk).text.toString()
//            if (thisImage.tag == "false") {
//                setFavorite(thisPk, 1)
//            } else {
//                setFavorite(thisPk, 0)
//            }
        })

        //클릭 이벤트 리스너
        ViewHolder.listItem.setOnClickListener(View.OnClickListener { view ->
            val thisTitleStr = view.findViewById<TextView>(R.id.shortcut_commandKeyStr).text
            val thisMessageStr = view.findViewById<TextView>(R.id.shortcut_commandString).text
            val thisCategoryNameHangul = view.findViewById<TextView>(R.id.shortcut_category_hangul).text
            val thisImage = Shortcut().getDrawableIconByHangul(thisCategoryNameHangul.toString())

            val context: Context = view.context
            val inflater = LayoutInflater.from(context)
            val customAlertView = inflater.inflate(R.layout.custom_alert_dialog, null)
            val builder: AlertDialog.Builder = AlertDialog.Builder(context, R.style.MyAlertDialogStyle) //버튼 스타일은 별도로 지정

            //builder.setTitle(thisTitleStr).setMessage(thisMessageStr) //서식이 들어간 커스텀 뷰로 대체함

            builder.setNegativeButton("닫기") { _, _ ->  }
            builder.setPositiveButton("나의 단축키로 등록") { _, _ ->
                //나의 단축키로 저장
                val thisFavoriteImage = view.findViewById<ImageView>(R.id.shortcut_favoriteIcon)
                thisFavoriteImage.setImageResource(R.drawable.ic_star_fill)
                thisFavoriteImage.setColorFilter(Color.parseColor("#03A9F4"), PorterDuff.Mode.SRC_IN)
                thisFavoriteImage.tag = "true"

                //알림
                Toast.makeText(context,"${thisTitleStr} : 나의 단축키로 콕~!", Toast.LENGTH_SHORT).show()
            }
            val alertDialog: AlertDialog = builder.create()
            customAlertView.findViewById<ImageView>(R.id.alertImageViewCustom).setImageResource(thisImage)
            customAlertView.findViewById<TextView>(R.id.alertTitleCustom).text = thisTitleStr
            customAlertView.findViewById<TextView>(R.id.alertMessageCustom).text = thisMessageStr
            alertDialog.setView(customAlertView)
            alertDialog.show()
        })
    }

    private fun setFavorite(pk: String, value: Int) {
        val realmConfig = RealmConfiguration.Builder().build()
        val realm = DynamicRealm.getInstance(realmConfig)
        realm.beginTransaction()
        realm.where("ShortCut").equalTo("pk", pk.toInt()).findAll().setInt("pk", value)
        realm.commitTransaction()
    }

    //iOS의 numberOfRows와 동일. 리사이클러뷰안에 들어갈 뷰 홀더의 개수
    override fun getItemCount(): Int {
        return commandKeyStrSet.size
    }
}

