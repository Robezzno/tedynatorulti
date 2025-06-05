package com.puffyai.puffyai.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.ads.MobileAds
import com.puffyai.puffyai.R
import com.puffyai.puffyai.databinding.ActivityMainBinding
import com.puffyai.puffyai.ui.common.BaseActivity
import com.puffyai.puffyai.ui.common.setEnabled
import com.puffyai.puffyai.ui.generation.GenerateActivity
import com.puffyai.puffyai.ui.purchase.PurchaseActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private val REQUEST_CAMERA_PERMISSION = 100
    private val REQUEST_GALLERY_PERMISSION = 101

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            handleImageSelection(it)
        } ?: run {
            Toast.makeText(this, R.string.error_no_image_selected, Toast.LENGTH_SHORT).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        bitmap?.let {
            handleBitmapSelection(it)
        } ?: run {
            Toast.makeText(this, R.string.failed_to_get_image_uri_from_bitmap, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this) {}

        setupListeners()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateUsageStatus()
    }

    private fun setupListeners() {
        binding.selectImageButton.setOnClickListener {
            showImageSelectionDialog()
        }

        binding.watchAdButton.setOnClickListener {
            viewModel.watchAdForCredit {
                showSnackbar(getString(R.string.credit_added_successfully))
            }
        }

        binding.purchasePacksButton.setOnClickListener {
            val intent = Intent(this, PurchaseActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.usageStatus.collect { remaining ->
                    binding.usageStatusTextView.text = getString(R.string.images_remaining_today, remaining)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    when (uiState) {
                        MainViewModel.UiState.Idle -> {
                            binding.selectImageButton.setEnabled(true)
                            binding.watchAdButton.setEnabled(true)
                            binding.purchasePacksButton.setEnabled(true)
                        }
                        is MainViewModel.UiState.Loading -> {
                            binding.selectImageButton.setEnabled(false)
                            binding.watchAdButton.setEnabled(false)
                            binding.purchasePacksButton.setEnabled(false)
                            // Optionally show a progress indicator
                        }
                        is MainViewModel.UiState.Error -> {
                            binding.selectImageButton.setEnabled(true)
                            binding.watchAdButton.setEnabled(true)
                            binding.purchasePacksButton.setEnabled(true)
                            showSnackbar(uiState.message, true)
                        }
                        MainViewModel.UiState.Success -> {
                            binding.selectImageButton.setEnabled(true)
                            binding.watchAdButton.setEnabled(true)
                            binding.purchasePacksButton.setEnabled(true)
                            // Handle success if needed, e.g., navigate
                        }
                    }
                }
            }
        }
    }

    private fun showImageSelectionDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Select Image")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> checkCameraPermission()
                1 -> checkGalleryPermission()
            }
        }
        builder.show()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        } else {
            takePictureLauncher.launch(null)
        }
    }

    private fun checkGalleryPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_GALLERY_PERMISSION)
        } else {
            pickImageLauncher.launch("image/*")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePictureLauncher.launch(null)
                } else {
                    Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_GALLERY_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageLauncher.launch("image/*")
                } else {
                    Toast.makeText(this, R.string.gallery_permission_denied, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleImageSelection(imageUri: Uri) {
        if (!viewModel.canGenerateImage()) {
            Toast.makeText(this, R.string.daily_limit_reached, Toast.LENGTH_LONG).show()
            return
        }

        val intent = Intent(this, GenerateActivity::class.java).apply {
            putExtra("imageUri", imageUri.toString())
        }
        startActivity(intent)
    }

    private fun handleBitmapSelection(bitmap: Bitmap) {
        if (!viewModel.canGenerateImage()) {
            Toast.makeText(this, R.string.daily_limit_reached, Toast.LENGTH_LONG).show()
            return
        }

        val uri = getImageUri(bitmap)
        uri?.let {
            val intent = Intent(this, GenerateActivity::class.java).apply {
                putExtra("imageUri", it.toString())
            }
            startActivity(intent)
        } ?: run {
            Toast.makeText(this, R.string.failed_to_get_image_uri_from_bitmap, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getImageUri(inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }
}