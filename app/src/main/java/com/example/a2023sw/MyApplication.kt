package com.example.a2023sw

import android.util.Log
import androidx.multidex.MultiDexApplication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await

class MyApplication : MultiDexApplication() {

    companion object{

//        lateinit var db : FirebaseFirestore
        var db = FirebaseFirestore.getInstance()
        var storage : FirebaseStorage = Firebase.storage
        lateinit var auth : FirebaseAuth
        var email:String? = null
        var userPoint:String? = null
        var imageurl:String? = null

        fun checkAuth(): Boolean{
            auth = Firebase.auth

            var currentuser = auth.currentUser
            return currentuser?.let{
                email = currentuser.email
                if(currentuser.isEmailVerified) true
                else false
            } ?: false
        }

    }

    override fun onCreate() {
        super.onCreate()
        auth = Firebase.auth

        db = FirebaseFirestore.getInstance()
        storage = Firebase.storage

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userDocRef = db.collection("users").document(currentUser.uid)
            userDocRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        userPoint = documentSnapshot.getString("userPoint")
                        imageurl = documentSnapshot.getString("imageUrl")
                    } else {
                        Log.d("TastyLog", "No such document")
                    }
                }
                .addOnFailureListener { e ->
                    Log.d("TastyLog", "Error getting document: ${e.message}")
                }
        } else {
            Log.d("TastyLog", "Current user is null")
        }

        if(true) {
            var userInfo = UserModel()

            userInfo.uid = auth.uid
            userInfo.userEmail = auth.currentUser?.email

            db.collection("users").document(auth.uid.toString())
                .get()
                .addOnCompleteListener{ task ->
                    if(task.isSuccessful){
                        val document = task.result
                        if (document.exists()){ Log.d("TastyLog", "이미 존재하는 계정입니다.") }
                        else {
                            userInfo.imageUrl = "https://firebasestorage.googleapis.com/v0/b/reviewmate-59794.appspot.com/o/profile_images%2Fimg_1.png?alt=media&token=eb7e37c7-bbc3-4ef5-9491-bbca0f8c60bc"
                            db.collection("users").document(auth.uid.toString()).set(userInfo)
                            Log.d("TastyLog", "계정을 user collection에 추가했습니다.")
                        }
                    }
                }
        }
    }

}