package com.example.a2023sw.view.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.a2023sw.DotsIndicatorDecoration
import com.example.a2023sw.MyApplication
import com.example.a2023sw.MyApplication.Companion.db
import com.example.a2023sw.MyPhotoAdapter
import com.example.a2023sw.R
import com.example.a2023sw.databinding.ActivityPhotoDetailBinding
import com.example.a2023sw.model.ItemPhotoModel
import com.example.a2023sw.view.adapter.MyDetailAdapter
import com.example.a2023sw.view.dialog.CaptureDialog
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.SimpleDateFormat


class PhotoDetailActivity : AppCompatActivity() {

    private lateinit var bookmarkItem: MenuItem
    lateinit var binding: ActivityPhotoDetailBinding

    lateinit var file: File

    lateinit var adapter: MyDetailAdapter

    private lateinit var docId: String
    private lateinit var title: String
    private lateinit var date: String

    private val dbFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")

    val MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.MEDIA_CONTENT_CONTROL), 1)
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)

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

        if(intent.getStringExtra("where")?.isEmpty() == true){
            binding.whereText.visibility = View.GONE
        }
        if(intent.getStringExtra("company")?.isEmpty() == true){
            binding.withPeopleText.visibility = View.GONE
        }
        if(intent.getStringExtra("memo")?.isEmpty() == true){
            binding.memo.visibility = View.GONE
        }

        docId = intent.getStringExtra("docId")!!
        title =intent.getStringExtra("title")!!
        date = intent.getStringExtra("date")!!

        val isBookmarked = intent.getStringExtra("bookmark")
        Log.d("TastyLog", "북마크 현황: ${isBookmarked}")
        if (isBookmarked == "1") {
            binding.menuBookmark.setText("저장 취소")
        } else {
            binding.menuBookmark.setText("저장")
        }

//        // 로딩창 객체 생성
//        customProgressDialog = CaptureDialog(this)
//        // 로딩창을 투명하게
//        customProgressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        customProgressDialog.setCancelable(true)

        val imageRef = MyApplication.storage.reference.child("images/${docId}_0.jpg")


        binding.captureDialog.setOnClickListener {
//            val intent = Intent(this, CaptureDialog::class.java)
//            startActivity(intent)
//            val captureDialog = CaptureDialog()
//            captureDialog.callCaptureDialog(
//                date,
//                getImageFromStorageReference(imageRef),
//                title
//            )

            val imageRef = MyApplication.storage.reference.child("images/${docId}_0.jpg")
            val captureDialog = CaptureDialog(this)  // Pass the context of PhotoDetailActivity
            checkExternalStoragePermission()
            captureDialog.callCaptureDialog(this, date, imageRef, title)
//            customProgressDialog.show()
        }

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
                        if(currentBookmark == "0"){
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

    // 외부 저장소 읽기 권한을 체크하고 요청하는 함수
    private fun checkExternalStoragePermission() {
        // 권한이 부여되어 있는지 체크
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 사용자에게 권한 요청
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE
            )
        } else {
            // 이미 권한이 부여되어 있는 경우 외부 저장소에 접근할 수 있습니다.
            // 이곳에서 외부 저장소에 대한 동작을 수행하면 됩니다.
        }
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE -> {
                // 권한 요청에 대한 응답을 받았을 때 처리
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // 권한이 부여되면 외부 저장소에 접근할 수 있습니다.
                    // 이곳에서 외부 저장소에 대한 동작을 수행하면 됩니다.
                } else {
                    // 권한이 거부되었을 경우 처리
                    // 사용자에게 권한이 필요한 이유를 설명하거나 다른 대체 동작을 수행할 수 있습니다.
                }
                return
            }
        }
    }

    fun updateBookmark(docRef: DocumentReference, updatedValue: String) {
        val updates = hashMapOf<String, Any>(
            "bookmark" to updatedValue
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