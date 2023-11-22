package com.example.a2023sw.ui.home

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a2023sw.*
import com.example.a2023sw.MyApplication.Companion.auth
import com.example.a2023sw.R
import com.example.a2023sw.databinding.FragmentHomeBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.Query
import com.prolificinteractive.materialcalendarview.*
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashSet

class HomeFragment : Fragment() {

    var nickname : String = ""

    private var selectedDate: LocalDate? = null
    private var selectedDate_add1: String = "0"
    lateinit var binding: FragmentHomeBinding

    private var recordDates: HashSet<CalendarDay> = HashSet() // 리뷰 날짜를 저장할 HashSet

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        binding = FragmentHomeBinding.inflate(inflater, container, false)

        if(MyApplication.checkAuth()){
            MyApplication.db.collection("users").document("${MyApplication.auth.uid}")
                .get()
                .addOnSuccessListener {  documentSnapshot ->
                    if(documentSnapshot.exists()) {
                        binding.HomeEmailView.text = documentSnapshot.getString("userNickname")
                        nickname = binding.HomeEmailView.text.toString()
                    }
                }
            binding.HomeEmailView.text = "${nickname}님 환영합니다!"
            userPoint()
        } else {
            binding.HomeEmailView.text = "로그인 혹은 회원가입을 진행해주세요."
            binding.userPoint.visibility = View.GONE
        }
        fetchRecordDates()

        binding.tutorialBtn.setOnClickListener {
            val intent = Intent(requireContext(), TutorialActivity::class.java)
            startActivity(intent)
        }

        // CalendarView에 날짜 데코레이터 추가
        val eventDecorator = EventDecorator(Color.RED, recordDates)
        binding.calendarView.addDecorator(eventDecorator)

        val currentDate = LocalDate.now()
        val calendarDay =
            CalendarDay.from(currentDate.year, currentDate.monthValue, currentDate.dayOfMonth)
        binding.calendarView.setDateSelected(calendarDay, true)

        binding.calendarView.setOnDateChangedListener(object : OnDateSelectedListener {
            override fun onDateSelected(
                widget: MaterialCalendarView,
                date: CalendarDay,
                selected: Boolean
            ) {
                selectedDate = LocalDate.of(
                    date.year,
                    date.month,
                    date.day
                )

                selectedDate_add1 = LocalDate.of(
                    date.year,
                    date.month,
                    date.day + 1
                ).toString()

                val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val formattedDate: String = selectedDate.toString().format(dateFormat)

//                Toast.makeText(context, "선택한 날짜: $formattedDate", Toast.LENGTH_SHORT).show()

                updateRecordListForSelectedDate()
            }
        })

        return binding.root
    }

    private fun fetchRecordDates() {
        // 파이어베이스에서 리뷰 날짜를 가져와 reviewDates에 추가
        MyApplication.db.collection("photos")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {

                    val item = document.toObject(ItemPhotoModel::class.java)

                    val dateStr = item.date // 리뷰의 날짜를 가져옴, 날짜 형식에 따라 변환 필요
                    if (dateStr != null) {
                        val date = parseDate(dateStr) // 날짜 문자열을 CalendarDay 객체로 파싱
                        if (date != null) {
                            if(item.email == MyApplication.email) { // 내가 쓴 리뷰만
                                recordDates.add(date) //
                            }

                            Log.d("days", "recordDates : ${date}")
//                            Toast.makeText(context, "${date}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                Log.d("days","all recordEates : ${recordDates.size}, booleaan: ${recordDates.contains(
                    CalendarDay.from(2023,9,18))}")
                // CalendarView를 업데이트하여 도트를 표시
                binding.calendarView.invalidateDecorators()
            }
            .addOnFailureListener {
                onError()
            }
    }

    private fun parseDate(dateStr: String): CalendarDay? {
        try {
            val dateFormat = SimpleDateFormat("yyyy-M-dd", Locale.getDefault())
            val date = dateFormat.parse(dateStr)
            if (date != null) {
                val calendar = Calendar.getInstance()
                calendar.time = date
                return CalendarDay.from(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1, // 월은 0부터 시작하므로 1을 더합니다.
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return null
    }


    private fun updateRecordListForSelectedDate() {
        if (MyApplication.checkAuth()) {
            MyApplication.db.collection("photos")
                .whereGreaterThanOrEqualTo("date", selectedDate.toString())
                .whereLessThan("date", selectedDate_add1)
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener { result ->
                    val itemList = mutableListOf<ItemPhotoModel>()
                    for (document in result) {
                        val item = document.toObject(ItemPhotoModel::class.java)
                        if (MyApplication.email.equals(item.email)) {
                            item.docId = document.id
                            itemList.add(item)
                        }
                    }
                    val bottomSheetDialog = BottomSheetDialog(requireContext())
                    val view = LayoutInflater.from(requireContext()).inflate(
                        R.layout.bottom_sheet_review_list,
                        null
                    )
                    val recyclerView = view.findViewById<RecyclerView>(R.id.bottomSheetRecyclerView)
                    val date = view.findViewById<TextView>(R.id.dateString)
                    val guide = view.findViewById<TextView>(R.id.textView)

                    date.text = selectedDate.toString()
                    recyclerView.layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    recyclerView.adapter = MyPhotoAdapter(requireContext(), itemList)

                    if (result.size() == 0 || result.size() < 0) guide.visibility = View.VISIBLE

                    bottomSheetDialog.setContentView(view)
                    bottomSheetDialog.show()
                }
                .addOnFailureListener {
                    onError()
                }
        }
    }

    private fun onError() {
        Toast.makeText(activity, "error Record", Toast.LENGTH_SHORT).show()
    }

    private fun userPoint(){
        if(MyApplication.checkAuth()){
            MyApplication.db.collection("users").document(auth.uid.toString())
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        binding.userPoint.text = documentSnapshot.getLong("userPoint").toString()
                    }
                }
                .addOnFailureListener { e ->
                    //
                }
        }
    }
}

class EventDecorator() : DayViewDecorator {

    private var color = Color.GREEN
    private var dates: HashSet<CalendarDay> =HashSet() // 날짜를 저장할 HashSet 초기화

    constructor(color: Int, reviewDates: HashSet<CalendarDay>) : this() {
        this.color = color
        this.dates = reviewDates
        Log.d("days", "dates list size : ${dates.size}")
    }


    override fun shouldDecorate(day: CalendarDay?): Boolean {
//        val today = CalendarDay.today()
//        return day?.equals(today) == true
        Log.d("days", "today : ${CalendarDay.today()}")
        return dates.contains(day)
    }


    override fun decorate(view: DayViewFacade?) {
//        //view?.addSpan(DotSpan(10F, color))
//        view?.setSelectionDrawable(drawable)
        view?.addSpan(DotSpan(5F, color)) // 원 모양의 도트 추가 (크기와 색상 지정 가능)
    }

}