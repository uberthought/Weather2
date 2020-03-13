package com.example.weather.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.TransitionInflater
import com.example.weather.databinding.DetailedConditionsFragmentBinding
import com.squareup.picasso.Picasso

class DetailedConditionsFragment : Fragment() {
    private val logTag = javaClass.kotlin.qualifiedName

    private lateinit var conditionsViewModel: ConditionsViewModel
    private lateinit var forecastViewModel: ForecastViewModel
    private lateinit var viewModel: DataViewModel
    private lateinit var binding:DetailedConditionsFragmentBinding

    private val args: DetailedConditionsFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this)[DataViewModel::class.java]
        binding = DetailedConditionsFragmentBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.fragment = this

        binding.root.apply {
            postponeEnterTransition()
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }


        val index = args.index
        ViewCompat.setTransitionName(binding.title, "title_$index")
        ViewCompat.setTransitionName(binding.icon, "icon_$index")
        ViewCompat.setTransitionName(binding.temperature, "temperature_$index")
        ViewCompat.setTransitionName(binding.temperatureLabel, "temperatureLabel_$index")
        ViewCompat.setTransitionName(binding.wind, "wind_$index")
        ViewCompat.setTransitionName(binding.shortDescription, "shortDescription_$index")
        ViewCompat.setTransitionName(binding.dewPoint, "dewPoint_$index")
        ViewCompat.setTransitionName(binding.windGust, "windGust_$index")
        ViewCompat.setTransitionName(binding.relativeHumidity, "relativeHumidity_$index")

        conditionsViewModel = ViewModelProvider(this)[ConditionsViewModel::class.java]
        forecastViewModel = ViewModelProvider(this)[ForecastViewModel::class.java]
        conditionsViewModel.dataViewModel.observe(viewLifecycleOwner, Observer { viewModelChanged() })
        forecastViewModel.forecasts.observe(viewLifecycleOwner, Observer { viewModelChanged() })

        return binding.root
    }

    private fun viewModelChanged() {
        val index = args.index
        val viewModel = when {
            conditionsViewModel.dataViewModel.value != null && index == 0 -> conditionsViewModel.dataViewModel.value!!
            conditionsViewModel.dataViewModel.value == null -> forecastViewModel.forecasts.value!![index]
            forecastViewModel.forecasts.value != null -> forecastViewModel.forecasts.value!![index - 1]
            else -> DataViewModel()
        }
        binding.viewModel = viewModel
        binding.invalidateAll()

        if (viewModel.icon != null)
            Picasso.with(context).load(viewModel.icon!!).into(binding.icon)
    }

    fun onClick(view:View) {
        findNavController().popBackStack()
    }
}
