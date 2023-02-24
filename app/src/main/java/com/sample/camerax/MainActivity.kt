package com.sample.camerax

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.sample.camerax.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

	companion object {
		private const val REQUEST_CODE_PERMISSIONS = 10

		private val REQUIRED_PERMISSIONS = arrayOf(
			Manifest.permission.CAMERA,
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.WRITE_EXTERNAL_STORAGE
		)
	}

	private lateinit var binding: ActivityMainBinding

	private val imageCapture: ImageCapture by lazy {
		ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
	}

	private lateinit var cameraExecutor: ExecutorService

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		cameraExecutor = Executors.newSingleThreadExecutor()

		if (allPermissionsGranted()) {
			startCamera()
		} else {
			ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
		}

		binding.shutterButton.setOnClickListener {
			takePicture()
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		cameraExecutor.shutdown()
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
		cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview, imageCapture)
	}

	private fun takePicture() {
		val contentValues = ContentValues()
		contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, SimpleDateFormat("yyyyMMddHHmmss").format(Date()) + ".jpg")
		contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")

		val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
			contentResolver,
			MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
			contentValues
		).build()

		imageCapture.takePicture(
			outputFileOptions,
			ContextCompat.getMainExecutor(this),
			object : ImageCapture.OnImageSavedCallback {
				override fun onError(error: ImageCaptureException) {
					setToast("error")
				}

				override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
					setToast("success")
				}
			})
	}

	/**
	 *  トースト作成
	 *
	 *  @param sentence テキスト
	 */
	private fun setToast(sentence: String) {
		val ts = Toast.makeText(this, sentence, Toast.LENGTH_LONG)
		ts.setGravity(Gravity.BOTTOM, 0, 320)
		ts.show()
	}
}