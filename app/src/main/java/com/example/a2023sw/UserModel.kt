package com.example.a2023sw

data class UserModel(
    var uid : String? = null,
    var userEmail : String? = null,
    var imageUrl : String? = null,
    var userNickname : String? = "친절한 햄버거",
    var userPoint: Long = 0,
    var profileList: ArrayList<Int>? = ArrayList(),

)