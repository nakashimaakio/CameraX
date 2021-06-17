package com.sample.camerax

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.sample.camerax.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

	companion object {
		private const val REQUEST_CODE_PERMISSIONS = 10
		private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
	}

	private lateinit var binding: ActivityMainBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
		if (allPermissionsGranted()) {
			startCamera()
		} else {
			ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
		}
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (requestCode == REQUEST_CODE_PERMISSIONS) {
			if (allPermissionsGranted()) {
				startCamera()
			} else {
				Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
				finish()
			}
		}
	}

	private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
		ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
	}

	private fun startCamera() {
		val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
		cameraProviderFuture.addListener({ bindPreview(cameraProviderFuture.get()) }, ContextCompat.getMainExecutor(this))
	}

	private fun bindPreview(cameraProvider: ProcessCameraProvider) {
		val preview = Preview.Builder().build()
		val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
		preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)
		cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview)
	}
}