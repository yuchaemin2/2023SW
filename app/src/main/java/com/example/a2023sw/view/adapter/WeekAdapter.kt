package com.example.a2023sw.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.a2023sw.R
import java.util.*


/**
 * 그리드뷰 어댑터 - 요일 리스트
 */
class WeekAdapter(private val context: Context, private val list: ArrayList<String>) :
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
        var holder: ViewHolder? = null
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_calendar_day_of_week, parent, false)
            holder = ViewHolder()
            holder!!.tvItemGridView =
                convertView.findViewById<View>(R.id.tv_day_of_week) as TextView
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
        holder.tvItemGridView!!.text = "" + getItem(position)
        return convertView
    }

    private inner class ViewHolder {
        var tvItemGridView: TextView? = null
    }
}