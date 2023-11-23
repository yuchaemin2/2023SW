package com.example.a2023sw

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.a2023sw.databinding.ActivityAuthBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentReference

class AuthActivity : AppCompatActivity() {
    lateinit var binding: ActivityAuthBinding

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
//            changeVisibility("logout")
            finish()
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