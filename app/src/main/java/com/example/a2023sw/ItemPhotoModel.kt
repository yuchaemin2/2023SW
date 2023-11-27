package com.example.a2023sw

import android.net.Uri

data class ItemPhotoModel(
    var docId: String? = null,
    var email: String? = null,
    var title: String? = null,
    var date: String? = null,
    var image_date: String? = null,
    var foodTime: String? = null,
    var food: String? = null,
    var uid: String? = null,
    var foodImage : String?= null,
    var company : String? = null,
    var where: String? = null,
    var memo: String? = null,
    var nickName: String? = null,
    var bookmark: String? = "0",
    var count: String? = null,
    var uriList: ArrayList<String> = ArrayList(),
)
