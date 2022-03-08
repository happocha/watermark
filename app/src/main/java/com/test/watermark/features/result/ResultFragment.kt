package com.test.watermark.features.result

import android.os.Bundle
import android.view.View
import android.widget.MediaController
import com.test.watermark.FILE_NAME
import com.test.watermark.R
import com.test.watermark.common.BaseFragment
import com.test.watermark.databinding.FragmentResultBinding
import com.test.watermark.features.result.di.provideResultViewModel

class ResultFragment : BaseFragment() {

    override val layoutRes: Int = R.layout.fragment_result

    private val viewModel: ResultViewModel by lazy(LazyThreadSafetyMode.NONE) { provideResultViewModel() }

    private lateinit var binding: FragmentResultBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentResultBinding.bind(view)
        val mediaController = MediaController(requireContext())
        binding.videoViewResult.apply {
            setMediaController(mediaController)
            setVideoPath("${requireContext().filesDir}/$FILE_NAME")
        }
        binding.ivBackResult.setOnClickListener { viewModel.onBackPressed() }
        initObservers()
        viewModel.init()
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    private fun initObservers() {
        viewModel.playVideo.observe(viewLifecycleOwner) {
            binding.videoViewResult.start()
        }
    }
}
