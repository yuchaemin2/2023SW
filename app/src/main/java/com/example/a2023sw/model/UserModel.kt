package com.example.a2023sw.model

data class UserModel(
    var uid : String? = null,
    var userEmail : String? = null,
    var imageUrl : String? = null,
    var userNickname : String? = "친절한 햄버거",
    var userPoint: Long = 30,
    var profileList: ArrayList<Int>? = ArrayList(),

)