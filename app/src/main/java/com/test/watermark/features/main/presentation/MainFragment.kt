package com.test.watermark.features.main.presentation

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import com.test.watermark.R
import com.test.watermark.common.BaseFragment
import com.test.watermark.databinding.FragmentMainBinding
import com.test.watermark.extensions.showNow
import com.test.watermark.features.main.di.provideMainViewModel

class MainFragment : BaseFragment() {

    override val layoutRes: Int = R.layout.fragment_main

    private lateinit var binding: FragmentMainBinding
    private lateinit var mainRenderer: MainRenderer
    private lateinit var camera: MainCamera

    private val recordingAnimator by lazy(LazyThreadSafetyMode.NONE) {
        ValueAnimator.ofFloat(1f, 0.8f).apply {
            duration = 300
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener { animation ->
                val scale = animation.animatedValue as Float
                binding.tvRecoding.scaleX = scale
                binding.tvRecoding.scaleY = scale
            }
        }
    }

    private val viewModel: MainViewModel by lazy(LazyThreadSafetyMode.NONE) { provideMainViewModel() }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        camera = MainCameraImpl(context)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMainBinding.bind(view)
        mainRenderer = MainRenderer(
            context = requireContext(),
            camera = camera,
            glSurfaceView = binding.glSurfaceViewMain
        )
        with(binding) {
            glSurfaceViewMain.apply {
                setEGLContextClientVersion(2)
                setRenderer(mainRenderer)
                renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
            }
            ivRecordMain.setOnTouchListener { _, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> viewModel.onRecordButtonPressed()
                    MotionEvent.ACTION_UP -> viewModel.onRecordButtonReleased()
                }
                true
            }
        }
        initObservers()
    }

    override fun onStart() {
        super.onStart()
        recordingAnimator.start()
    }

    override fun onResume() {
        super.onResume()
        binding.glSurfaceViewMain.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.glSurfaceViewMain.onPause()
    }

    override fun onStop() {
        super.onStop()
        recordingAnimator.cancel()
        binding.glSurfaceViewMain.queueEvent { mainRenderer.stop() }
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    private fun initObservers() {
        viewModel.recordingState.observe(viewLifecycleOwner) { state ->
            mainRenderer.setScreenState(state)
        }
        viewModel.recordingText.observe(viewLifecycleOwner) {
            binding.tvRecoding.showNow(it)
        }
    }
}
