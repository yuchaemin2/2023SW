package com.example.a2023sw

import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a2023sw.databinding.ActivityPhotoDetailBinding
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import java.io.File

class PhotoDetailActivity : AppCompatActivity() {

    lateinit var binding: ActivityPhotoDetailBinding

    lateinit var file: File

    lateinit var adapter: ImageAdapter

    private lateinit var animatedRecyclerView: RecyclerView
    private lateinit var animatedImageView: ImageView
    private lateinit var scrollView: NestedScrollView

    private lateinit var relativeLayout1: RelativeLayout
    private lateinit var relativeLayout2: RelativeLayout
    private lateinit var relativeLayout3: RelativeLayout
    private lateinit var relativeLayout4: RelativeLayout
    private lateinit var relativeLayout5: RelativeLayout

    private var bookmarkItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = com.example.a2023sw.databinding.ActivityPhotoDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uriList = intent.getParcelableArrayListExtra<Uri>("uriList")

        val toolbar = binding.toolbarBack
        setSupportActionBar(toolbar)
//        supportActionBar?.setTitle(intent.getStringExtra("date").toString())
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 뒤로가기 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back)
        supportActionBar?.setDisplayShowTitleEnabled(false)//타이틀 없애기

        binding.titleDate.text = intent.getStringExtra("date")
        binding.whereText.text = intent.getStringExtra("where")
        binding.withPeopleText.text = intent.getStringExtra("company")
        binding.memoText.text = intent.getStringExtra("memo")
        binding.titleText.text = intent.getStringExtra("title")

//        if(intent.getStringExtra("where") == null){
//            binding.where.visibility = View.GONE
//        } else if(intent.getStringExtra("company") == null){
//            binding.withPeople.visibility = View.GONE
//        } else if ( intent.getStringExtra("memo") == null ){
//            binding.memo.visibility = View.GONE
//        } else if ( intent.getStringExtra("title") == null){
//            binding.title.visibility = View.GONE
//        }

        val docId = intent.getStringExtra("docId")
        val date = intent.getStringExtra("date")

//        val imageRef = MyApplication.storage.reference.child("images/${docId}.jpg")
//        imageRef.downloadUrl.addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                Glide.with(this)
//                    .load(task.result)
////                    .apply(RequestOptions().override(350, 550).centerInside())
//                    .into(binding.recyclerview)
//            }
//        }


//        // RecyclerView에 Adapter 연결하기
//        adapter = ImageAdapter(this, uriList!!)
//        binding.recyclerview.adapter = adapter
//        // LinearLayoutManager을 사용하여 수평으로 아이템을 배치한다.
//        binding.recyclerview.layoutManager =
//            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        if(MyApplication.checkAuth()) {
            val storage = Firebase.storage
            val storageRef = storage.getReference("images")
            if (storageRef == null) {
                Toast.makeText(this, "저장소에 사진이 없습니다.", Toast.LENGTH_SHORT).show();
            } else {
//                val mountainsRef = storageRef.child("${date}_0.jpg")
//                mountainsRef.downloadUrl.addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        Glide.with(this)
//                            .load(task.result)
//                            .into(binding.recyclerview)
//                    }
//                }
            }
        }

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