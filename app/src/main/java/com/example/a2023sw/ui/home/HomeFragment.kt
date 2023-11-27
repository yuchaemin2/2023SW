package com.example.a2023sw.ui.home

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
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
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.Query
import com.prolificinteractive.materialcalendarview.*
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashSet

class HomeFragment : Fragment() {

    var nickname : String = ""

    private var selectedDate: LocalDate? = null
    private var selectedDate_add1: String = "0"
    lateinit var binding: FragmentHomeBinding

    private var recordDates: HashSet<CalendarDay> = HashSet() // 리뷰 날짜를 저장할 HashSet

    var chart : PieChart? = null
    var chart2 : BarChart? = null
    var chart3 : LineChart? = null

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

//        if (MyApplication.checkAuth()) {
//            MyApplication.db.collection("photos")
//                .get()
//                .addOnSuccessListener { result ->
//                    val itemList = mutableListOf<ItemPhotoModel>()
//                    for (document in result) {
//                        val item = document.toObject(ItemPhotoModel::class.java)
//                        if (MyApplication.email.equals(item.email)) {
//                            item.docId = document.id
//                            itemList.add(item)
//                        }
//                        setPieChart(itemList)
//                        setBarChart(itemList)
//                    }
//                }
//                .addOnFailureListener {
//                    onError()
//                }
//        }

        initUI(binding)

        return binding.root
    }

    private fun initUI(binding : FragmentHomeBinding){
        chart = binding.chart1
        chart!!.setUsePercentValues(true)
        chart!!.description.isEnabled = false

        chart!!.centerText = "음식별 비율"

        chart!!.setTransparentCircleColor(Color.WHITE)
        chart!!.setTransparentCircleAlpha(110)

        chart!!.holeRadius = 58f
        chart!!.transparentCircleRadius = 61f

        chart!!.setDrawCenterText(true)

        chart!!.isHighlightPerTapEnabled = true

        val legent1 = chart!!.legend
        legent1.isEnabled = false

        chart!!.setEntryLabelColor(Color.WHITE)
        chart!!.setEntryLabelTextSize(12f)

        setData1()


        chart2 = binding.chart2
        chart2!!.setDrawValueAboveBar(true)

        chart2!!.getDescription().isEnabled = false
        chart2!!.setDrawGridBackground(false)

        val xAxis = chart2!!.getXAxis()
        xAxis.isEnabled = false

        val leftAxis = chart2!!.getAxisLeft()
        leftAxis.setLabelCount(6, false)
        leftAxis.axisMinimum = 0.0f
        leftAxis.isGranularityEnabled = true
        leftAxis.granularity = 1f


        val rightAxis = chart2!!.getAxisRight()
        rightAxis.isEnabled = false

        val legend2 = chart2!!.getLegend()
        legend2.isEnabled = false

        chart2!!.animateXY(1500, 1500)

        setData2()


        chart3 = binding.chart3

        chart3!!.getDescription().isEnabled = false
        chart3!!.setDrawGridBackground(false)

        // set an alternative background color

        // set an alternative background color
        chart3!!.setBackgroundColor(Color.WHITE)
        chart3!!.setViewPortOffsets(0f, 0f, 0f, 0f)

        // get the legend (only possible after setting data)

        // get the legend (only possible after setting data)
        val legend3 = chart3!!.getLegend()
        legend3.isEnabled = false

        val xAxis3 = chart3!!.getXAxis()
        xAxis3.position = XAxis.XAxisPosition.BOTTOM_INSIDE
        xAxis3.textSize = 10f
        xAxis3.textColor = Color.WHITE
        xAxis3.setDrawAxisLine(false)
        xAxis3.setDrawGridLines(true)
        xAxis3.textColor = Color.rgb(255, 192, 56)
        xAxis3.setCenterAxisLabels(true)
        xAxis3.granularity = 1f
        xAxis3.setValueFormatter(object : ValueFormatter() {
            private val mFormat =
                SimpleDateFormat("MM-DD", Locale.KOREA)

            override fun getFormattedValue(value: Float): String? {
                val millis =
                    TimeUnit.HOURS.toMillis(value.toLong())
                return mFormat.format(Date(millis))
            }
        })

        val leftAxis3 = chart3!!.getAxisLeft()
        leftAxis3.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
        leftAxis3.textColor = ColorTemplate.getHoloBlue()
        leftAxis3.setDrawGridLines(true)
        leftAxis3.isGranularityEnabled = true
        leftAxis3.axisMinimum = 0f
        leftAxis3.axisMaximum = 170f
        leftAxis3.yOffset = -9f
        leftAxis3.textColor = Color.rgb(255, 192, 56)

        val rightAxis3 = chart3!!.getAxisRight()
        rightAxis3.isEnabled = false

        setData3()
    }


    private fun setData1() {
        val entries = ArrayList<PieEntry>()
        entries.add(
            PieEntry(
                20.0f, "",
                resources.getDrawable(R.drawable.baseline_local_pizza_black_18)
            )
        )
        entries.add(
            PieEntry(
                20.0f, "",
                resources.getDrawable(R.drawable.baseline_cake_black_18)
            )
        )
        entries.add(
            PieEntry(
                20.0f, "",
                resources.getDrawable(R.drawable.baseline_rice_bowl_black_18)
            )
        )
        entries.add(
            PieEntry(
                20f, "",
                resources.getDrawable(R.drawable.baseline_ramen_dining_black_18)
            )
        )
        entries.add(
            PieEntry(
                20.0f, "",
                resources.getDrawable(R.drawable.baseline_dinner_dining_black_18)
            )
        )
        val dataSet = PieDataSet(entries, "음식별 비율")
        dataSet.setDrawIcons(true)
        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0F, (-40).toFloat())
        dataSet.selectionShift = 5f
        val colors = ArrayList<Int>()
        for (c in ColorTemplate.JOYFUL_COLORS) {
            colors.add(c)
        }
        dataSet.colors = colors
        val data = PieData(dataSet)
        data.setValueTextSize(22.0f)
        data.setValueTextColor(Color.WHITE)
        chart!!.data = data
        chart!!.invalidate()
    }

    private fun setData2() {
        val entries = ArrayList<BarEntry>()
        entries.add(
            BarEntry(
                1.0f, 20.0f,
                resources.getDrawable(R.drawable.baseline_light_mode_black_18)
            )
        )
        entries.add(
            BarEntry(
                2.0f, 40.0f,
                resources.getDrawable(R.drawable.number12)
            )
        )
        entries.add(
            BarEntry(
                3.0f, 60.0f,
                resources.getDrawable(R.drawable.baseline_wb_twilight_black_18)
            )
        )
        entries.add(
            BarEntry(
                4.0f, 10.0f,
                resources.getDrawable(R.drawable.baseline_nightlight_black_18)
            )
        )
//        entries.add(
//            BarEntry(
//                5.0f, 90.0f,
//                resources.getDrawable(R.drawable.baseline_dinner_dining_black_18)
//            )
//        )
        val dataSet2 = BarDataSet(entries, "Sinus Function")
        dataSet2.color = Color.rgb(240, 120, 124)
        val colors = ArrayList<Int>()
        for (c in ColorTemplate.JOYFUL_COLORS) {
            colors.add(c)
        }
        dataSet2.colors = colors
        dataSet2.iconsOffset = MPPointF(0F, (-10).toFloat())
        val data = BarData(dataSet2)
        data.setValueTextSize(10f)
        data.setDrawValues(false)
        data.barWidth = 0.8f
        chart2!!.data = data
        chart2!!.invalidate()
    }

    private fun setData3() {
        val values =
            ArrayList<Entry>()
        values.add(Entry(24f, 20.0f))
        values.add(Entry(48f, 50.0f))
        values.add(Entry(72f, 30.0f))
        values.add(Entry(96f, 70.0f))
        values.add(Entry(120f, 90.0f))

        // create a dataset and give it a type
        val set1 = LineDataSet(values, "DataSet 1")
        set1.axisDependency = YAxis.AxisDependency.LEFT
        set1.color = ColorTemplate.getHoloBlue()
        set1.valueTextColor = ColorTemplate.getHoloBlue()
        set1.lineWidth = 1.5f
        set1.setDrawCircles(true)
        set1.setDrawValues(false)
        set1.fillAlpha = 65
        set1.fillColor = ColorTemplate.getHoloBlue()
        set1.highLightColor = Color.rgb(244, 117, 117)
        set1.setDrawCircleHole(false)

        // create a data object with the data sets
        val data = LineData(set1)
        data.setValueTextColor(Color.WHITE)
        data.setValueTextSize(9f)

        // set data
        chart3!!.data = data
        chart3!!.invalidate()
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
