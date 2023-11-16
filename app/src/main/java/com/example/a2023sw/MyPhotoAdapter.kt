package com.example.a2023sw

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.a2023sw.MyApplication.Companion.auth
import com.example.a2023sw.MyApplication.Companion.storage
import com.example.a2023sw.databinding.ItemPhotoBinding
import com.google.firebase.firestore.FirebaseFirestore

class MyPhotoViewHolder(val binding: ItemPhotoBinding) : RecyclerView.ViewHolder(binding.root)

class MyPhotoAdapter(val context: Context, val itemList: MutableList<ItemPhotoModel>): RecyclerView.Adapter<MyPhotoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPhotoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return MyPhotoViewHolder(ItemPhotoBinding.inflate(layoutInflater))
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    private fun removeItem(position: Int) {
        itemList.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onBindViewHolder(holder: MyPhotoViewHolder, position: Int) {
        val position = holder.getAdapterPosition()
        val data = itemList.get(position)

        holder.binding.run {
            itemEmailView.text = data.email
            itemTitleView.text = data.title
            itemDateView.text = data.date
            itemFoodTimeView.text = data.foodTime
            itemFoodView.text = data.food
            itemCompanyView.text = data.company
            itemWhereView.text = data.where
            itemMemoView.text = data.memo
            itemNickNameView.text = data.nickName

            // Firestore에서 user 모델 데이터 가져오기
            val db = FirebaseFirestore.getInstance()
//            val usersCollection = db.collection("users")

                itemFoodImageView.setOnClickListener {
                    val bundle: Bundle = Bundle()
                    bundle.putString("email", data.email)
                    bundle.putString("title", data.title)
                    bundle.putString("date", data.date)
                    bundle.putString("foodTime", data.foodTime)
                    bundle.putString("food", data.food)
                    bundle.putString("company", data.company)
                    bundle.putString("userEmail", data.email)
                    bundle.putString("where", data.where)
                    bundle.putString("foodImage", data.foodImage)
                    bundle.putString("memo", data.memo)
                    bundle.putString("nickName", data.nickName)

                    Intent(context, PhotoDetailActivity::class.java).apply {
                        putExtras(bundle)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }.run { context.startActivity(this) }
                }

                val alertHandler = object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        when (which) {
                            DialogInterface.BUTTON_POSITIVE -> {
                                if (data.docId !== null) {
                                    storage.getReference().child("images")
                                        .child("${data.docId!!}.jpg")
                                        .delete()
                                    db.collection("photos").document("${data.docId}")
                                        .delete()
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                context,
                                                "삭제가 완료되었습니다.",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(
                                                context,
                                                "삭제가 실패하였습니다.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                } else {
                                    Toast.makeText(context, "문서가 존재하지 않습니다.", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                            DialogInterface.BUTTON_NEGATIVE -> {
                                Log.d("TastyLog", "DialogInterface.BUTTON_NEGATIVE")
                            }
                        }
                    }
                }

            //스토리지 이미지 다운로드........................
            val imageRef = MyApplication.storage.reference.child("images/${data.docId}.jpg")
            imageRef.downloadUrl.addOnCompleteListener { task ->
                if (task.isSuccessful) {
//                // 다운로드 이미지를 ImageView에 보여줌
//                    GlideApp.with(context)
//                        .load(task.result)
//                        .into(holder.binding.itemFoodImageView)

                    Glide.with(context)
                        .load(task.result)
                        .apply(RequestOptions().override(120, 120).centerCrop())
                        .into(holder.binding.itemFoodImageView)
                }
            }
        }
    }
}
