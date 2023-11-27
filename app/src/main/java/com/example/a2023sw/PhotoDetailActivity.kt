package com.example.a2023sw

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.a2023sw.MyApplication.Companion.auth
import com.example.a2023sw.MyApplication.Companion.db
import com.example.a2023sw.databinding.ActivityPhotoDetailBinding
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import kotlin.collections.ArrayList


class PhotoDetailActivity : AppCompatActivity() {

    private lateinit var bookmarkItem: MenuItem
    lateinit var binding: ActivityPhotoDetailBinding

    lateinit var file: File

    lateinit var adapter: MyDetailAdapter

    private lateinit var docId: String
    private lateinit var count: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.MEDIA_CONTENT_CONTROL), 1)


        val toolbar = binding.toolbarBack
        setSupportActionBar(toolbar)
//        supportActionBar?.setTitle(intent.getStringExtra("date").toString())
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 뒤로가기 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back)
        supportActionBar?.setDisplayShowTitleEnabled(false)//타이틀 없애기

//        date = binding.titleDate.toString()
        binding.date.text = intent.getStringExtra("date")
        binding.whereText.text = intent.getStringExtra("where")
        binding.withPeopleText.text = intent.getStringExtra("company")
        binding.memoText.text = intent.getStringExtra("memo")
        binding.titleText.text = intent.getStringExtra("title")
        binding.foodTimeText.text = intent.getStringExtra("foodTime")
        binding.foodText.text = intent.getStringExtra("food")

        docId = intent.getStringExtra("docId")!!

        val uriStringList: ArrayList<String> = intent.getStringArrayListExtra("uriList") as ArrayList<String>
        val uriList: ArrayList<Uri> = uriStringList.map { Uri.parse(it) } as ArrayList<Uri>

        adapter = MyDetailAdapter(this, uriList)
        Log.d("TastyLog", "${uriList}")
        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerview)
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
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

        val alertHandler = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    if (docId !== null) {
                        for (i in 0 until uriList.count()) {
                            MyApplication.storage.getReference().child("images")
                                .child("${docId}_${i}.jpg")
                                .delete()
                            Log.d("TastyLog", "${docId}_${i}.jpg")
                        }
                        db.collection("photos").document("${docId}")
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "삭제가 완료되었습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this,
                                    "삭제가 실패하였습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(this, "문서가 존재하지 않습니다.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                    Log.d("ToyProject", "DialogInterface.BUTTON_NEGATIVE")
                }
            }
            dialog.dismiss() // 다이얼로그를 닫습니다.
        }

        if(MyApplication.checkAuth()){
            binding.menuBookmark.setOnClickListener {
                val bookmarkDocRef = db.collection("photos").document(docId)

                db.collection("photos").document(docId)
                    .get()
                    .addOnSuccessListener {  documentSnapshot ->
                        val currentBookmark = documentSnapshot.getString("bookmark")
                        if(currentBookmark != "1"){
                            currentBookmark?.let {
                                val updatedBookmark = "1"
                                updateBookmark(bookmarkDocRef, updatedBookmark)
                                setBookmarkIcon()
                                Toast.makeText(this,
                                    "북마크 추가되었습니다.",
                                    Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            val updatedBookmark = "0"
                            updateBookmark(bookmarkDocRef, updatedBookmark)
                            setBookmarkIcon()
                            Toast.makeText(this,
                                "북마크 해제되었습니다.",
                                Toast.LENGTH_SHORT).show()
                        }

                    }
            }
            binding.menuUpdate.setOnClickListener {
                intent = Intent(this, UpdateActivity::class.java)
                intent.putExtra("docId", docId)
                startActivity(intent)
            }
            binding.menuDelete.setOnClickListener {
                AlertDialog.Builder(this).run {
                    setTitle("정말 삭제하시겠습니까?")
                    setMessage("한 번 삭제하면 되돌릴 수 없습니다.")
                    setNegativeButton("Cancle", alertHandler)
                    setPositiveButton("Yes", alertHandler)
                    show()
                }
            }
        }

    }


    fun updateBookmark(docRef: DocumentReference, updatedValue: String) {
        val updates = hashMapOf<String, Any>(
            "bookmark" to "1"
        )

        docRef.update(updates)
            .addOnSuccessListener {
                // 업데이트 성공 처리
            }
            .addOnFailureListener { e ->
                // 업데이트 실패 처리
            }

    }

    private fun setBookmarkIcon() {
        val bookmarkRef = MyApplication.db.collection("photos")
            .document(docId)

        lifecycleScope.launch {
            try {
                val querySnapshot = bookmarkRef.get().await()

                val isBookmarked = querySnapshot.getString("bookmark")
                if (isBookmarked == "1") {
                    binding.menuBookmark.setText("저장 취소")
                } else {
                    binding.menuBookmark.setText("저장")
                }
            } catch (e: Exception) {
                // 예외 처리
            }
        }
    }


//    private fun setBookmarkIcon() {
//        val bookmarkRef = MyApplication.db.collection("photos")
//            .document(docId)
//
//        bookmarkRef.get().addOnSuccessListener { querySnapshot ->
//            val isBookmarked = querySnapshot.getString("bookmark")
//            if (isBookmarked == "1") binding.menuBookmark.setText("저장 취소")
//            else binding.menuBookmark.setText("저장")
//        }
//    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_detail, menu)
//        bookmarkItem = menu!!.findItem(R.id.menu_bookmark)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> { //뒤로 가기 버튼
                onBackPressed() // 기본 뒤로가기 동작 수행
                return true
            }
//            R.id.menu_bookmark -> {
//                if (bookmarkItem?.icon?.constantState == resources.getDrawable(R.drawable.heartempty, null).constantState) {
//                    bookmarkItem?.setIcon(R.drawable.heart)
//                    Toast.makeText(this, "북마크 추가됨", Toast.LENGTH_SHORT).show()
//                } else {
//                    bookmarkItem?.setIcon(R.drawable.heartempty)
//                    Toast.makeText(this, "북마크 해제됨", Toast.LENGTH_SHORT).show()
//                }
////                setBookmarkIcon()
////                toggleBookmarkStatus()
//                return true
//            }
//            R.id.menu_update -> {
//                intent = Intent(this, UpdateActivity::class.java)
//                intent.putExtra("docId", docId)
//                startActivity(intent)
//            }
//            R.id.menu_delete -> {
//                AlertDialog.Builder(this).run {
//                    setTitle("정말 삭제하시겠습니까?")
//                    setMessage("한 번 삭제하면 되돌릴 수 없습니다.")
//                    setNegativeButton("Cancle", alertHandler)
//                    setPositiveButton("Yes", alertHandler)
//                    show()
//                }
//            }
        }
        return super.onOptionsItemSelected(item)
    }
}