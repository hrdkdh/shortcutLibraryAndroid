package com.example.shortcutlibrary

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.Realm
import io.realm.kotlin.where


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

        //데이터 로드
        Realm.init(context!!)
        val realm= Realm.getDefaultInstance()
        val result = realm.where<Shortcut>().findAll()

        //데이터 변수에 저장
        var iconSet = ArrayList<Int>()
        var categoryHangulSet = ArrayList<String>()
        var commandKeyStrSet = ArrayList<String>()
        var commandStringSet = ArrayList<String>()
        Log.d("단축키 Count", result.count().toString())
        for (item in result) {
            val thisIcon = getDrawableIcon(item.category)
            iconSet.add(thisIcon)
            categoryHangulSet.add(item.category_hangul)
            commandKeyStrSet.add(item.commandKeyStr)
            commandStringSet.add(item.commandString)
        }
        // 어댑터 할당, 어댑터는 기본 어댑터를 확장한 커스텀 어댑터를 사용할 것이다.
        adapter = RecyclerViewAdapter(iconSet, categoryHangulSet, commandKeyStrSet, commandStringSet)
//        adapter.setItemClickListener( object : RecyclerViewAdapter.ItemClickListener{
//            override fun onClick(view: View, i: Int) {
//                val builder: AlertDialog.Builder = AlertDialog.Builder(context)
//                builder.setTitle("인사말").setMessage("반갑습니다")
//                val alertDialog: AlertDialog = builder.create()
//                alertDialog.show()
//            }
//        })
        recyclerView!!.adapter = adapter
//
//        //클릭리스너 등록
//        recyclerView!!.layoutManager = LinearLayoutManager(context)


        // Inflate the layout for this fragment
        return thisView
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
}

class RecyclerViewAdapter (private val iconSet: ArrayList<Int>, private val categoryHangulSet: ArrayList<String>, private val commandKeyStrSet: ArrayList<String>, private val commandStringSet: ArrayList<String>) :
    RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    // 리사이클러뷰에 들어갈 뷰 홀더, 그리고 그 뷰 홀더에 들어갈 아이템들을 지정
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var imageView: ImageView = view.findViewById(R.id.shortcut_icon)
        val categoryHangulTextView : TextView = view.findViewById(R.id.shortcut_category_hangul)
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
    override fun onBindViewHolder(myViewHolder: ViewHolder, i: Int) {
        myViewHolder.imageView.setBackgroundResource(iconSet[i])
        myViewHolder.categoryHangulTextView.text = categoryHangulSet[i]
        myViewHolder.commandKeyStrTextView.text = commandKeyStrSet[i]
        myViewHolder.commandStringTextView.text = commandStringSet[i]
//
//        //각 리스트에 클릭 이벤트 리스너 등록
//        myViewHolder.itemView.setOnClickListener {
//            itemClickListner.onClick(it, i)
//        }
    }

    //iOS의 numberOfRows와 동일. 리사이클러뷰안에 들어갈 뷰 홀더의 개수
    override fun getItemCount(): Int {
        return commandKeyStrSet.size
    }
//
//    //클릭 인터페이스 정의
//    interface ItemClickListener {
//        fun onClick(view: View, i: Int)
//    }
//
//    //클릭리스너 선언
//    private lateinit var itemClickListner: ItemClickListener
//
//    //클릭리스너 등록 매소드
//    fun setItemClickListener(itemClickListener: ItemClickListener) {
//        this.itemClickListner = itemClickListener
//    }

}

