package com.example.shortcutlibrary

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import io.realm.*
import kotlin.collections.ArrayList

class Quiz : Fragment(), View.OnClickListener {
    private lateinit var mInterstitialAd: InterstitialAd

    var thisView : View? = null
    var questionData = ArrayList<Map<String, Any>>()
    val questionMaxCnt: Int = 10
    var questionCntNo: Int = 1
    var score : Int = 0
    var wantMoreQuizCheck: Boolean = false
    val animationDuration: Int = 200

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        thisView = inflater.inflate(R.layout.fragment_quiz, container, false)

        MobileAds.initialize(thisView!!.context) {}
        mInterstitialAd = InterstitialAd(thisView!!.context)
        mInterstitialAd.adUnitId = getString(R.string.adView_quiz_adId)
        mInterstitialAd.loadAd(AdRequest.Builder().build()) //광고 로드
        mInterstitialAd.adListener = object: AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            override fun onAdFailedToLoad(errorCode: Int) {
                // Code to be executed when an ad request fails.
            }

            override fun onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            override fun onAdClosed() {
                // Code to be executed when the interstitial ad is closed.
                if (!mInterstitialAd.isLoaded) {
                    mInterstitialAd.loadAd(AdRequest.Builder().build()) //광고 로드
                }
                printQuiz(questionCntNo)
            }
        }
        // Inflate the layout for this fragment
        return thisView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        thisView?.findViewById<LinearLayout>(R.id.quiz_main_wrap)!!.visibility = View.INVISIBLE

        thisView?.findViewById<TextView>(R.id.quiz_entrance_button)!!.setOnClickListener(this)
        thisView?.findViewById<TextView>(R.id.quiz_end_button)!!.setOnClickListener(this)
        for (i in 1..5) {
            thisView!!.findViewById<TextView>(getOptionRid(i)).setOnClickListener(this)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.quiz_entrance_button -> {
                if (!mInterstitialAd.isLoaded) {
                    mInterstitialAd.loadAd(AdRequest.Builder().build()) //광고 로드
                }
                loadQuiz()
            }
            R.id.quiz_end_button -> {
                resetQuiz()
            }
            R.id.quiz_option_1 -> {
                checkAnswer(R.id.quiz_option_1)
            }
            R.id.quiz_option_2 -> {
                checkAnswer(R.id.quiz_option_2)
            }
            R.id.quiz_option_3 -> {
                checkAnswer(R.id.quiz_option_3)
            }
            R.id.quiz_option_4 -> {
                checkAnswer(R.id.quiz_option_4)
            }
            R.id.quiz_option_5 -> {
                checkAnswer(R.id.quiz_option_5)
            }
        }
    }

    private fun resetQuiz() {
        questionData = ArrayList<Map<String, Any>>()
        questionCntNo = 1
        score = 0

        //퀴즈 화면 Off
        thisView!!.findViewById<LinearLayout>(R.id.quiz_entrance_wrap).apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate()
                .alpha(1f)
                .setDuration(animationDuration.toLong())
                .setListener(null)
        }

        thisView!!.findViewById<LinearLayout>(R.id.quiz_main_wrap).animate()
            .alpha(0f)
            .setDuration(animationDuration.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    thisView!!.findViewById<LinearLayout>(R.id.quiz_main_wrap).visibility = View.INVISIBLE
                }
            })
    }

    private fun loadQuiz() {
        //데이터 로드
        Realm.init(thisView!!.context!!)
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

        if (filterCount>0) {
            //검색어 체크하여 쿼리 변경
            val shortcuts: RealmResults<DynamicRealmObject>
            shortcuts = query.findAll()

            //로드한 데이터가 questionMaxCnt 이상 로드되어야만 출력함
            if (shortcuts.count() >= questionMaxCnt) {
                //랜덤으로 10개 단축키 로드 (필터 적용)
                var numbers = ArrayList<Int>()
                val lastRandomNumber: Int = shortcuts.count()-1
                while (numbers.count() < 100) { //중복 시 재시도까지 고려해 넉넉히 100번 돌리자...
                    val number =(0..lastRandomNumber).random()
                    if (!numbers.contains(number)) {
                        numbers.add(number)
                    }
                    if (numbers.count()==questionMaxCnt) { //문항수 가득 채워지면 중지
                        break
                    }
                }
                //Log.d("랜덤 숫자", numbers.toString())
                //문제를 배열로 저장
                for (questionNo in numbers) {
                    val thisQuestion = shortcuts[questionNo]
                    val thisQuestionPk: Int = thisQuestion!!["pk"]
                    val thisDic: Map<String, Any> = mapOf("pk" to thisQuestionPk, "category" to thisQuestion["category"], "commandKeyStr" to thisQuestion["commandKeyStr"], "commandString" to thisQuestion["commandString"])
                    questionData.add(thisDic)
                }
                //누적 score 초기화
                score = 0

                //문항번호 초기화
                questionCntNo = 1

                //퀴즈에 참여하였음을 기록
                wantMoreQuizCheck = true

                //광고 로드 후 첫 문제 출제
                if (mInterstitialAd.isLoaded) {
                    mInterstitialAd.show()
                } else {
                    Log.d("전면광고", "전면광고가 로드되지 않음")
                    printQuiz(questionCntNo)
                }
            } else {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("문항로드 실패").setMessage("로드한 문항의 갯수가 너무 적어 퀴즈를 실행할 수 없습니다.")
                val alertDialog = builder.create()
                alertDialog.show()
            }

            //퀴즈 화면 On
            thisView!!.findViewById<LinearLayout>(R.id.quiz_main_wrap).apply {
                alpha = 0f
                visibility = View.VISIBLE
                animate()
                    .alpha(1f)
                    .setDuration(animationDuration.toLong())
                    .setListener(null)
            }

            thisView!!.findViewById<LinearLayout>(R.id.quiz_entrance_wrap).animate()
                .alpha(0f)
                .setDuration(animationDuration.toLong())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        thisView!!.findViewById<LinearLayout>(R.id.quiz_entrance_wrap).visibility = View.INVISIBLE
                    }
                })
        } else {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("필터설정 확인필요").setMessage("필터가 모두 Off 되었습니다. 필터 설정 메뉴에서 필터를 On으로 바꿔 주세요!")
            val alertDialog = builder.create()
            alertDialog.show()
        }
    }

    private fun printQuiz(thisQuestionCntNo: Int) {
        view!!.findViewById<TextView>(R.id.quiz_main_question_no).text = thisQuestionCntNo.toString()+"/"+questionMaxCnt.toString()
        val questionCntNoForArr: Int = thisQuestionCntNo-1
        val thisQuestionData = questionData[questionCntNoForArr]
        val categoryNameEng: String = thisQuestionData["category"].toString()
        val categoryNameKor: String = Shortcut().getCategoryNameHangul(categoryNameEng)
        val thisCommandKeyStr: String = thisQuestionData["commandKeyStr"].toString()
        val thisCommandString: String = thisQuestionData["commandString"].toString()

        //문항 유형을 어떤 것으로 할 것인가.. 랜덤 선택
        val cate = (1..2).random()

        //문제 출력
        var correctOptionStr: String?
        var correctOptionCate = ""

        view!!.findViewById<ImageView>(R.id.quiz_question_icon).setImageResource(Shortcut().getDrawableIcon(categoryNameEng))
        view!!.findViewById<TextView>(R.id.quiz_question_icon_category_name).text = categoryNameKor
        if (cate == 1) { //단축키 명령어가 문제로 나오는 유형
            correctOptionCate = "commandString"
            view!!.findViewById<TextView>(R.id.quiz_main_pre_question).text =
                "아래 단축키로 할 수 있는 작업은 무엇일까요?"
            view!!.findViewById<TextView>(R.id.quiz_question_string).text = thisCommandKeyStr
        } else if (cate == 2) { //단축키 설명이 문제로 나오는 유형
            correctOptionCate = "commandKeyStr"
            view!!.findViewById<TextView>(R.id.quiz_main_pre_question).text = "아래 작업을 할 수 있는 단축키는 무엇일까요?"
            view!!.findViewById<TextView>(R.id.quiz_question_string).text = thisCommandString
        }
        correctOptionStr = thisQuestionData[correctOptionCate].toString()

        val optionButtons = ArrayList<TextView>()
        for (i in 1..5) {
            optionButtons.add(thisView!!.findViewById<TextView>(getOptionRid(i)))
        }
        //오답 추출
        Realm.init(thisView!!.context!!)
        val realmConfig = RealmConfiguration.Builder().build()
        val realm = DynamicRealm.getInstance(realmConfig)
        val query = realm.where("Shortcut")
        val thisPkForOption: Int = thisQuestionData["pk"] as Int
        query.beginGroup()
            .equalTo("category", categoryNameEng)
            .and()
            .notEqualTo("commandKeyStr", thisCommandString)
            .and()
            .notEqualTo("pk", thisPkForOption)
            .endGroup()

        val shortcuts: RealmResults<DynamicRealmObject>
        shortcuts = query.findAll()

        //랜덤으로 오답 4개 로드
        var anotherOptionList = ArrayList<String>()
        while (anotherOptionList.count() < 10) { //중복 시 재시도까지 고려해 넉넉히 10번 돌리자...
            val lastRandomNumber: Int = shortcuts.count()-1
            val number =(0..lastRandomNumber).random()
            val thisCorrectOptionCate = shortcuts[number]?.get(correctOptionCate) as String
            if (!anotherOptionList.contains(thisCorrectOptionCate)) {
                anotherOptionList.add(thisCorrectOptionCate)
            }
            if (anotherOptionList.count()==5) { //오답수 가득 채워지면 중지
                break
            }
        }
        //몇번째 보기에 정답을 넣을 것인가.. 랜덤 선택
        val correctOptionNo = (1..5).random()
        val correctButton = thisView!!.findViewById<TextView>(getOptionRid(correctOptionNo))

        var index=0
        for (button in optionButtons) {
            if (button == correctButton) {
                button.text = correctOptionStr
                button.tag = "true"
            } else {
                button.text = anotherOptionList[index]
                button.tag = "false"
                index+=1
            }
        }
    }

    private fun checkAnswer(thisElement: Int) {
        questionCntNo+=1
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false)
        if (thisView!!.findViewById<TextView>(thisElement).tag == "true") {
            builder.setTitle("결과").setMessage("정답입니다")
            builder.setNegativeButton("닫기") { _, _ ->  }
            score += 1
        } else {
            val optionButtons = ArrayList<TextView>()
            for (i in 1..5) {
                optionButtons.add(thisView!!.findViewById<TextView>(getOptionRid(i)))
            }
            var correctStr = ""
            for (button in optionButtons) {
                if (button.tag == "true") {
                    correctStr = button.text.toString()
                    break
                }
            }
            builder.setTitle("오답입니다").setMessage("정답은 '${correctStr}'입니다.")
        }

        if (questionCntNo == questionMaxCnt+1) {
            builder.setNegativeButton("최종결과 확인") { _, _ ->
                //show score action
                printQuizResult()
            }
        } else {
            builder.setNegativeButton("다음 문제") { _, _ ->
                printQuiz(questionCntNo)
            }
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun printQuizResult() {
        val finalScore = (score*10).toString()
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false)
        builder.setTitle("최종 결과").setMessage("총 "+questionMaxCnt.toString()+"문제 중 "+score.toString()+"문제를 맞춰 최종 점수는 ${finalScore}점입니다.")
        builder.setNegativeButton("퀴즈 종료") { _, _ ->
            resetQuiz()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun getOptionRid(num: Int): Int {
        when (num) {
            1 -> return R.id.quiz_option_1
            2 -> return R.id.quiz_option_2
            3 -> return R.id.quiz_option_3
            4 -> return R.id.quiz_option_4
            5 -> return R.id.quiz_option_5
            else -> return R.id.quiz_option_1
        }
    }
}
