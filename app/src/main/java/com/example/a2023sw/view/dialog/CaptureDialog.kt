package com.example.a2023sw.view.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.a2023sw.R
import com.example.a2023sw.databinding.ActivityCaptureDialogBinding
import com.example.a2023sw.view.activity.MediaScanner
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.FileOutputStream


class CaptureDialog(context: Context) : Dialog(context) {

    lateinit var binding: ActivityCaptureDialogBinding
    lateinit var cTitle: String
    lateinit var cDate: String
    lateinit var cDocId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCaptureDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    // 호출할 다이얼로그 함수 정의
    fun callCaptureDialog(context: Context, date: String, diary_image: StorageReference, diary_text: String) {

        val binding = ActivityCaptureDialogBinding.inflate(LayoutInflater.from(context))

        // 다이얼로그 호출
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.activity_capture_dialog)
        dialog.show()

        Log.d("TastyLog", "캡쳐 다이얼로그 호출 성공")

        val capture_target_Layout = dialog.findViewById<View>(R.id.capture) as RelativeLayout
        val btnDate = dialog.findViewById<View>(R.id.tv_capture_date) as TextView
        val btnTitle = dialog.findViewById<View>(R.id.title_view) as TextView
        btnDate.text = date
        btnTitle.text = diary_text

        val image = dialog.findViewById<View>(R.id.iv_captrue_image) as ImageView
        diary_image.downloadUrl.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Glide.with(context)
                    .load(task.result)
                    .apply(RequestOptions().override(260, 300).centerCrop())
                    .into(image)
            }

        }
        val btn_capture_cancel = dialog.findViewById<View>(R.id.btn_capture_cancel) as Button
        val btn_capture_this = dialog.findViewById<View>(R.id.btn_capture_this) as Button

        btn_capture_this.setOnClickListener { // '일기 저장' 버튼
            Request_Capture(capture_target_Layout, date)
            Toast.makeText(context, "폴라로이드를 저장했습니다.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        btn_capture_cancel.setOnClickListener { // '캡쳐 취소' 버튼
            Toast.makeText(context, "캡쳐를 취소했습니다.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
    }

    fun Request_Capture(view: View?, title: String) {
        if (view == null) { //Null Point Exception ERROR 방지
            Log.d("TastyLog", "::::ERROR:::: view == NULL")
            return
        }

        /* 캡쳐 파일 저장 */
        view.buildDrawingCache() //캐시 비트 맵 만들기
        val bitmap = view.drawingCache
        val fos: FileOutputStream

        /* 저장할 폴더 Setting */
        val uploadFolder: File =
            Environment.getExternalStoragePublicDirectory("/DCIM/Camera/") //저장 경로 (File Type형 변수)
        if (!uploadFolder.exists()) { //만약 경로에 폴더가 없다면
            uploadFolder.mkdir() //폴더 생성
        }

        /* 파일 저장 */
        val Str_Path: String = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/DCIM/Camera/" //저장 경로 (String Type 변수)
        Log.d("TastyLog", "폴라로이드 저장 경로: ${Str_Path}")
        try {
            fos = FileOutputStream("$Str_Path$title.jpg") // 경로 + 제목 + .jpg로 FileOutputStream Setting
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos)
            Log.d("TastyLog", "폴라로이드 저장 성공")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("TastyLog", "폴라로이드 저장 실패: ${e}")
        }

        //캡쳐 파일 미디어 스캔 (https://hongdroid.tistory.com/7)
        val ms: MediaScanner = context.let { MediaScanner.newInstance(it) }
        try { // TODO : 미디어 스캔
            ms.mediaScanning("$Str_Path$title.jpg")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("TastyLog", "::::ERROR:::: $e")
        }
    }
}