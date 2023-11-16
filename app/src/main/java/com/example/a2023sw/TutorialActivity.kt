package com.example.a2023sw

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.a2023sw.databinding.ActivityMainBinding
import com.example.a2023sw.databinding.ActivityTutorialBinding

class TutorialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTutorialBinding

    private lateinit var viewPager: ViewPager
    private lateinit var pagerAdapter: PagerAdapter
    private lateinit var dotsLayout: LinearLayout
    private lateinit var dots: Array<TextView>
    private lateinit var layouts: IntArray
    private lateinit var btnSkip: Button
    private lateinit var btnNext: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTutorialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewPager = binding.viewpager2
        dotsLayout = findViewById(R.id.layoutDots)
        btnSkip = findViewById(R.id.btn_skip)
        btnNext = binding.btnNext

        layouts = intArrayOf(
            R.layout.page1,
            R.layout.page2,
            R.layout.page3,
        )

        addBottomDots(0)

        pagerAdapter = PagerAdapter()
        viewPager.adapter = pagerAdapter
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener)

        btnSkip.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        btnNext.setOnClickListener {
            finish()
        }

    }

    private fun addBottomDots(currentPage: Int) {
        dots = Array(layouts.size) { TextView(this) }

        val colorsActive = resources.getIntArray(R.array.array_dot_active)
        val colorsInactive = resources.getIntArray(R.array.array_dot_inactive)

        dotsLayout.removeAllViews()
        for (i in dots.indices) {
            dots[i] = TextView(this)
            dots[i].text = Html.fromHtml("&#8226;")
            dots[i].textSize = 35f
            dots[i].setTextColor(colorsInactive[currentPage])
            dotsLayout.addView(dots[i])
        }

        if (dots.isNotEmpty()) {
            dots[currentPage].setTextColor(colorsActive[currentPage])
        }
    }

    private fun getItem(i: Int): Int {
        return viewPager.currentItem + i
    }

    private val viewPagerPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageSelected(position: Int) {
            addBottomDots(position)

            if (position == layouts.size - 1) {
                btnNext.text = "Start"
                btnNext.visibility = View.VISIBLE
                btnSkip.visibility = View.GONE
            } else {
                btnNext.text = "next"
                btnNext.visibility = View.GONE
                btnSkip.visibility = View.VISIBLE
            }
        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}

        override fun onPageScrollStateChanged(arg0: Int) {}
    }

    inner class PagerAdapter : androidx.viewpager.widget.PagerAdapter() {
        private var layoutInflater: LayoutInflater? = null

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            val view: View = layoutInflater!!.inflate(layouts[position], container, false)
            container.addView(view)

            return view
        }

        override fun getCount(): Int {
            return layouts.size
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view === obj
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val view: View = `object` as View
            container.removeView(view)
        }
    }
}