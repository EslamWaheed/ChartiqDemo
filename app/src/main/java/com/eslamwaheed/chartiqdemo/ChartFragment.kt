package com.eslamwaheed.chartiqdemo

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.chartiq.sdk.ChartIQ
import com.eslamwaheed.chartiqdemo.databinding.ChartFragmentBinding

class ChartFragment : Fragment() {
    private lateinit var binding: ChartFragmentBinding
    private val chartIQ: ChartIQ by lazy {
        (requireActivity().application as ChartDemoApplication).chartIQ
    }

    private val mainViewModel: MainViewModel by activityViewModels(factoryProducer = {
        MainViewModel.ViewModelFactory(
            ChartIQNetworkManager(),
            chartIQ,
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        )
    })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ChartFragmentBinding.inflate(inflater, container, false)
        setupViews()
        setChartIQView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel.updateSymbol(Symbol("1010-99-S"))
    }

    private fun setupViews() {
        mainViewModel.errorLiveData.observe(viewLifecycleOwner) {
            Toast.makeText(
                requireContext(),
                getString(R.string.general_warning_something_went_wrong),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setChartIQView() {
        chartIQ.chartView.apply {
            (parent as? FrameLayout)?.removeAllViews()
            binding.chartIqView.addView(this)
        }
    }

}