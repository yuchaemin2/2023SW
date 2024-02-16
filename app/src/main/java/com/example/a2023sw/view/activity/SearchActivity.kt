package com.example.a2023sw.view.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.a2023sw.MyApplication
import com.example.a2023sw.MyApplication.Companion.auth
import com.example.a2023sw.R
import com.example.a2023sw.databinding.ActivitySearchBinding
import com.example.a2023sw.model.ItemPhotoModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class SearchActivity : AppCompatActivity() {
    lateinit var binding: ActivitySearchBinding
    var firestore : FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        binding= ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 파이어스토어 인스턴스 초기화
        firestore = FirebaseFirestore.getInstance()

        binding.recyclerview.adapter = RecyclerViewAdapter()
        binding.recyclerview.layoutManager = GridLayoutManager(this,3)

        // 검색 옵션 변수
        var searchOption = "title"

        // 스피너 옵션에 따른 동작
        binding.spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (binding.spinner.getItemAtPosition(position)) {
                    "제목" -> {
                        searchOption = "title"
                    }
                    "음식" -> {
                        searchOption = "food"
                    }
                    "함께한사람" -> {
                        searchOption = "company"
                    }
                    "시간대" -> {
                        searchOption = "foodTime"
                    }
                    "장소" -> {
                        searchOption = "where"
                    }
                }
            }
        }

        // 검색 옵션에 따라 검색
        binding.searchWord.setOnEditorActionListener { view, actionId, event ->
            when(actionId){
                EditorInfo.IME_ACTION_SEARCH -> {
                    (binding.recyclerview.adapter as RecyclerViewAdapter).search(binding.searchWord.text.toString(), searchOption)
                    true
                }
                else -> false
            }
        }

        binding.btnAdd.setOnClickListener{
            intent = Intent(this, AddActivity::class.java)
            startActivity(intent)
        }

        binding.back.setOnClickListener {
            onBackPressed()
        }
    }

    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var itemList : ArrayList<ItemPhotoModel> = arrayListOf()

        init {
            if(MyApplication.checkAuth()){
                MyApplication.db.collection("photos")
                    .whereEqualTo("uid", auth.uid)
                    .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    // ArrayList 비워줌
                    itemList.clear()

                    for (snapshot in querySnapshot!!.documents) {
                        val item = snapshot.toObject(ItemPhotoModel::class.java)
                        item!!.docId = snapshot.id // Assuming you have a variable named docId in your ItemPhotoModel
                        itemList.add(item!!)
                    }
                    notifyDataSetChanged()
                }
            }
        }

        // xml파일을 inflate하여 ViewHolder를 생성
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
            return ViewHolder(view)
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        }

        // onCreateViewHolder에서 만든 view와 실제 데이터를 연결
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val viewHolder = (holder as ViewHolder).itemView
            val data = itemList.get(position)

            viewHolder.findViewById<TextView>(R.id.itemTitleView).text = itemList[position].title
            viewHolder.findViewById<TextView>(R.id.itemFoodView).text = itemList[position].food
            viewHolder.findViewById<TextView>(R.id.itemCompanyView).text = itemList[position].company
            viewHolder.findViewById<TextView>(R.id.itemFoodTimeView).text = itemList[position].foodTime
            viewHolder.findViewById<TextView>(R.id.itemWhereView).text = itemList[position].where

            Log.d("TastyLog", "data: ${data.docId}, itemList: ${itemList[position].docId} - first")

            // Load image using Glide
            val itemFoodImageView = viewHolder.findViewById<ImageView>(R.id.itemFoodImageView)
            val imageRef = MyApplication.storage.reference.child("images/${itemList[position].docId}_0.jpg")
            imageRef.downloadUrl.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Glide.with(this@SearchActivity)
                        .load(task.result)
                        .apply(RequestOptions().override(120, 120).centerCrop())
                        .into(itemFoodImageView)
                } else {
                    // Handle the case where the file doesn't exist
                    Log.e("TastyLog", "File not found: ${task.exception}")
                }
            }

            Log.d("TastyLog", "data: ${data.docId}, itemList: ${itemList[position].docId}")

            viewHolder.findViewById<ImageView>(R.id.itemFoodImageView).setOnClickListener{
                val bundle: Bundle = Bundle()
                bundle.putString("docId", data.docId)
                bundle.putString("email", data.email)
                bundle.putString("title", data.title)
                bundle.putString("date", data.date)
                bundle.putString("foodTime", data.foodTime)
                bundle.putString("food", data.food)
                bundle.putString("company", data.company)
                bundle.putString("userEmail", data.email)
                bundle.putString("where", data.where)
                bundle.putString("foodImage", data.foodImage)
                bundle.putString("memo", data.memo)
                bundle.putString("nickName", data.nickName)
                bundle.putString("bookmark", data.bookmark)
                bundle.putStringArrayList("uriList", data.uriList)

                Log.d("TastyLog", "data: ${data.docId}, itemList: ${itemList[position].docId}")

                Intent(this@SearchActivity, PhotoDetailActivity::class.java).apply{
                    putExtras(bundle)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run {
                    this@SearchActivity.startActivity(this)
                }
            }
        }

        // 리사이클러뷰의 아이템 총 개수 반환
        override fun getItemCount(): Int {
            return itemList.size
        }

        // 파이어스토어에서 데이터를 불러와서 검색어가 있는지 판단
        fun search(searchWord : String, option : String) {
            if(MyApplication.checkAuth()){
                MyApplication.db.collection("photos")
                    .whereEqualTo("uid", auth.uid)
                    .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    // ArrayList 비워줌
                    itemList.clear()

                    for (snapshot in querySnapshot!!.documents) {
                        if (snapshot.getString(option)!!.contains(searchWord)) {
                            val item = snapshot.toObject(ItemPhotoModel::class.java)
                            item!!.docId = snapshot.id
                            itemList.add(item)
                        }
                        if(itemList.size.equals(0)){
                            binding.textView.visibility = View.VISIBLE
                            binding.btnAdd.visibility = View.VISIBLE
                        }
                        else{
                            binding.textView.visibility = View.GONE
                            binding.btnAdd.visibility = View.GONE
                        }
                    }
                    notifyDataSetChanged()
                }
            }
        }
    }
}
