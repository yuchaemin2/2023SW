package com.example.a2023sw.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.infiniteRepeatable
import com.example.a2023sw.R
import com.example.a2023sw.databinding.FragmentWaterBinding
import com.scwang.wave.MultiWaveHeader


class WaterFragment : Fragment() {
    lateinit var binding: FragmentWaterBinding

    private lateinit var waveFooter: MultiWaveHeader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWaterBinding.inflate(inflater, container, false)

        return binding.root
    }
}