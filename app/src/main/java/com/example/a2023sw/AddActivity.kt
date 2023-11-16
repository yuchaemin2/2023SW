package com.example.a2023sw

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.a2023sw.MyApplication.Companion.auth
import com.example.a2023sw.databinding.ActivityAddBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddBinding
    lateinit var filePath: String

    lateinit var selectedImageUri: Uri

    private var PICK_IMAGE_REQUEST_CODE = 1;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val requestGalleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            try{
                val calRatio = calculateInSampleSize(it.data!!.data!!,
                    resources.getDimensionPixelSize(R.dimen.imgSize),
                    resources.getDimensionPixelSize(R.dimen.imgSize) )
                val option = BitmapFactory.Options()
                option.inSampleSize = calRatio
                var inputStream = contentResolver.openInputStream(it.data!!.data!!)
                val bitmap = BitmapFactory.decodeStream(inputStream, null, option)
                inputStream!!.close()
                inputStream = null

                selectedImageUri = it.data!!.data!!

                bitmap?.let {
                    binding.foodImage.setImageBitmap(bitmap)
                } ?: let{ Log.d("TastyLog", "bitmap NULL")}
            } catch(e:Exception) { e.printStackTrace() }

            if(it.resultCode === android.app.Activity.RESULT_OK) {
//                Glide
//                    .with(applicationContext)
//                    .load(it.data?.data)
//                    .apply(RequestOptions().override(150,230))
//                    .centerCrop()
//                    .into(binding.addImageView)
                val cursor = contentResolver.query(it.data?.data as Uri, arrayOf<String>(MediaStore.Images.Media.DATA), null, null, null)
                cursor?.moveToFirst().let{
                    filePath = cursor?.getString(0) as String
                    Log.d("TastyLog", "${filePath}")
                }
            }
        }

        binding.foodImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            requestGalleryLauncher.launch(intent)
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
            "foodTime" to binding.foodTimeText.toString(),
            "food" to binding.foodText.text.toString(),
            "foodImage" to selectedImageUri.toString(),
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


    fun uploadImage(selectedImageUri:String){
        val storage = MyApplication.storage
        val storageRef = storage.reference
        val imageRef = storageRef.child("images/${selectedImageUri}.jpg")
        val file = Uri.fromFile(File(filePath))
        imageRef.putFile(file)
            .addOnSuccessListener {
                Log.d("TastyLog", "imageRef.putFile(file) - addOnSuccessListener")
                finish()
            }
            .addOnFailureListener {
                Log.d("TastyLog", "imageRef.putFile(file) - addOnFailureListener")
            }

    }
}