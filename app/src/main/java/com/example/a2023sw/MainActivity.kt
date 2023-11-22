package com.example.a2023sw

import android.app.ActivityOptions
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import androidx.annotation.NonNull
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.a2023sw.MyApplication.Companion.auth
import com.example.a2023sw.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    val MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE = 123

    override fun onCreate(savedInstanceState: Bundle?) {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            with(window) {
                requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
                // set an slide transition
                enterTransition = Slide(Gravity.END)
                exitTransition = Slide(Gravity.START)
            }
        }
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkExternalStoragePermission()

        if (MyApplication.checkAuth()) {

            val navView: BottomNavigationView = binding.navView

            val navController = findNavController(R.id.nav_host_fragment_activity_main)
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            val appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.navigation_home, R.id.navigation_add, R.id.navigation_mypage,
                )
            )

            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)


        } else {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }

        // Check if the permission is not granted
//        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
//            != PackageManager.PERMISSION_GRANTED
//        ) {
//            // Request the permission
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
//                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
//            )
//        } else {
//            // Permission already granted, proceed with accessing content
//            // ...
//        }
    }

    override fun onStart() {
        super.onStart()

        if (MyApplication.checkAuth()) {

            val navView: BottomNavigationView = binding.navView

            val navController = findNavController(R.id.nav_host_fragment_activity_main)
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            val appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.navigation_home, R.id.navigation_add, R.id.navigation_mypage,
                )
            )

            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)


        } else {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_all, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_search ->
                return true
            R.id.menu_settings -> {
                intent = Intent(this, DrawerActivity::class.java)
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
            }


        }
        return super.onOptionsItemSelected(item)
    }

    // 외부 저장소 읽기 권한을 체크하고 요청하는 함수
    private fun checkExternalStoragePermission() {
        // 권한이 부여되어 있는지 체크
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 사용자에게 권한 요청
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE
            )
        } else {
            // 이미 권한이 부여되어 있는 경우 외부 저장소에 접근할 수 있습니다.
            // 이곳에서 외부 저장소에 대한 동작을 수행하면 됩니다.
        }
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE -> {
                // 권한 요청에 대한 응답을 받았을 때 처리
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // 권한이 부여되면 외부 저장소에 접근할 수 있습니다.
                    // 이곳에서 외부 저장소에 대한 동작을 수행하면 됩니다.
                } else {
                    // 권한이 거부되었을 경우 처리
                    // 사용자에게 권한이 필요한 이유를 설명하거나 다른 대체 동작을 수행할 수 있습니다.
                }
                return
            }
        }
    }

}