package com.example.weather.ui.main

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.databinding.ConditionsCardBinding
import com.example.weather.databinding.ConditionsCardBinding.bind
import com.example.weather.databinding.ConditionsCardBinding.inflate
import com.example.weather.databinding.ConditionsFragmentBinding
import com.example.weather.databinding.DetailedConditionsCardBinding
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.conditions_fragment.*
import kotlinx.android.synthetic.main.conditions_fragment.view.*

class ConditionsFragment: Fragment() {

    lateinit var locationViewModel: LocationViewModel
    lateinit var conditionsViewModel: ConditionsViewModel
    lateinit var forecastViewModel: ForecastViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        locationViewModel = ViewModelProvider(this)[LocationViewModel::class.java]
        conditionsViewModel = ViewModelProvider(this)[ConditionsViewModel::class.java]
        forecastViewModel = ViewModelProvider(this)[ForecastViewModel::class.java]
        val binding = ConditionsFragmentBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        binding.locationViewModel = locationViewModel
        binding.conditionsViewModel = conditionsViewModel

        val recyclerView = binding.root.recyclerView as RecyclerView
        val itemDecor = DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
        recyclerView.addItemDecoration(itemDecor)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        val adapter = ConditionsAdapter()
        recyclerView.adapter = adapter

        locationViewModel.location.observe(viewLifecycleOwner, Observer { location ->
        })
        conditionsViewModel.dataViewModel.observe(viewLifecycleOwner, Observer { _ -> adapter.notifyDataSetChanged() })
        forecastViewModel.forecasts.observe(viewLifecycleOwner, Observer { _ -> adapter.notifyDataSetChanged() })

        return binding.root
    }

    inner class ConditionsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = inflate(inflater)
            val viewHolder = ViewHolder(binding)
            val lp = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            viewHolder.itemView.layoutParams = lp
            return viewHolder
        }

        override fun getItemCount(): Int {
            var count = 0
            if (conditionsViewModel.dataViewModel.value != null) count++
            if (forecastViewModel.forecasts.value != null) count += forecastViewModel.forecasts.value!!.count()
            return count
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val viewHolder = holder as ViewHolder
            val viewModel = when {
                conditionsViewModel.dataViewModel.value != null && position == 0 -> conditionsViewModel.dataViewModel.value!!
                conditionsViewModel.dataViewModel.value == null -> forecastViewModel.forecasts.value!![position]
                forecastViewModel.forecasts.value != null -> forecastViewModel.forecasts.value!![position - 1]
                else -> DataViewModel()
            }
            viewHolder.bind(viewModel)
        }

        inner class ViewHolder(val binding: ConditionsCardBinding) : RecyclerView.ViewHolder(binding.root) {

            fun bind(viewModel:DataViewModel) {
                binding.viewModel = viewModel
                binding.viewHolder = this

                if (viewModel.icon != null)
                    Picasso.with(context).load(viewModel.icon!!).into(binding.icon)
            }

            fun onClick(view:View) {
                val recyclerView = this@ConditionsFragment.view!!.recyclerView as RecyclerView
                val position = recyclerView.getChildLayoutPosition(view)

                val inflater = LayoutInflater.from(context)
                val binding = DetailedConditionsCardBinding.inflate(inflater)
                val viewModel = when {
                    conditionsViewModel.dataViewModel.value != null && position == 0 -> conditionsViewModel.dataViewModel.value!!
                    conditionsViewModel.dataViewModel.value == null -> forecastViewModel.forecasts.value!![position]
                    forecastViewModel.forecasts.value != null -> forecastViewModel.forecasts.value!![position - 1]
                    else -> DataViewModel()
                }
                binding.viewModel = viewModel

                if (viewModel.icon != null)
                    Picasso.with(context).load(viewModel.icon!!).into(binding.icon)

                val pw = PopupWindow(binding.root, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
                pw.setTouchInterceptor { _, _ ->
                    pw.dismiss()
                    true
                }
                pw.showAtLocation(this@ConditionsFragment.view, Gravity.CENTER, 0, 0)
            }
        }
    }
}
