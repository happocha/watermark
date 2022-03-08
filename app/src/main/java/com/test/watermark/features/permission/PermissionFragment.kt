package com.test.watermark.features.permission

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.test.watermark.R
import com.test.watermark.common.BaseFragment
import com.test.watermark.features.permission.di.providePermissionViewModel

class PermissionFragment : BaseFragment() {

    override val layoutRes = R.layout.layout_container

    private val viewModel: PermissionViewModel by lazy(LazyThreadSafetyMode.NONE) { providePermissionViewModel() }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { permissions ->
        viewModel.permissionUpdated(permissions)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        permissionLauncher.launch(android.Manifest.permission.CAMERA)
    }

    private fun initObservers() {
        viewModel.showDialogMessage.observe(viewLifecycleOwner) {
            showAlertDialog(it)
        }
    }

    private fun showAlertDialog(message: String) {
        AlertDialog.Builder(requireContext()).apply {
            setMessage(message)
            setCancelable(false)
            setPositiveButton(
                android.R.string.ok
            ) { _, _ -> viewModel.onBackPressed() }
        }
            .create()
            .show()
    }
}
