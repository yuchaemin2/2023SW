package com.example.a2023sw

import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.a2023sw.MyApplication.Companion.auth
import com.example.a2023sw.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    lateinit var binding: ActivityProfileBinding

    private lateinit var imageView: ImageView
    private var imageUrl : String? = null

    private var userPoint : Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchPoint()

        CoroutineScope(Dispatchers.Main).launch {
            imageUrl =  MyApplication.getImageUrl(MyApplication.email).toString()
            imageView = binding.userProfile
            if( imageUrl != null){
                Glide.with(this@ProfileActivity)
                    .load(imageUrl)
                    .into(binding.userProfile)
            }
        }

        if(MyApplication.checkAuth()){
            val userDocRef = MyApplication.db.collection("users").document(MyApplication.auth.uid.toString())
            MyApplication.db.collection("users").document("${MyApplication.auth.uid}")
                .get()
                .addOnSuccessListener {  documentSnapshot ->
                    if(documentSnapshot.exists()) {
                        val userNickname = documentSnapshot.getString("userNickname")
                        binding.NicknameView.text = userNickname
                        binding.nicknameText.hint = userNickname.toString()
                    }
                }
        }

        binding.saveNickname.setOnClickListener {
            val userDocRef = MyApplication.db.collection("users").document(auth.uid.toString())
            MyApplication.db.collection("users").document("${auth.uid}")
                .get()
                .addOnSuccessListener {  documentSnapshot ->
                    if(documentSnapshot.exists()) {
                        val currentNickname = documentSnapshot.getString("userNickname")
                        currentNickname?.let {
                            val updatedNickname = binding.nicknameText.text.toString()
                            updateNickname(userDocRef, updatedNickname)
                        }
                    }
                }
            finish()
        }

        var toolbar = binding.toolbarBack
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)//타이틀 없애기
    }

    private fun fetchPoint() {
        val currentUser = MyApplication.auth.currentUser
        currentUser?.let {
            val userId = currentUser.uid

            val userRef = MyApplication.db.collection("users").document(userId)
            userRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        userPoint = documentSnapshot.getLong("userPoint")!!
                        if (userPoint != null) { // 업로드 & 다이얼로그
                            binding.PointView.text = userPoint.toString()
                            openDialog(userPoint!!)

                        } else {
                            Toast.makeText(this, "사용자의 포인트를 가져오는데 실패했습니다...", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "사용자의 포인트를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    val characters = arrayOf(
        arrayOf("/profile/level_1.png", "햄버거", "맛있는 햄버거. 베이컨이 두개 들어있다."),
        arrayOf("/profile/level_2.png", "피자", "사람은 다섯명인데 피자는 여섯조각이네. 선착순 시작!"),
        arrayOf("/profile/level_3.png", "딸기 생크림 케이크", "달콤한 생크림과 상큼한 딸기가 잘 어울린다."),
        arrayOf("/profile/level_4.png", "스파게티", "나는 크림 스파게티가 좋다고...미트볼 스파게티 말고!"),
        arrayOf("/profile/level_5.png", "팬케이크", "메이플 시럽과 팬케이크의 조화가 최고야."),
        arrayOf("/profile/level_6.png", "아이스크림", "후식으로는 아이스크림 세 덩이는 먹어줘야한다."),
        arrayOf("/profile/level_7.png", "버터와플", "아직 따뜻한 와플에 버터 한 조각을 올리면 녹아내릴거야."),
        arrayOf("/profile/level_8.png", "핫도그", "야구 경기를 보러 갈때 자주 먹었지."),
        arrayOf("/profile/level_9.png", "감자튀김", "감자튀김은 케찹에 많이 먹지만... 개인적으로는 마요네즈가 좋다고 생각해."),
        arrayOf("/profile/level_10.png", "통닭", "따뜻한 통닭은 이 세상 무엇과도 비교할 수 없다."),
        arrayOf("/profile/level_11.png", "도넛", "도넛은 초콜릿 도넛이지!"),
        arrayOf("/profile/level_12.png", "샌드위치", "베이컨, 상추, 토마토만 있다면 어디든 갈 수 있어.")
    )


    val imgResourceIds = arrayOf(
        R.drawable.level_1,
        R.drawable.level_2,
        R.drawable.level_3,
        R.drawable.level_4,
        R.drawable.level_5,
        R.drawable.level_6,
        R.drawable.level_7,
        R.drawable.level_8,
        R.drawable.level_9,
        R.drawable.level_10,
        R.drawable.level_11,
        R.drawable.level_12,
    )

    fun openDialog(Point: Long) {
        val btnLevels = arrayOf(
            binding.level1,
            binding.level2,
            binding.level3,
            binding.level4,
            binding.level5,
            binding.level6,
            binding.level7,
            binding.level8,
            binding.level9,
            binding.level10,
            binding.level11,
            binding.level12,

        )

        // 모든 버튼에 리스너 설정
        for (i in 0 until 11) {
            btnLevels[i].setOnClickListener {
                // 해당 버튼 클릭 이벤트 처리

                AlertDialog.Builder(this).run {
                    setTitle(characters[i][1])
                    setMessage(characters[i][2]) // 여기 함수로 작성
                    setPositiveButton("프로필 구입하기") { dialog, id ->
                        val userDocRef = MyApplication.db.collection("users").document(auth.uid.toString())
                        MyApplication.db.collection("users").document("${auth.uid}")
                            .get()
                            .addOnSuccessListener {  documentSnapshot ->
                                if(documentSnapshot.exists()) {
                                    val currentPoint = documentSnapshot.getLong("userPoint")
                                    if (currentPoint != null) {
                                        if(currentPoint >= 10){
                                            upLoadProfileImg(characters[i][0]) // 이미지 파일 전달 해줘야함
                                            binding.userProfile.setImageResource(imgResourceIds[i])
                                            currentPoint?.let {
                                                val updatedPoint = it - 30
                                                updatePoint(userDocRef, updatedPoint)
                                            }
                                        } else{
                                            Toast.makeText(this@ProfileActivity, "포인트가 부족합니다! 기록을 더 작성해주세요.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }

                    }
                    setNegativeButton("닫기", alertHandler)
                    show()
                }
            }
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

    fun updateNickname(docRef: DocumentReference, updatedValue: String) {
        val updates = hashMapOf<String, Any>(
            "userNickname" to updatedValue
        )

        docRef.update(updates)
            .addOnSuccessListener {
                // 업데이트 성공 처리
            }
            .addOnFailureListener { e ->
                // 업데이트 실패 처리
            }

    }

    fun upLoadProfileImg(strImg : String) {
        // Firebase Storage의 파일 참조 가져오기
        val storageReference =
            FirebaseStorage.getInstance().getReference(strImg)
        Log.d(ContentValues.TAG, "해당 imageUrl${storageReference}")
        // 파일의 다운로드 URL 가져오기
        storageReference.downloadUrl.addOnSuccessListener { uri ->
            val downloadUrl = uri.toString()
            Log.d(ContentValues.TAG, "해당 imageUrl${downloadUrl}")
            // 유저 프로필에 저장하기
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            Log.d(ContentValues.TAG, "해당 아이디 ${userId}")
//            Toast.makeText(context, "base${userId}", Toast.LENGTH_SHORT).show()
            if (userId != null) {
                val userDocumentRef =
                    FirebaseFirestore.getInstance().collection("users").document(userId)

                userDocumentRef.get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val user = documentSnapshot.toObject(UserModel::class.java)

                            if (user != null) {
                                // 이미지 URL을 사용하여 UserModel 업데이트
                                user.imageUrl = downloadUrl

                                // Firestore의 users 컬렉션 업데이트
                                userDocumentRef.set(user, SetOptions.merge())
                                    .addOnSuccessListener {
                                        Glide.with(baseContext)
                                            .load(downloadUrl)
                                            .into(binding.userProfile)  // yourImageView는 이미지를 표시할 ImageView입니다.

                                    }
                                    .addOnFailureListener { exception ->
                                        Log.e(ContentValues.TAG, "사용자 정보 업데이트 중 오류 발생: $exception")
                                    }
                            }
                        } else {
                            Log.d(ContentValues.TAG, "사용자 문서가 존재하지 않습니다.")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e(ContentValues.TAG, "사용자 데이터 가져오기 중 오류 발생: $exception")
                    }
            }

            Log.d(ContentValues.TAG, "다운로드 URL: $downloadUrl")
        }.addOnFailureListener { exception ->
            // 다운로드 URL을 가져오는 도중에 오류가 발생한 경우 처리
            Log.e(ContentValues.TAG, "다운로드 URL을 가져오는 중 오류 발생: $exception")
        }
    }



    val alertHandler = object: DialogInterface.OnClickListener {
        override fun onClick(dialog: DialogInterface?, which: Int) {
            when(which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    Log.d("ToyProject", "DialogInterface.BUTTON_POSITIVE")
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                    Log.d("ToyProject", "DialogInterface.BUTTON_NEGATIVE")
                }
                DialogInterface.BUTTON_NEUTRAL -> {
                    Log.d("ToyProject", "DialogInterface.BUTTON_NEUTRAL")
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_profile, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_cancle -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
