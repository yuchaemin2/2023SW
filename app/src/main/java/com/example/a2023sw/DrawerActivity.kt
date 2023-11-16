package com.example.a2023sw

import android.content.Intent
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.a2023sw.databinding.ActivityDrawerBinding
import com.example.a2023sw.databinding.ActivityMainBinding
import com.example.a2023sw.ui.mypage.AlarmFragment1
import com.google.android.material.navigation.NavigationView

class DrawerActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding:ActivityDrawerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDrawerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.CertifyEmailView.text = MyApplication.email

        binding.btnNext.setOnClickListener {
            if (!binding.drawer.isDrawerOpen(Gravity.RIGHT)){
                binding.drawer.openDrawer(Gravity.RIGHT)
            }
        }

        binding.mainDrawer.setNavigationItemSelectedListener(this)

//        binding.logout.paintFlags = Paint.UNDERLINE_TEXT_FLAG
//
//        binding.logout.setOnClickListener {
////            auth.signOut()
//            val intent = Intent(this, AuthActivity::class.java)
//            startActivity(intent)
//        }

        binding.settingsNotification.setOnClickListener{
            var bundle : Bundle = Bundle()
            bundle.putString("notification", "알람프레그먼트")
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            val alarmfragment1: Fragment = AlarmFragment1()
            alarmfragment1.arguments = bundle
            transaction.replace(R.id.container, alarmfragment1)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        var toolbar = binding.toolbarBack
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 뒤로가기 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back)
        supportActionBar?.setDisplayShowTitleEnabled(false)//타이틀 없애기
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_mypage, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> { //뒤로 가기 버튼
                onBackPressed() // 기본 뒤로가기 동작 수행
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.item_logout -> {
                val intent = Intent(this, AuthActivity::class.java)
                startActivity(intent)
            }
            R.id.item_service_out -> {
                val intent = Intent(this, AuthActivity::class.java)
                startActivity(intent)
            }
        }
//        binding.drawer.closeDrawer(GravityCompat.START)
        return false
    }
}