package com.example.a2023sw.view.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.a2023sw.model.Day
import com.example.a2023sw.R
import java.util.*


/**
 * 그리드뷰 어댑터 - 캘린더
 */
class CalendarAdapter(private val context: Context, private val list: ArrayList<Day>) :
    BaseAdapter() {
    private val inflater: LayoutInflater
    private val mCal: Calendar? = null

    /**
     * 생성자
     *
     * @param context
     * @param list
     */
    init {
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        var convertView = convertView
        val day = list[position]
        var holder: ViewHolder? = null
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_calendar_gridview, null)
            holder = ViewHolder()
            holder!!.tv_item = convertView.findViewById<View>(R.id.tv_date) as TextView
            holder.iv_item = convertView.findViewById<View>(R.id.iv_date) as ImageView
            holder.iv_item!!.clipToOutline = true
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
        if (day != null) {
            holder!!.tv_item!!.text = day.day
            if (day.isInMonth) {
                if (position % 7 == 0) holder.tv_item!!.setTextColor(Color.RED) else holder.tv_item!!.setTextColor(
                    Color.GRAY
                )
            } else {
                holder.tv_item!!.setTextColor(Color.LTGRAY)
            }
        }
        return convertView
    }

    private inner class ViewHolder {
        var tv_item: TextView? = null
        var iv_item: ImageView? = null
    }
}