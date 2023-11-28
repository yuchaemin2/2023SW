package com.example.a2023sw

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.GridView
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.a2023sw.view.adapter.CalendarAdapter
import com.example.a2023sw.view.adapter.WeekAdapter
import com.example.a2023sw.model.Day
import java.util.*


class MainActivity2 : AppCompatActivity() {
    /**
     * 월/요일 텍스트뷰
     */
    private var tv_month: TextView? = null
    private var tv_date: TextView? = null

    /**
     * 년도, 글쓰기, 사이드바 버튼
     */
    private var btn_year: Button? = null
    private var btn_write: ImageButton? = null
    private var btn_sidebar: ImageButton? = null

    /**
     * 그리드뷰 어댑터
     */
    private var calendarAdapter: CalendarAdapter? = null
    private var day_of_weekGridAdapter: WeekAdapter? = null

    /**
     * 요일 리스트
     */
    private var dayList: ArrayList<Day>? = null
    private var day_of_weekList: ArrayList<String>? = null

    /**
     * 그리드뷰
     */
    private var gv_month: GridView? = null
    private var gv_day_of_week: GridView? = null

    /**
     * 캘린더 변수
     */
    private var mCal: Calendar? = null
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv_month = findViewById<View>(R.id.tv_month) as TextView
        tv_date = findViewById<View>(R.id.tv_date) as TextView
        gv_month = findViewById<View>(R.id.gv_month) as GridView
        gv_day_of_week = findViewById<View>(R.id.gv_day_of_week) as GridView
        btn_year = findViewById<View>(R.id.btn_year) as Button

        //gridview_day_of_week에 요일 리스트 표시
        day_of_weekList = ArrayList()
        day_of_weekList!!.add("SUN")
        day_of_weekList!!.add("MON")
        day_of_weekList!!.add("TUE")
        day_of_weekList!!.add("WED")
        day_of_weekList!!.add("THU")
        day_of_weekList!!.add("FRI")
        day_of_weekList!!.add("SAT")
        day_of_weekGridAdapter = WeekAdapter(this, day_of_weekList!!)
        gv_day_of_week!!.adapter = day_of_weekGridAdapter
        dayList = ArrayList()
    }

    override fun onResume() {
        super.onResume()

        // 캘린더 인스턴스 생성
        mCal = Calendar.getInstance()
//        mCal.set(Calendar.DAY_OF_MONTH, 1)
        getCalendar(mCal)
    }

    /**
     * 달력 세팅
     * @param calendar
     */
    private fun getCalendar(calendar: Calendar?) {
        var lastMonthStartDay: Int
        val dayOfMonth: Int
        val thisMonthLastDay: Int
        dayList!!.clear()

        // 이번달 시작 요일
        dayOfMonth = calendar!![Calendar.DAY_OF_WEEK]
        thisMonthLastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        calendar.add(Calendar.MONTH, -1)
        Log.e("지난달 마지막일", calendar[Calendar.DAY_OF_MONTH].toString() + "")

        // 지난달 마지막 일자
        lastMonthStartDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        calendar.add(Calendar.MONTH, 1)
        Log.e("이번달 시작일", calendar[Calendar.DAY_OF_MONTH].toString() + "")
        lastMonthStartDay -= dayOfMonth - 1 - 1

        // 년월 표시
        tv_month!!.text = (mCal!![Calendar.MONTH] + 1).toString() + ""
        btn_year!!.text = mCal!![Calendar.YEAR].toString() + ""
        var day: Day
        Log.e("DayOfMonth", dayOfMonth.toString() + "")
        for (i in 0 until dayOfMonth - 1) {
            val date = lastMonthStartDay + i
            day = Day()
            day.day = Integer.toString(date)
            day.isInMonth = false
            dayList!!.add(day)
        }
        for (i in 1..thisMonthLastDay) {
            day = Day()
            day.day = Integer.toString(i)
            day.isInMonth = true
            dayList!!.add(day)
        }
        for (i in 1 until 35 - (thisMonthLastDay + dayOfMonth) + 1) {
            day = Day()
            day.day = Integer.toString(i)
            day.isInMonth = false
            dayList!!.add(day)
        }
        initCalendarAdapter()
    }

    private fun initCalendarAdapter() {
        calendarAdapter = CalendarAdapter(this, dayList!!)
        gv_month!!.adapter = calendarAdapter
    }
}