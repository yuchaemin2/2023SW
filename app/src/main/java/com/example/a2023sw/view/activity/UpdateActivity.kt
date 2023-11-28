package com.example.a2023sw.view.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.a2023sw.view.dialog.LoadingDialog
import com.example.a2023sw.R
import com.example.a2023sw.databinding.ActivityUpdateBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UpdateActivity : AppCompatActivity() {
    lateinit var binding: ActivityUpdateBinding

    lateinit var docId: String

    private lateinit var customProgressDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        docId = intent.getStringExtra("docId") ?: ""

        fetchRecordData()

        // 로딩창 객체 생성
        customProgressDialog = LoadingDialog(this)
        // 로딩창을 투명하게
        customProgressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        customProgressDialog.setCancelable(false)

        var toolbar = binding.toolbarBack
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 뒤로가기 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back)
        supportActionBar?.setDisplayShowTitleEnabled(false)//타이틀 없애기
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add, menu)
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
                    updateRecord()
                    val resultIntent = Intent()
                    resultIntent.putExtra("isUpdated", true)
                    setResult(Activity.RESULT_OK, resultIntent)

                    customProgressDialog.show()

                    Handler(Looper.getMainLooper()).postDelayed({
                        if (!isFinishing) {  // Check if the activity is still running
                            customProgressDialog.dismiss() // 다이얼로그 종료
                            finish()
                        }
                    }, 3000) // 2000ms(2초) 후에 종료하도록 설정 (원하는 시간으로 변경 가능)
                } else {
                    Toast.makeText(this, "내용을 입력해주세요..", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun fetchRecordData() {
        // reviewId를 사용하여 해당 리뷰 데이터 가져오기
        val db = Firebase.firestore
        val recordRef = db.collection("photos").document(docId)

        recordRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val title = document.getString("title")
                    val date = document.getString("date")
                    val foodTime = document.getString("foodTime")
                    val food = document.getString("food")
                    val company = document.getString("company")
                    val where = document.getString("where")
                    val memo = document.getString("memo")
                    val count = document.getString("count")
                    val bookmark = document.getString("bookmark")
//                    val uriList = document.getStringArray("uriList")

                    binding.writeText.setText(title)
                    binding.foodTimeText.setText(foodTime)
                    binding.foodText.setText(food)
                    binding.companyText.setText(company)
                    binding.whereText.setText(where)
                    binding.memoText.setText(memo)
//                    if(movieImage != null && movieImage != "null"){
//                        // Glide를 사용하여 프로필 이미지 로드
//                        Glide.with(baseContext)
//                            .load(movieImage)
//                            .into(binding.addImageView)
//                    }
                }
            }
            .addOnFailureListener { exception ->
                // 데이터 가져오기 실패 처리
            }
    }

    private fun updateRecord() {
        val db = Firebase.firestore
        val title = binding.writeText.text.toString()
        val food = binding.foodText.text.toString()
        val foodTime = binding.foodText.text.toString()
        val company = binding.companyText.text.toString()
        val where = binding.whereText.text.toString()
        val memo = binding.memoText.text.toString()

        if (title.isNotEmpty()) {
            // 리뷰 데이터 업데이트
            val recordRef = db.collection("photos").document(docId)
            val data = hashMapOf(
                "title" to title,
                "food" to food,
                "foodTime" to foodTime,
                "company" to company,
                "where" to where,
                "memo" to memo
                // 다른 필드도 필요한 경우 추가
            )

            recordRef.update(data as Map<String, Any>)
                .addOnSuccessListener {
                    // 업데이트 성공 처리
                    setResult(RESULT_OK)
                    finish()
                }
                .addOnFailureListener { exception ->
                    // 업데이트 실패 처리
                }
        } else {
            // 제목이나 내용이 비어있을 경우 처리
            // 필요한 경우 사용자에게 알림을 보여줄 수 있습니다.
        }
    }

}