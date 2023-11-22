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
        var userNickname:String? = null
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

        suspend fun getImageUrl(userEmail: String?): String? {
            return try {
                val querySnapshot = db.collection("users")
                    .whereEqualTo("userEmail", userEmail)
                    .get().await()

                if (!querySnapshot.isEmpty) {
                    val userDocument = querySnapshot.documents[0]
                    userDocument.getString("imageUrl")
                } else {
                    null
                }
            } catch (exception: Exception) {
                Log.e("MyPhotoAdapter", "Error getting user document: $exception")
                null
            }
        }

        fun userCheck() {
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
                            db.collection("users").document(auth.uid.toString()).set(userInfo)
                            Log.d("TastyLog", "계정을 user collection에 추가했습니다.")
                            userInfo.imageUrl = "https://firebasestorage.googleapis.com/v0/b/sw-7b025.appspot.com/o/profile%2Flevel_1.png?alt=media&token=b38fbb1c-a264-41cc-a333-2905a1fd07f3"
                        }
                    }
                }
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
                        userNickname = documentSnapshot.getString("userNickname")
                    } else {
                        Log.d("TastyLog", "No such document")
                        userCheck()
                    }
                }
                .addOnFailureListener { e ->
                    Log.d("TastyLog", "Error getting document: ${e.message}")
                }
        } else {
            Log.d("TastyLog", "Current user is null")
        }

    }

}