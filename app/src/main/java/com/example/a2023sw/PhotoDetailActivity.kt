package com.example.a2023sw

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.a2023sw.databinding.ActivityPhotoDetailBinding
import java.io.File
import java.util.*

class PhotoDetailActivity : AppCompatActivity() {

    lateinit var binding: ActivityPhotoDetailBinding

    lateinit var file: File

    private lateinit var animatedImageView: ImageView
    private lateinit var scrollView: NestedScrollView

    private lateinit var relativeLayout1: RelativeLayout
    private lateinit var relativeLayout2: RelativeLayout
    private lateinit var relativeLayout3: RelativeLayout

    private var bookmarkItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = com.example.a2023sw.databinding.ActivityPhotoDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.toolbarBack
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle(intent.getStringExtra("date"))
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 뒤로가기 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back)
        supportActionBar?.setDisplayShowTitleEnabled(false)//타이틀 없애기

        binding.whereText.text = intent.getStringExtra("where")
        binding.withPeopleText.text = intent.getStringExtra("company")
        binding.memoText.text = intent.getStringExtra("memo")
        binding.titleText.text = intent.getStringExtra("title")

//        val foodImage = intent.getStringExtra("foodImage")
        val docId = intent.getStringExtra("docId")
//        if(foodImage != null && foodImage != "null"){
//            Glide.with(baseContext)
//                .load(foodImage)
//                .into(binding.photoDetail)
//        }

        val imageRef = MyApplication.storage.reference.child("images/${docId}.jpg")
        imageRef.downloadUrl.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Glide.with(this)
                    .load(task.result)
//                    .apply(RequestOptions().override(350, 550).centerInside())
                    .into(binding.photoDetail)
            }
        }

        animatedImageView = binding.photoDetail
        scrollView = binding.mainScroll

        relativeLayout1 = findViewById(R.id.with_people)
        relativeLayout2 = findViewById(R.id.where)
        relativeLayout3 = findViewById(R.id.memo)

        // 스크롤 리스너 등록
        scrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->
            val scale = 1 - scrollY / 400f // 스크롤에 따라 크기를 조절할 비율

            animatedImageView.scaleX = scale
            animatedImageView.scaleY = scale

            // 스크롤에 따라 투명도를 조절할 비율
            val alpha = 1 - scrollY / 200f
            animatedImageView.alpha = alpha

            animatedImageView.animate()
                .setDuration(10000) // Adjust the duration (e.g., set to 500 milliseconds for a slower animation)
                .start()
        }

        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val scrollY = scrollView.scrollY

            // 상수값은 애니메이션이 얼마나 빠르게 동작할지를 결정합니다.
            val animationSpeed = 0.5f

            // 이미지뷰가 사라질 때까지의 스크롤 영역
            val threshold = 200

            // 스크롤이 일정 영역 이상이면 애니메이션을 적용
            if (scrollY > threshold) {
                animateView(relativeLayout1, animationSpeed)
                animateView(relativeLayout2, animationSpeed)
                animateView(relativeLayout3, animationSpeed)
            } else {
                // 원래 위치로 되돌리는 애니메이션
                resetView(relativeLayout1)
                resetView(relativeLayout2)
                resetView(relativeLayout3)
            }
        }
    }

    private fun animateView(view: View, speed: Float) {
        view.animate()
            .translationY(-view.height * speed * 10)
            .setDuration(200)
            .start()
    }

    private fun resetView(view: View) {
        view.animate()
            .translationY(0f)
            .setDuration(400)
            .start()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_detail, menu)
        bookmarkItem = menu!!.findItem(R.id.menu_bookmark)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> { //뒤로 가기 버튼
                onBackPressed() // 기본 뒤로가기 동작 수행
                return true
            }
            R.id.menu_bookmark -> {
                if (bookmarkItem?.icon?.constantState == resources.getDrawable(R.drawable.heartempty, null).constantState) {
                    bookmarkItem?.setIcon(R.drawable.heart)
                    Toast.makeText(this, "북마크 추가됨", Toast.LENGTH_SHORT).show()
                } else {
                    bookmarkItem?.setIcon(R.drawable.heartempty)
                    Toast.makeText(this, "북마크 해제됨", Toast.LENGTH_SHORT).show()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}