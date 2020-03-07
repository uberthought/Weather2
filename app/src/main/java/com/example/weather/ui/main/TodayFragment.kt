package com.example.weather.ui.main

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.databinding.ConditionsFragmentBinding
import com.example.weather.databinding.DetailedForecastFragementBinding
import com.example.weather.databinding.TodayFragmentBinding
import com.squareup.picasso.Picasso

class TodayFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewModel = ViewModelProvider(this)[TodayViewModel::class.java]
        val binding = TodayFragmentBinding.inflate(getLayoutInflater())
        binding.viewModel = viewModel
        binding.fragment = this

        viewModel.location.observe(viewLifecycleOwner, Observer { text -> binding.location.text = text })

        viewModel.conditions.observe(viewLifecycleOwner, Observer { conditions ->
            binding.invalidateAll()

            if (conditions.icon != null)
                Picasso.with(this.context).load(conditions.icon!!).into(binding.icon)
        })

        viewModel.forecast.observe(viewLifecycleOwner, Observer { forecast ->
            binding.invalidateAll()

            binding.forecastCard.setCardBackgroundColor(ContextCompat.getColor(context!!, forecast.backgroundColor))

            if (forecast.icon != null)
                Picasso.with(this.context).load(forecast.icon!!).into(binding.forecastIcon)
        })

        return binding.root;
    }

    fun onClickConditions(view:View) {
        val inflater = LayoutInflater.from(context)
        val binding = ConditionsFragmentBinding.inflate(inflater)
        var viewModel = ViewModelProvider(this)[ConditionsViewModel::class.java]
        binding.viewModel = viewModel

        if (viewModel.icon != null)
            Picasso.with(context).load(viewModel.icon!!).into(binding.icon)

        val pw = PopupWindow(binding.root, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        pw.setTouchInterceptor { _, _ ->
            pw.dismiss()
            true
        }
        pw.showAtLocation(view, Gravity.CENTER, 0, 0)
    }

    fun onClickForecast(view:View) {
        val inflater = LayoutInflater.from(context)
        val binding = DetailedForecastFragementBinding.inflate(inflater)
        var viewModel = ViewModelProvider(this)[DetailedForecastViewModel::class.java]
        viewModel.index = 0
        binding.viewModel = viewModel

        binding.detaildForecastCard.setCardBackgroundColor(ContextCompat.getColor(context!!, viewModel.backgroundColor))

        if (viewModel.icon != null)
            Picasso.with(context).load(viewModel.icon!!).into(binding.icon)

        val pw = PopupWindow(binding.root, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        pw.setTouchInterceptor { _, _ ->
            pw.dismiss()
            true
        }
        pw.showAtLocation(view, Gravity.CENTER, 0, 0)
    }
}
