package com.example.a2023sw

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.a2023sw.MyApplication.Companion.auth
import com.example.a2023sw.databinding.ActivityPhotoDetailBinding
import java.io.File
import java.security.AccessController.getContext
import java.util.*
import kotlin.collections.ArrayList


class PhotoDetailActivity : AppCompatActivity() {

    private lateinit var bookmarkItem: MenuItem
    lateinit var binding: ActivityPhotoDetailBinding

    lateinit var file: File

    var uriList: ArrayList<Uri> = ArrayList()
    lateinit var adapter: MyDetailAdapter

    private lateinit var animatedRecyclerView: RecyclerView
    private lateinit var animatedImageView: ImageView
    private lateinit var scrollView: NestedScrollView

    private lateinit var relativeLayout1: RelativeLayout
    private lateinit var relativeLayout2: RelativeLayout
    private lateinit var relativeLayout3: RelativeLayout
    private lateinit var relativeLayout4: RelativeLayout
    private lateinit var relativeLayout5: RelativeLayout

    private lateinit var date: String
    private lateinit var docId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = com.example.a2023sw.databinding.ActivityPhotoDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)

        val toolbar = binding.toolbarBack
        setSupportActionBar(toolbar)
//        supportActionBar?.setTitle(intent.getStringExtra("date").toString())
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 뒤로가기 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back)
        supportActionBar?.setDisplayShowTitleEnabled(false)//타이틀 없애기

        date = binding.titleDate.toString()
        binding.titleDate.text = intent.getStringExtra("date")
        binding.whereText.text = intent.getStringExtra("where")
        binding.withPeopleText.text = intent.getStringExtra("company")
        binding.memoText.text = intent.getStringExtra("memo")
        binding.titleText.text = intent.getStringExtra("title")
        binding.foodTimeText.text = intent.getStringExtra("foodTime")

        docId = intent.getStringExtra("docId")!!
        val count = intent.getStringExtra("count")

        val uriStringList: ArrayList<String> = intent.getStringArrayListExtra("uriList") as ArrayList<String>
        val uriList: ArrayList<Uri> = uriStringList.map { Uri.parse(it) } as ArrayList<Uri>

        adapter = MyDetailAdapter(this, uriList)
        Log.d("TastyLog", "${uriList}")
        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        animatedRecyclerView = binding.recyclerview
//        animatedImageView = binding.recyclerview
        scrollView = binding.mainScroll

        relativeLayout1 = binding.title
        relativeLayout2 = findViewById(R.id.with_people)
        relativeLayout3 = findViewById(R.id.where)
        relativeLayout4 = findViewById(R.id.memo)
        relativeLayout5 = binding.foodTime

        // 스크롤 리스너 등록
        scrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->
            val scale = 1 - scrollY / 400f // 스크롤에 따라 크기를 조절할 비율

            animatedRecyclerView.scaleX = scale
            animatedRecyclerView.scaleY = scale

            // 스크롤에 따라 투명도를 조절할 비율
            val alpha = 1 - scrollY / 200f
            animatedRecyclerView.alpha = alpha

            animatedRecyclerView.animate()
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
                animateView(relativeLayout4, animationSpeed)
                animateView(relativeLayout5, animationSpeed)
            } else {
                // 원래 위치로 되돌리는 애니메이션
                resetView(relativeLayout1)
                resetView(relativeLayout2)
                resetView(relativeLayout3)
                resetView(relativeLayout4)
                resetView(relativeLayout5)
            }
        }

        val recyclerView: RecyclerView =
            findViewById(R.id.recyclerview)
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = MyDetailAdapter(this, uriList)
        val radius = resources.getDimensionPixelSize(R.dimen.radius)
        val dotsHeight = resources.getDimensionPixelSize(R.dimen.dots_height)
        val color = ContextCompat.getColor(this, R.color.image_dots)
        recyclerView.addItemDecoration(
            DotsIndicatorDecoration(
                radius,
                radius * 4,
                dotsHeight,
                color,
                color
            )
        )
        PagerSnapHelper().attachToRecyclerView(recyclerView)

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

    private fun setBookmarkIcon() {
        val menu = binding.toolbarBack.menu
        val bookmarkItem = menu.findItem(R.id.menu_bookmark)

        val bookmarkRef = MyApplication.db.collection("users")
            .document(auth.uid.toString())
            .collection("bookmarked_records")
            .whereEqualTo("docId", docId)

        bookmarkRef.get().addOnSuccessListener { querySnapshot ->
            val isBookmarked = !querySnapshot.isEmpty
            val bookmarkIconRes =
                if (isBookmarked) R.drawable.heart else R.drawable.heartempty
            bookmarkItem.setIcon(bookmarkIconRes)
        }
    }

    private fun toggleBookmarkStatus() {
        val bookmarkRef = MyApplication.db.collection("users")
            .document(auth.uid.toString())
            .collection("bookmarked_records")
            .whereEqualTo("docId", docId)

        bookmarkRef.get().addOnSuccessListener { querySnapshot ->
            if (querySnapshot.isEmpty) {
                saveBookmark()
            } else {
                querySnapshot.documents.firstOrNull()?.reference?.delete()
            }
            setBookmarkIcon()
        }
    }

    fun saveBookmark() {
        val uriStringList = uriList.map { it.toString() }
        val data = mapOf(
            "email" to MyApplication.email,
            "title" to binding.titleText.text.toString(),
            "foodTime" to binding.foodTimeText.text.toString(),
            "date" to binding.titleDate.text.toString(),
            "food" to binding.foodText.text.toString(),
//            "foodImage" to filePath,
            "company" to binding.withPeopleText.text.toString(),
            "uid" to auth.uid,
            "where" to binding.whereText.text.toString(),
            "memo" to binding.memoText.text.toString(),
            "count" to uriList.count().toString(),
            "uriList" to uriStringList
        )

        MyApplication.db.collection("users").document(auth.uid.toString()).collection("bookmarked_records")
            .add(data)
            .addOnSuccessListener {
                Log.d("TastyLog", "Bookmark added successfully")
            }
            .addOnFailureListener {
                Log.d("TastyLog", "Failed to add bookmark")
            }
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
//                setBookmarkIcon()
//                toggleBookmarkStatus()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}