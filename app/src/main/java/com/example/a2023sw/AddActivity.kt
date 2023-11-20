package com.example.a2023sw

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.a2023sw.MyApplication.Companion.auth
import com.example.a2023sw.databinding.ActivityAddBinding
import com.google.firebase.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddBinding
    lateinit var filePath: String

    private lateinit var imageView: ImageView
    private var imageUrl : String? = null

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

        val requestLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode === android.app.Activity.RESULT_OK) {
                Glide
                    .with(applicationContext)
                    .load(it.data?.data)
                    .apply(RequestOptions().override(120,120))
                    .centerCrop()
                    .into(binding.foodImage)
                val cursor = contentResolver.query(it.data?.data as Uri, arrayOf<String>(MediaStore.Images.Media.DATA), null, null, null)
                cursor?.moveToFirst().let{
                    filePath = cursor?.getString(0) as String
                    Log.d("ToyProject", "${filePath}")
                }
            }
        }

        binding.foodImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            requestLauncher.launch(intent)
        }

//        binding.nickname.text =

        var toolbar = binding.toolbarBack
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 뒤로가기 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back)
        supportActionBar?.setDisplayShowTitleEnabled(false)//타이틀 없애기
    }

    private fun calculateInSampleSize(fileUri: Uri, reqWidth: Int, reqHeight: Int): Int {
        val options = BitmapFactory.Options()

        options.inJustDecodeBounds = true
        try {
            var inputStream = contentResolver.openInputStream(fileUri)

            //inJustDecodeBounds 값을 true 로 설정한 상태에서 decodeXXX() 를 호출.
            //로딩 하고자 하는 이미지의 각종 정보가 options 에 설정 된다.
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream!!.close()
            inputStream = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //비율 계산........................
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        //inSampleSize 비율 계산
        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
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
                    finish()
                } else {
                    Toast.makeText(this, "내용을 입력해주세요..", Toast.LENGTH_SHORT).show()
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
//            "foodImage" to filePath,
            "company" to binding.companyText.text.toString(),
            "uid" to auth.uid,
            "where" to binding.whereText.text.toString(),
            "memo" to binding.memoText.text.toString(),
            "nickName" to binding.nickname.text.toString(),
        )

        MyApplication.db.collection("photos")
            .add(data)
            .addOnSuccessListener {
                Log.d("TastyLog", "data firestore save ok")
                uploadImage(it.id)
            }
            .addOnFailureListener {
                Log.d("TastyLog", "data firestore save error")
            }
    }

//    fun uploadImage(docId:String){
//        val storage = MyApplication.storage
//        val storageRef = storage.reference
//
//        val imageRef = storageRef.child("images/${docId}.jpg")
//        val file = Uri.fromFile(File(filePath))
//        imageRef.putFile(file)
//            .addOnSuccessListener {
//                Log.d("TastyLog", "imageRef.putFile(file) - addOnSuccessListener")
//                finish()
//            }
//            .addOnFailureListener {
//                Log.d("TastyLog", "imageRef.putFile(file) - addOnFailureListener")
//            }
//    }

    private fun uploadImage(docId: String){
        //add............................
        val storage = MyApplication.storage
        // 스토리지를 참조하는 StorageReference 생성
        val storageRef: StorageReference = storage.reference
        // 실제 업로드하는 파일을 참조하는 StorageReference 생성
        val imgRef: StorageReference = storageRef.child("images/${docId}.jpg")
        // 파일 업로드
        var fileUri = Uri.fromFile(File(filePath))
        Log.d("kkang", "File URI: $fileUri")

        imgRef.putFile(fileUri)
            .addOnFailureListener { exception ->
                Toast.makeText(this, "이미지 업로드 실패...", Toast.LENGTH_SHORT).show()
                Log.d("kkang", "Failure uploading file: $exception")
            }
            .addOnSuccessListener {
                Toast.makeText(this, "데이터가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

}