package io.flaterlab.smsbroadcast.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import io.flaterlab.smsbroadcast.R
import io.flaterlab.smsbroadcast.SmsBroadcastService
import io.flaterlab.smsbroadcast.databinding.FragmentHomeBinding
import io.flaterlab.smsbroadcast.domain.ServiceStatus

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.serviceStatus.observe(viewLifecycleOwner) { status ->
            binding.btnToggleButton.text = getString(
                when (status) {
                    ServiceStatus.ON -> R.string.stop
                    else -> R.string.start
                }
            )
            Log.d("Mylog", "Status: $status")
            if (status == ServiceStatus.ON) {
                SmsBroadcastService.start(requireActivity())
            }
        }

        binding.btnToggleButton.setOnClickListener {
            viewModel.onToggleClicked()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}