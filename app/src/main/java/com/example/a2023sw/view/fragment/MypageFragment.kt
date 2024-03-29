package com.example.a2023sw.view.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.a2023sw.*
import com.example.a2023sw.MyApplication.Companion.auth
import com.example.a2023sw.databinding.FragmentMypageBinding
import com.example.a2023sw.model.ItemPhotoModel
import com.example.a2023sw.view.activity.AddActivity
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MypageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MypageFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    lateinit var binding: FragmentMypageBinding
    private lateinit var imageView: ImageView
    private var  imageUrl : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMypageBinding.inflate(inflater, container, false)

//        profile = binding.userProfile

        if(MyApplication.checkAuth()){
            val userDocRef = MyApplication.db.collection("users").document(auth.uid.toString())
            MyApplication.db.collection("users").document("${auth.uid}")
                .get()
                .addOnSuccessListener {  documentSnapshot ->
                    if(documentSnapshot.exists()) {
                        val userNickname = documentSnapshot.getString("userNickname")
                        binding.NicknameView.text = userNickname.toString()
                    }
                }
        }

        CoroutineScope(Dispatchers.Main).launch {
            imageUrl =  MyApplication.getImageUrl(MyApplication.email).toString()
            imageView = binding.userProfile
            if( imageUrl != null){
                Glide.with(requireContext())
                    .load(imageUrl)
                    .into(binding.userProfile)
            }
        }

        binding.addBtn.setOnClickListener {
            val intent = Intent(requireContext(), AddActivity::class.java)
            startActivity(intent)
        }

        binding.imageView.setOnClickListener {
            Toast.makeText(requireContext(), "달콤한 버터와플이 먹고싶네...", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        if(MyApplication.checkAuth()){
            MyApplication.db.collection("photos")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    val itemList = mutableListOf<ItemPhotoModel>()
                    for(document in result){
                        val item = document.toObject(ItemPhotoModel::class.java)
                        if(MyApplication.email.equals(item.email)){
                            val uriStringList: ArrayList<String> = item.uriList as ArrayList<String>
                            val uriList: ArrayList<Uri> = uriStringList.map { Uri.parse(it) } as ArrayList<Uri>

                            item.docId = document.id
                            itemList.add(item)
                        }
                    }
                    binding.feedRecyclerView.layoutManager = GridLayoutManager(requireContext(),3)
                    binding.feedRecyclerView.adapter = MyPhotoAdapter(requireContext(), itemList)

                    if(!itemList.isEmpty()){
                        binding.imageView.visibility = View.GONE
                        binding.textView.visibility = View.GONE
                        binding.addBtn.visibility = View.GONE
                        Log.d("TastyLog", "차있음")
                    } else {
                        binding.imageView.visibility = View.VISIBLE
                        binding.textView.visibility = View.VISIBLE
                        binding.addBtn.visibility = View.VISIBLE
                        Log.d("TastyLog", "비어있음")
                    }
                }
                .addOnFailureListener{
                    Toast.makeText(requireContext(), "데이터 획득 실패", Toast.LENGTH_SHORT).show()
                }
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MypageFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MypageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}