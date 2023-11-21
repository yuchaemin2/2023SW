package com.example.a2023sw

data class UserModel(
    var uid : String? = null,
    var userEmail : String? = null,
    var imageUrl : String? = null,
    var userNickname : String? = null,
    var userPoint: Long = 0
)