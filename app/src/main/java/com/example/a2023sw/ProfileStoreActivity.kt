package com.example.a2023sw

import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import com.bumptech.glide.Glide
import com.example.a2023sw.MyApplication.Companion.auth
import com.example.a2023sw.MyApplication.Companion.db
import com.example.a2023sw.databinding.ActivityProfileStoreBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileStoreActivity : AppCompatActivity() {
    lateinit var binding: ActivityProfileStoreBinding

    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileStoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchPoint()

        imageView = binding.userProfile

        CoroutineScope(Dispatchers.Main).launch {
            val imageUrl =  MyApplication.getImageUrl(MyApplication.email).toString()
            imageView = binding.userProfile
            if( imageUrl != null){
                Glide.with(baseContext)
                    .load(imageUrl)
                    .into(binding.userProfile)
            }
        }
    }

    private fun fetchPoint() {
        val currentUser = auth.currentUser
        var userPoint :Long? = null
        currentUser?.let {
            val userId = currentUser.uid

            val userRef = db.collection("users").document(userId)
            userRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        userPoint = documentSnapshot.getLong("userPoint")
                        if (userPoint != null) { // 업로드 & 다이얼로그
                            binding.PointView.text = userPoint.toString()
                            openDialog(userPoint!!)

                        } else {
                            Toast.makeText(this, "사용자의 포인트를 가져오는데 실패했습니다...", Toast.LENGTH_SHORT).show()
                        }
//                        Toast.makeText(requireContext(), "사용자의 레벨은 ${userLevel}입니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "사용자의 포인트를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    val characters = arrayOf(
        arrayOf("/profile/level_1.png", "햄버거", "하품하고 피곤해 보이는 고양이다."),
        arrayOf("/profile/level_2.png", "피자", "털을 그루밍하는 고양이다."),
        arrayOf("/profile/level_3.png", "딸기 생크림 케이크", "호기심 가득한 눈으로 주변을 늘 경계하는 고양이다."),
        arrayOf("/profile/level_4.png", "스파게티", "졸려서 하품하는 검정색 고양이다."),
        arrayOf("/profile/level_5.png", "팬케이크", "독특한 삼색 모습으로 사람들의 관심을 끄는 고양이다."),
        arrayOf("/profile/level_6.png", "아이스크림", "눈을 반쯤 감고 먼 곳을 응시하는 듯한 고요한 고양이다."),
        arrayOf("/profile/level_7.png", "버터와플", "까칠한 느낌으로 주변을 바라보는 고양이다."),
        arrayOf("/profile/level_8.png", "핫도그", "따뜻한 햇살 아래에서 편안함을 느끼는 고양이다."),
        arrayOf("/profile/level_9.png", "감자튀김", "마치 미소를 짓는 듯한 표정으로 귀여움을 뽐내는 고양이다.")
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
        R.drawable.level_9
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
            binding.level9
        )

        // 모든 버튼에 리스너 설정
        for (i in 0 until 9) {
            btnLevels[i].setOnClickListener {
                // 해당 버튼 클릭 이벤트 처리

                AlertDialog.Builder(this).run {
                    setTitle(characters[i][1])
                    setMessage(characters[i][2]) // 여기 함수로 작성
                    setPositiveButton("프로필 구입하기") { dialog, id ->
                        upLoadProfileImg(characters[i][0]) // 이미지 파일 전달 해줘야함
                        binding.userProfile.setImageResource(imgResourceIds[i])
                    }
                    setNegativeButton("닫기", alertHandler)
                    show()
                }
            }
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

}