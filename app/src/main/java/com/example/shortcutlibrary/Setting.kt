package com.example.shortcutlibrary

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import io.realm.DynamicRealm
import io.realm.RealmConfiguration

class Setting : Fragment() {

    lateinit var mAdView : AdView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val thisView = inflater.inflate(R.layout.fragment_setting, container, false)

        MobileAds.initialize(thisView.context) {}
        mAdView = thisView.findViewById(R.id.adView_setting)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        val realmConfig = RealmConfiguration.Builder().build()
        val realm = DynamicRealm.getInstance(realmConfig)

        val result = realm.where("FilterSetting").findAll()
        val categoryNames = FilterSetting().javaClass.declaredFields

        categoryNames.forEach {
            if (it.name != "pk") {
                val thisFieldName : String = it.name
                val thisSwitch = thisView.findViewWithTag<Switch>(thisFieldName)
                val thisCategoryNameHangul = Shortcut().getCategoryNameHangul(thisFieldName)
                if (result[0]!![it.name]) {
                    thisSwitch.isChecked = true
                }
                //클릭 시 이벤트 리스너
                thisSwitch.setOnClickListener {
                    if (thisSwitch.isChecked) {
                        setFilterValue(thisFieldName, true)
                        Toast.makeText(context, "$thisCategoryNameHangul : 설정됨", Toast.LENGTH_SHORT).show()
                    } else {
                        setFilterValue(thisFieldName, false)
                        Toast.makeText(context, "$thisCategoryNameHangul : 해제됨", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Inflate the layout for this fragment
        return thisView
    }

    fun setFilterValue(FieldName: String, value: Boolean) {
        val realmConfig = RealmConfiguration.Builder().build()
        val realm = DynamicRealm.getInstance(realmConfig)
        realm.beginTransaction()
        realm.where("FilterSetting").findFirst()!!.set(FieldName, value)
        realm.commitTransaction()
    }
}
