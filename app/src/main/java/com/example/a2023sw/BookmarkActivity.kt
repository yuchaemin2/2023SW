package com.example.a2023sw

import android.app.AlertDialog
import android.graphics.Movie
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a2023sw.MyApplication.Companion.auth
import com.example.a2023sw.MyApplication.Companion.db
import com.example.a2023sw.databinding.ActivityBookmarkBinding

class BookmarkActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookmarkBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookmarkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db.collection("photos")
            .whereEqualTo("bookmark", "1")
            .get()
            .addOnSuccessListener { result ->
                val itemList = mutableListOf<ItemPhotoModel>()
                for(document in result){
                    val item = document.toObject(ItemPhotoModel::class.java)
                    if(MyApplication.email.equals(item.email)){
                        val uriStringList: ArrayList<String> = item.uriList as ArrayList<String>
                        val uriList: ArrayList<Uri> = uriStringList.map { Uri.parse(it) } as ArrayList<Uri>

                        item.docId = document.id
                        itemList.add(item)
                    }
                    binding.feedRecyclerView.layoutManager = GridLayoutManager(this,3)
                    binding.feedRecyclerView.adapter = MyPhotoAdapter(this, itemList)
                }
                result.size()
                if(result.size() == 0){
                    binding.textView.visibility = View.VISIBLE
                }
                Log.d("TastyLog", "${result.size()}")
            }.addOnFailureListener { exception ->
                // Handle any errors
                Toast.makeText(this, "데이터 획득 실패", Toast.LENGTH_SHORT).show()
            }

        var toolbar = binding.toolbarBack
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 뒤로가기 버튼 활성화
        supportActionBar?.setDisplayShowTitleEnabled(false)//타이틀 없애기
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_list, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> { //뒤로 가기 버튼
                onBackPressed() // 기본 뒤로가기 동작 수행
                return true
            }
            R.id.menu_delete_all -> {
                showDeleteConfirmationDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteAllUserLikedMovie(userEmail: String) {
        val currentUser = auth.currentUser

        currentUser?.let {
            db.collection("photos")
                .whereEqualTo("bookmark", "1")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (documentSnapshot in querySnapshot.documents) {
                        val docId = documentSnapshot.id
                        MyApplication.db.collection("users").document(auth.uid.toString()).collection("bookmarked_records").document(docId)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(this, "북마크가 모두 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                refreshRecyclerView()
                            }
                            .addOnFailureListener {  }
//                        isRemoving
                    }
                }
                .addOnFailureListener {  }
        }
    }

    private fun refreshRecyclerView() {
        // 리사이클러뷰를 새로고침하고 데이터를 다시 로드
        val currentUser = auth.currentUser

        currentUser?.let {
            db.collection("users").document(auth.uid.toString())
                .collection("bookmarked_records")
                .get()
                .addOnSuccessListener { result ->
                    val itemList = mutableListOf<ItemPhotoModel>()
                    for(document in result){
                        val item = document.toObject(ItemPhotoModel::class.java)
                        if(MyApplication.email.equals(item.email)){
                            val uriStringList: ArrayList<String> = item.uriList as ArrayList<String>
                            val uriList: ArrayList<Uri> = uriStringList.map { Uri.parse(it) } as ArrayList<Uri>

                            item.docId = document.id
                            itemList.add(item)
                        }
                    }
                    binding.feedRecyclerView.layoutManager = GridLayoutManager(this,3)
                    binding.feedRecyclerView.adapter = MyPhotoAdapter(this, itemList)
                }
                .addOnFailureListener{
                    Toast.makeText(this, "데이터 획득 실패", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("정말로 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                deleteAllUserLikedMovie(MyApplication.email.toString())
            }
            .setNegativeButton("취소") { _, _ ->
                // "취소" 버튼 클릭 시, 아무 동작 없음
            }
            .create()
            .show()
    }
}