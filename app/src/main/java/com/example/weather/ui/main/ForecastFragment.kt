package com.example.weather.ui.main

import android.graphics.Rect
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.databinding.DetailedForecastFragementBinding
import com.example.weather.databinding.ForecastCardBinding
import com.example.weather.databinding.ForecastFragmentBinding
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.forecast_fragment.view.*
import java.lang.Exception


class ForecastFragment: Fragment() {

    lateinit var viewModel: ForecastViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel = ViewModelProvider(this)[ForecastViewModel::class.java]
        val binding = ForecastFragmentBinding.inflate(layoutInflater)
        binding.viewModel = viewModel

        val recyclerView = binding.root.recyclerView as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(activity)
        val adapter = ForecastAdapter()
        recyclerView.adapter = adapter

        viewModel.forecasts.observe(viewLifecycleOwner, Observer { _ ->
            adapter.notifyDataSetChanged()
        })

        return binding.root;
    }

    inner class ForecastAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ForecastCardBinding.inflate(inflater)
            val viewHolder = ViewHolder(binding)
            val lp = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            viewHolder.itemView.layoutParams = lp
            return viewHolder
        }

        override fun getItemCount(): Int = viewModel.count()

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val viewHolder = holder as ViewHolder
            viewHolder.binding.viewModel = viewModel[position]
            viewHolder.binding.viewHolder = viewHolder
            viewHolder.bind(position)
        }

        inner class ViewHolder(val binding: ForecastCardBinding) : RecyclerView.ViewHolder(binding.root) {

            fun bind(index:Int) {
                val forecast = viewModel[index]
                binding.name.text = forecast.name
                binding.temperatureLabel.text = forecast.temperatureLabel
                binding.temperature.text = forecast.temperature
                binding.wind.text = forecast.wind
                binding.shortForecast.text = forecast.shortForecast
                binding.forecastCard.setCardBackgroundColor(ContextCompat.getColor(context!!, forecast.backgroundColor))

                if (forecast.icon != null)
                    Picasso.with(context).load(forecast.icon!!).into(binding.icon)
            }

            fun onClick(view:View) {
                val recyclerView = this@ForecastFragment.view!!.recyclerView as RecyclerView
                val index = recyclerView.getChildLayoutPosition(view)

                val inflater = LayoutInflater.from(context)
                val binding = DetailedForecastFragementBinding.inflate(inflater)
                var viewModel = ViewModelProvider(this@ForecastFragment)[DetailedForecastViewModel::class.java]
                viewModel.index = index
                binding.viewModel = viewModel

                binding.detaildForecastCard.setCardBackgroundColor(ContextCompat.getColor(context!!, viewModel.backgroundColor))

                if (viewModel.icon != null)
                    Picasso.with(context).load(viewModel.icon!!).into(binding.icon)

                val pw = PopupWindow(binding.root, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
                pw.setTouchInterceptor { _, _ ->
                    pw.dismiss()
                    true
                }
                pw.showAtLocation(this@ForecastFragment.view, Gravity.CENTER, 0, 0)
            }
        }
    }
}

