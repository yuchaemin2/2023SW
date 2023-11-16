package com.example.a2023sw.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.a2023sw.R
import com.example.a2023sw.databinding.FragmentAlarm1Binding


/**
 * A simple [Fragment] subclass.
 * Use the [AlarmFragment1.newInstance] factory method to
 * create an instance of this fragment.
 */
class AlarmFragment1 : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var binding: FragmentAlarm1Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAlarm1Binding.inflate(inflater, container, false)

        setHasOptionsMenu(true)

//        binding.chatListToolbar.setNavigationOnClickListener {
//            requireActivity().onBackPressed()
//        }
//        binding.chatListToolbar.setOnMenuItemClickListener { menuItem ->
//            when(menuItem.itemId){
//                R.id.menu_delete_all -> {
//                    showDeleteConfirmationDialog()
//                    return@setOnMenuItemClickListener true
//                }
//                else -> return@setOnMenuItemClickListener true
//            }
//        }

        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AlarmFragment1.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AlarmFragment1().apply {
                arguments = Bundle().apply {

                }
            }
    }
}