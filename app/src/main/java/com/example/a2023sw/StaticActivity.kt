package com.example.a2023sw

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.a2023sw.databinding.ActivityStaticBinding
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
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class StaticActivity : AppCompatActivity() {

    lateinit var binding: ActivityStaticBinding

    var chart : PieChart? = null
    var chart2 : BarChart? = null
    var chart3 : LineChart? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaticBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI(binding)

        var toolbar = binding.toolbarBack
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 뒤로가기 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back)
        supportActionBar?.setDisplayShowTitleEnabled(false)//타이틀 없애기
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_static, menu)
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

    private fun initUI(binding : ActivityStaticBinding){
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
}