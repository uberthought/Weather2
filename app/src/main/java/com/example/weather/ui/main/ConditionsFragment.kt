package com.example.weather.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.databinding.ConditionsCardBinding
import com.example.weather.databinding.ConditionsCardBinding.inflate
import com.example.weather.databinding.ConditionsFragmentBinding
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.conditions_fragment.*
import kotlinx.android.synthetic.main.conditions_fragment.view.*

class ConditionsFragment: Fragment() {

    private lateinit var locationViewModel: LocationViewModel
    private lateinit var conditionsViewModel: ConditionsViewModel
    private lateinit var forecastViewModel: ForecastViewModel

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
        recyclerView.apply {
            this.adapter = adapter
            postponeEnterTransition()
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }

        locationViewModel.location.observe(viewLifecycleOwner, Observer { location.invalidate() })
        conditionsViewModel.dataViewModel.observe(viewLifecycleOwner, Observer { adapter.notifyDataSetChanged() })
        forecastViewModel.forecasts.observe(viewLifecycleOwner, Observer { adapter.notifyDataSetChanged() })

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

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, index: Int) {
            val viewHolder = holder as ViewHolder
            viewHolder.bind(index)
        }

        inner class ViewHolder(private val binding: ConditionsCardBinding) : RecyclerView.ViewHolder(binding.root) {

            private var index: Int = -1
            lateinit var viewModel: DataViewModel

            fun bind(index: Int) {
                this.index = index

                viewModel = when {
                    conditionsViewModel.dataViewModel.value != null && index == 0 -> conditionsViewModel.dataViewModel.value!!
                    conditionsViewModel.dataViewModel.value == null -> forecastViewModel.forecasts.value!![index]
                    forecastViewModel.forecasts.value != null -> forecastViewModel.forecasts.value!![index - 1]
                    else -> DataViewModel()
                }

                binding.viewModel = viewModel
                binding.viewHolder = this
                ViewCompat.setTransitionName(binding.title, "title_$index")
                ViewCompat.setTransitionName(binding.icon, "icon_$index")
                ViewCompat.setTransitionName(binding.temperature, "temperature_$index")
                ViewCompat.setTransitionName(binding.temperatureLabel, "temperatureLabel_$index")
                ViewCompat.setTransitionName(binding.wind, "wind_$index")
                ViewCompat.setTransitionName(binding.shortDescription, "shortDescription_$index")
                ViewCompat.setTransitionName(binding.dewPoint, "dewPoint_$index")
                ViewCompat.setTransitionName(binding.windGust, "windGust_$index")
                ViewCompat.setTransitionName(binding.relativeHumidity, "relativeHumidity_$index")

                if (viewModel.icon != null)
                    Picasso.with(context).load(viewModel.icon!!).into(binding.icon)
            }

            fun onClick(view:View) {
                val extras = FragmentNavigatorExtras(
                    binding.title to "title_$index",
                    binding.icon to "icon_$index",
                    binding.temperature to "temperature_$index",
                    binding.temperatureLabel to "temperatureLabel_$index",
                    binding.wind to "wind_$index",
                    binding.shortDescription to "shortDescription_$index",
                    binding.dewPoint to "dewPoint_$index",
                    binding.windGust to "windGust_$index",
                    binding.relativeHumidity to "relativeHumidity_$index"
                )

                val action = ConditionsFragmentDirections.actionConditionsFragmentToDetailedConditions(index)
                findNavController().navigate(action, extras)
            }
        }
    }
}
