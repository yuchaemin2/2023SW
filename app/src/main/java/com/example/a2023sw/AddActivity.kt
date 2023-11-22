package com.example.a2023sw

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.a2023sw.MyApplication.Companion.auth
import com.example.a2023sw.databinding.ActivityAddBinding
import com.example.a2023sw.ui.home.HomeFragment
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AddActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddBinding
    lateinit var filePath: String

    private lateinit var imageView: ImageView
    private var imageUrl : String? = null

    var uriList: ArrayList<Uri> = ArrayList()
    private val maxNumber = 10
    lateinit var adapter: ImageAdapter

    lateinit var docId: String

    private lateinit var customProgressDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)

        CoroutineScope(Dispatchers.Main).launch {
            imageUrl =  MyApplication.getImageUrl(MyApplication.email).toString()
            imageView = binding.userProfile
            if( imageUrl != null){
                Glide.with(this@AddActivity)
                    .load(imageUrl)
                    .into(binding.userProfile)
            }
        }

        printCount()
        // RecyclerView에 Adapter 연결하기
        adapter = ImageAdapter(this, uriList)
        binding.recyclerview.adapter = adapter
        // LinearLayoutManager을 사용하여 수평으로 아이템을 배치한다.
        binding.recyclerview.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        binding.imageArea.setOnClickListener {
            if (uriList.count() == maxNumber) {
                Toast.makeText(
                    this,
                    "이미지는 최대 ${maxNumber}장까지 첨부할 수 있습니다.",
                    Toast.LENGTH_SHORT
                ).show();
                return@setOnClickListener
            }
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
//            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            registerForActivityResult.launch(intent)
        }

        // 로딩창 객체 생성
        customProgressDialog = LoadingDialog(this)
        // 로딩창을 투명하게
        customProgressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        customProgressDialog.setCancelable(false)

//        binding.nickname.text =

        var toolbar = binding.toolbarBack
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 뒤로가기 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back)
        supportActionBar?.setDisplayShowTitleEnabled(false)//타이틀 없애기
    }

    private val registerForActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                AppCompatActivity.RESULT_OK -> {
                    val clipData = result.data?.clipData
                    if (clipData != null) { // 이미지를 여러 개 선택할 경우
                        val clipDataSize = clipData.itemCount
                        val selectableCount = maxNumber - uriList.count()
                        if (clipDataSize > selectableCount) { // 최대 선택 가능한 개수를 초과해서 선택한 경우
                            Toast.makeText(
                                this,
                                "이미지는 최대 ${selectableCount}장까지 첨부할 수 있습니다.",
                                Toast.LENGTH_SHORT
                            ).show();
                        } else {
                            // 선택 가능한 경우 ArrayList에 가져온 uri를 넣어준다.
                            for (i in 0 until clipDataSize) {
                                uriList.add(clipData.getItemAt(i).uri)
                            }
                        }
                    } else { // 이미지를 한 개만 선택할 경우 null이 올 수 있다.
                        val uri = result?.data?.data
                        if (uri != null) {
                            uriList.add(uri)
                        }
                    }
                    // notifyDataSetChanged()를 호출하여 adapter에게 값이 변경 되었음을 알려준다.
                    adapter.notifyDataSetChanged()
                    printCount()
                }
            }
        }

    private fun printCount() {
        val text = "${uriList.count()}/${maxNumber}"
        binding.countArea.text = text
    }

    private fun imageUpload(uri: Uri, count: Int) {
        // storage 인스턴스 생성
        val storage = Firebase.storage
        // storage 참조
        val storageRef = storage.getReference("images")
        // storage에 저장할 파일명 선언
        val mountainsRef = storageRef.child("${docId}_${count}.jpg")
        val uploadTask = mountainsRef.putFile(uri)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            // 파일 업로드 성공
            Toast.makeText(this, "사진 업로드 성공", Toast.LENGTH_SHORT).show();
        }.addOnFailureListener {
            // 파일 업로드 실패
            Toast.makeText(this, "사진 업로드 실패", Toast.LENGTH_SHORT).show();
        }
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
                    saveStore()
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

    fun dateToString(date: Date): String {
        val format = SimpleDateFormat("YYYY-MM-dd HH:mm:ss", Locale.KOREAN)
        return format.format(date)
    }

    fun dateToString_image(date: Date): String {
        val format = SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREAN)
        return format.format(date)
    }

    fun saveStore() {
        val uriStringList = uriList.map { it.toString() }
        val data = mapOf(
            "email" to MyApplication.email,
            "title" to binding.writeText.text.toString(),
            "date" to dateToString(Date()),
            "image_date" to dateToString_image(Date()),
            "foodTime" to binding.foodTimeText.text.toString(),
            "food" to binding.foodText.text.toString(),
//            "foodImage" to filePath,
            "company" to binding.companyText.text.toString(),
            "uid" to auth.uid,
            "where" to binding.whereText.text.toString(),
            "memo" to binding.memoText.text.toString(),
            "nickName" to binding.nickname.text.toString(),
            "count" to uriList.count().toString(),
            "uriList" to uriStringList
        )

        MyApplication.db.collection("photos")
            .add(data)
            .addOnSuccessListener {
                Log.d("TastyLog", "data firestore save ok")
                docId = it.id
                for (i in 0 until uriList.count()) {
                    imageUpload(uriList.get(i), i)
                    Log.d("TastyLog", "${uriList.get(i)}")
                    try {
                        Thread.sleep(500)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }

//                uploadImage(it.id)
                val userDocRef = MyApplication.db.collection("users").document(auth.uid.toString())
                MyApplication.db.collection("users").document("${auth.uid}")
                    .get()
                    .addOnSuccessListener {  documentSnapshot ->
                        if(documentSnapshot.exists()) {
                            val currentPoint = documentSnapshot.getLong("userPoint")
                            currentPoint?.let {
                                val updatedPoint = it + 10
                                updatePoint(userDocRef, updatedPoint)
                            }
                        }
                    }
            }
            .addOnFailureListener {
                Log.d("TastyLog", "data firestore save error")
            }
    }

    fun updatePoint(docRef: DocumentReference, updatedValue: Long) {
        val updates = hashMapOf<String, Any>(
            "userPoint" to updatedValue
        )

        docRef.update(updates)
            .addOnSuccessListener {
                // 업데이트 성공 처리
            }
            .addOnFailureListener { e ->
                // 업데이트 실패 처리
            }

    }

}