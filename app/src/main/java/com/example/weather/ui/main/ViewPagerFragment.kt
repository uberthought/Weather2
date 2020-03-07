package com.example.weather.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.weather.R
import com.google.android.material.tabs.TabLayout

class ViewPagerFragment : Fragment() {

    private lateinit var pagerAdapter: FragmentPagerAdapter
    private lateinit var viewPager: ViewPager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.viewpager, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        pagerAdapter = PagerAdapter(childFragmentManager)
        viewPager = view.findViewById(R.id.pager)
        viewPager.adapter = pagerAdapter

        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)
        tabLayout.setupWithViewPager(viewPager)
    }
}

class PagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    class Page(val name:String, val fragmentClass: Class<Fragment>)

    @Suppress("UNCHECKED_CAST")
    val pages:List<Page> = listOf(
        Page("Today", TodayFragment::class.java as Class<Fragment>),
        Page("Forecast", ForecastFragment::class.java as Class<Fragment>)
    )

    override fun getCount(): Int = pages.count()

    override fun getItem(i: Int): Fragment = pages[i].fragmentClass.constructors[0].newInstance() as Fragment

    override fun getPageTitle(i: Int): CharSequence = pages[i].name
}