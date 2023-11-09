package com.example.a2023sw

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.a2023sw.MyApplication.Companion.auth
import com.example.a2023sw.MyApplication.Companion.email
import com.example.a2023sw.R
import com.example.a2023sw.databinding.ActivityAddBinding
import java.text.SimpleDateFormat
import java.util.*

class AddActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var toolbar = binding.toolbarBack
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 뒤로가기 버튼 활성화
        supportActionBar?.setDisplayShowTitleEnabled(false)//타이틀 없애기
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_nav, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> { //뒤로 가기 버튼
                onBackPressed() // 기본 뒤로가기 동작 수행
                return true
            }
            R.id.save_record ->{
                if(binding.writeText.text.isNotEmpty()){
                    saveStore()
                    finish()
                } else {
                    Toast.makeText(this, "제목을 입력해주세요..", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun dateToString(date: Date): String {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREAN)
        return format.format(date)
    }

    fun saveStore() {
        val data = mapOf(
            "email" to MyApplication.email,
            "title" to binding.writeText.text.toString(),
            "date" to dateToString(Date()),
            "foodTime" to binding.foodTimeText.text.toString(),
            "food" to binding.foodText.text.toString(),
            "foodImage" to binding.foodImage,
            "company" to binding.companyText.text.toString(),
            "uid" to auth.uid,
            "where" to binding.whereText.text.toString(),
            "memo" to binding.memoText.text.toString(),
            "nickName" to binding.nickname.text.toString(),
        )

        MyApplication.Companion.db.collection("records")
            .add(data)
            .addOnSuccessListener {
                Log.d("2023sw", "data firestore save ok")
//                uploadImage(it.id)
            }
            .addOnFailureListener {
                Log.d("2023sw", "data firestore save error")
            }
    }
}