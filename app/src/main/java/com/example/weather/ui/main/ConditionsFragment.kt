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
import com.example.weather.databinding.ConditionsFragmentBinding
import com.example.weather.databinding.DetailedConditionsFragmentBinding
import com.example.weather.databinding.DetailedForecastFragementBinding
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.conditions_fragment.view.*

class ConditionsFragment: Fragment() {

    lateinit var viewModel: ConditionsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel = ViewModelProvider(this)[ConditionsViewModel::class.java]
        val binding = ConditionsFragmentBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        val recyclerView = binding.root.recyclerView as RecyclerView
        val itemDecor = DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
        recyclerView.addItemDecoration(itemDecor)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        val adapter = ConditionsAdapter()
        recyclerView.adapter = adapter

        viewModel.conditions.observe(viewLifecycleOwner, Observer { _ -> adapter.notifyDataSetChanged() })
        viewModel.forecasts.observe(viewLifecycleOwner, Observer { _ -> adapter.notifyDataSetChanged() })

        return binding.root
    }

    inner class ConditionsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ConditionsCardBinding.inflate(inflater)
            val viewHolder = ViewHolder(binding)
            val lp = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            viewHolder.itemView.layoutParams = lp
            return viewHolder
        }

        override fun getItemCount(): Int = viewModel.count()

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val viewHolder = holder as ViewHolder
            viewHolder.bind(viewModel[position])
        }

        inner class ViewHolder(val binding: ConditionsCardBinding) : RecyclerView.ViewHolder(binding.root) {

            fun bind(viewModel:ConditionsViewModel.Conditions) {
                binding.viewModel = viewModel
                binding.viewHolder = this
                binding.invalidateAll()
                binding.executePendingBindings()

                if (viewModel.icon != null)
                    Picasso.with(context).load(viewModel.icon!!).into(binding.icon)
            }

            fun onClick(view:View) {
                val recyclerView = this@ConditionsFragment.view!!.recyclerView as RecyclerView
                val index = recyclerView.getChildLayoutPosition(view)

                if (viewModel.conditions.value != null) {
                    if (index == 0)
                        showConditionsDetails()
                    else
                        showForecastDetails(index - 1)
                }

                else if (viewModel.forecasts.value != null)
                    showForecastDetails(index)
            }

            private fun showConditionsDetails() {
                val inflater = LayoutInflater.from(context)
                val binding = DetailedConditionsFragmentBinding.inflate(inflater)
                val viewModel = ViewModelProvider(this@ConditionsFragment)[DetailedConditionsViewModel::class.java]
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

            private fun showForecastDetails(index: Int) {
                val inflater = LayoutInflater.from(context)
                val binding = DetailedForecastFragementBinding.inflate(inflater)
                val viewModel = ViewModelProvider(this@ConditionsFragment)[DetailedForecastViewModel::class.java]
                viewModel.index = index
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
