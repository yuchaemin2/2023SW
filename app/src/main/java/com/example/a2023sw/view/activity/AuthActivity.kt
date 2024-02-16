package com.example.a2023sw.view.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.a2023sw.MyApplication
import com.example.a2023sw.MyApplication.Companion.email
import com.example.a2023sw.R
import com.example.a2023sw.databinding.ActivityAuthBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.scwang.wave.MultiWaveHeader
import java.util.regex.Pattern

class AuthActivity : AppCompatActivity() {
    lateinit var binding: ActivityAuthBinding

    val emailValidation = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(MyApplication.checkAuth()){
            changeVisibility("login")
        } else {
            changeVisibility("logout")
        }

        binding.goSignInBtn.setOnClickListener{
            // 회원가입
            changeVisibility("signin")
        }

        //객체 생성
        val messageEdit: EditText = binding.authEmailEditView
        val messageEdit2: EditText = binding.authPasswordEditView
        val messageBtn: Button = binding.signBtn
        val messageBtn2: Button = binding.loginBtn

        //메시지 담을 변수
        var message: String = ""
        var message2: String = ""

        //버튼 비활성화
        messageBtn.isEnabled = false
        messageBtn2.isEnabled = false
        messageBtn.setBackgroundResource(R.drawable.button_round_grey)
        messageBtn2.setBackgroundResource(R.drawable.button_round_grey)

        messageEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // text가 변경된 후 호출
                // s에는 변경 후의 문자열이 담겨 있다.
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // text가 변경되기 전 호출
                // s에는 변경 전 문자열이 담겨 있다.
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // text가 바뀔 때마다 호출된다.
                // 우린 이 함수를 사용한다.
                checkEmail()
            }
        })

        //EditText 값 있을때만 버튼 활성화
        messageEdit2.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            //값 변경 시 실행되는 함수
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                //입력값 담기
                message = messageEdit2.text.toString()

                //값 유무에 따른 활성화 여부
                if(message.isNotEmpty() && checkEmail()){
                    messageBtn.isEnabled = true
                    messageBtn2.isEnabled = true
                    messageBtn.setBackgroundResource(R.drawable.button_round_color)
                    messageBtn2.setBackgroundResource(R.drawable.button_round_color)
                } else {
                    messageBtn.isEnabled = false
                    messageBtn2.isEnabled = false
                    messageBtn.setBackgroundResource(R.drawable.button_round_grey)
                    messageBtn2.setBackgroundResource(R.drawable.button_round_grey)
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        binding.signBtn.setOnClickListener {
            //이메일,비밀번호 회원가입........................
            val email:String = binding.authEmailEditView.text.toString()
            val password:String = binding.authPasswordEditView.text.toString()
            MyApplication.auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this){ task ->
                    if(task.isSuccessful){
                        // 이메일 2차 인증 작업
                        MyApplication.auth.currentUser?.sendEmailVerification()?.addOnCompleteListener{
                                sendTask ->
                            if(sendTask.isSuccessful){
                                // Log.d("mobileApp", "회원가입 성공..이메일 확인")
                                Toast.makeText(baseContext, "회원가입 성공..이메일 확인", Toast.LENGTH_LONG).show()
                                changeVisibility("logout")
                            }
                            else{
                                // Log.d("mobileApp", "메일 전송 실패...")
                                Toast.makeText(baseContext, "메일 전송 실패...", Toast.LENGTH_LONG).show()
                                changeVisibility("logout")
                            }
                        }
                    }
                    else{
                        // Log.d("mobileApp", "회원가입 실패..")
                        Toast.makeText(baseContext, "회원가입 실패..", Toast.LENGTH_LONG).show()
                        changeVisibility("logout")
                    }
                    binding.authEmailEditView.text.clear()
                    binding.authPasswordEditView.text.clear()
                }
        }

        binding.loginBtn.setOnClickListener {
            //이메일, 비밀번호 로그인.......................
            val email:String = binding.authEmailEditView.text.toString()
            val password:String = binding.authPasswordEditView.text.toString()
            MyApplication.auth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this){ task ->
                    if(task.isSuccessful){
                        if(MyApplication.checkAuth()){
                            MyApplication.email = email
                            Toast.makeText(baseContext, "로그인 성공..", Toast.LENGTH_LONG).show()
                            // changeVisibility("login")
                            finish()
                            MyApplication.userCheck()
                            val intent = Intent(this, TutorialActivity::class.java)
                            startActivity(intent)
                        }
                        else{
                            Toast.makeText(baseContext, "이메일 인증 실패..", Toast.LENGTH_LONG).show()
                            // changeVisibility("logout")
                        }
                    }
                    else{
                        Toast.makeText(baseContext, "로그인 실패..", Toast.LENGTH_LONG).show()
                        changeVisibility("logout")
                    }
                    binding.authEmailEditView.text.clear()
                    binding.authPasswordEditView.text.clear()
                }
        }

        binding.logoutBtn.setOnClickListener {
            //로그아웃...........
            MyApplication.auth.signOut()
            MyApplication.email = null
            changeVisibility("logout")
//            finish()
        }

        binding.leaveBtn.setOnClickListener {
            val userId = MyApplication.auth.currentUser?.uid

            // Check if the user is authenticated
            if (userId != null) {
                // Get a reference to the Firestore collection
                val usersCollection = MyApplication.db.collection("users")

                // Get a reference to the document using the user ID
                val userDocument = usersCollection.document(userId)

                // Delete the document
                userDocument.delete()
                    .addOnSuccessListener {
                        // If deletion is successful, delete the Firebase Authentication user
                        MyApplication.auth.currentUser?.delete()
                            ?.addOnSuccessListener {
                                changeVisibility("logout")
                                Toast.makeText(this, "회원탈퇴 성공", Toast.LENGTH_SHORT).show()
                            }
                            ?.addOnFailureListener { e ->
                                // Handle the failure to delete the Firebase Authentication user
                                Toast.makeText(this, "Firebase Authentication 사용자 삭제 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        // Handle the failure to delete the Firestore document
                        Toast.makeText(this, "Firestore 문서 삭제 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Handle the case where the user is not authenticated
                Toast.makeText(this, "사용자가 인증되지 않았습니다.", Toast.LENGTH_SHORT).show()
            }
        }

//        binding.UnsubscribingBtn.setOnClickListener {
//            MyApplication.auth.currentUser!!.delete()
//
//            val userDocRef = MyApplication.db.collection("users").document(MyApplication.auth.uid.toString())
//            MyApplication.db.collection("users").document("${MyApplication.auth.uid}")
//                .get()
//                .addOnSuccessListener {  documentSnapshot ->
//                    if(documentSnapshot.exists()) {
//                        val currentEmail = documentSnapshot.getString("userEmail")
//                        currentEmail?.let {
//                            val updatedEmail = "Unsubscribed members"
//                            updateEmail(userDocRef, updatedEmail)
//                        }
//                        val currentImage = documentSnapshot.getString("imageUrl")
//                        currentImage?.let {
//                            val updatedImage = "https://firebasestorage.googleapis.com/v0/b/reviewmate-59794.appspot.com/o/profile_images%2Fimg_1.png?alt=media&token=eb7e37c7-bbc3-4ef5-9491-bbca0f8c60bc"
//                            updateProfile(userDocRef, updatedImage)
//                        }
//                    }
//                }
//
//            MyApplication.email = null
//            finish()
//        }

        val requestLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            // ApiException : Google Play 서비스 호출이 실패했을 때 태스크에서 반환할 예외
            try{
                val account = task.getResult(ApiException::class.java) // account에 대한 정보를 따로 받음
                val credential = GoogleAuthProvider.getCredential(account.idToken, null) // 인증 되었는지 확인
                MyApplication.auth.signInWithCredential(credential)
                    .addOnCompleteListener(this){ task ->
                        if(task.isSuccessful){
                            MyApplication.email = account.email
                            // changeVisibility("login")
                            Log.d("TastyLog", "GoogleSingIn - Successful")
                            finish()
                        }
                        else {
                            changeVisibility("logout")
                            Log.d("TastyLog", "GoogleSingIn - NOT Successful")
                        }
                    }
            } catch (e: ApiException){
                changeVisibility("logout")
                Log.d("TastyLog", "GoogleSingIn - ${e.message}")
            }
        }
        binding.googleLoginBtn.setOnClickListener {
            //구글 로그인....................
            val gso : GoogleSignInOptions = GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val signInIntent : Intent = GoogleSignIn.getClient(this, gso).signInIntent
            requestLauncher.launch(signInIntent)
        }
    }

    fun checkEmail():Boolean{
        var email = binding.authEmailEditView.text.toString().trim() //공백제거
        val p = Pattern.matches(emailValidation, email) // 서로 패턴이 맞닝?
        if (p) {
            //이메일 형태가 정상일 경우
            binding.authEmailEditView.setTextColor(R.color.black.toInt())
            return true
        } else {
            binding.authEmailEditView.setTextColor(-65536)
            //또는 questionEmail.setTextColor(R.color.red.toInt())
            return false
        }
    }

    fun changeVisibility(mode: String){
        if(mode.equals("signin")){
            binding.run {
                logoutBtn.visibility = View.GONE
                goSignInBtn.visibility = View.GONE
                googleLoginBtn.visibility = View.GONE
                email.visibility = View.VISIBLE
                passwd.visibility = View.VISIBLE
                signBtn.visibility = View.VISIBLE
                loginBtn.visibility = View.GONE
                leaveBtn.visibility = View.GONE
            }
        }else if(mode.equals("login")){
            binding.run {
//                authMainTextView.text = "${MyApplication.email} 님 반갑습니다."
                logoutBtn.visibility= View.VISIBLE
                leaveBtn.visibility= View.VISIBLE
                goSignInBtn.visibility= View.GONE
                googleLoginBtn.visibility= View.GONE
                email.visibility= View.GONE
                passwd.visibility= View.GONE
                signBtn.visibility= View.GONE
                loginBtn.visibility= View.GONE
            }

        }else if(mode.equals("logout")){
            binding.run {
//                authMainTextView.text = "로그인 하거나 회원가입 해주세요."
                logoutBtn.visibility = View.GONE
                goSignInBtn.visibility = View.VISIBLE
                googleLoginBtn.visibility = View.VISIBLE
                email.visibility = View.VISIBLE
                passwd.visibility = View.VISIBLE
                signBtn.visibility = View.GONE
                loginBtn.visibility = View.VISIBLE
                leaveBtn.visibility = View.GONE
            }
        }
    }
}