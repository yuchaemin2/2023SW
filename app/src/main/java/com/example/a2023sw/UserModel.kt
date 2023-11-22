package com.example.a2023sw

data class UserModel(
    var uid : String? = null,
    var userEmail : String? = null,
    var imageUrl : String? = null,
    var userNickname : String? = "쿨한 감자튀김",
    var userPoint: Long = 0,
    var profileList: ArrayList<String> = ArrayList(),
)