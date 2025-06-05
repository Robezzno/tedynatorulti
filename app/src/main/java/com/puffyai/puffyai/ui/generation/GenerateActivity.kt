package com.puffyai.puffyai.ui.generation

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.puffyai.puffyai.R
import com.puffyai.puffyai.databinding.ActivityGenerateBinding
import com.puffyai.puffyai.ui.common.BaseActivity
import com.puffyai.puffyai.ui.common.setEnabled
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GenerateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGenerateBinding
    private val viewModel: GenerateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenerateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUriString = intent.getStringExtra("imageUri")
        val imageUri = Uri.parse(imageUriString)

        if (imageUri != null) {
            viewModel.generateImage(imageUri, contentResolver)
        } else {
            showSnackbar(getString(R.string.error_no_image_selected), true)
            finish()
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    when (uiState) {
                        GenerateViewModel.UiState.Idle -> {
                            binding.progressBar.visibility = View.GONE
                            binding.generationStatusTextView.text = ""
                            binding.generateButton.visibility = View.GONE
                        }
                        is GenerateViewModel.UiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.generationStatusTextView.text = uiState.message
                            binding.generatedImageView.visibility = View.GONE
                            binding.generateButton.setEnabled(false)
                        }
                        is GenerateViewModel.UiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            binding.generationStatusTextView.text = getString(R.string.image_generated_successfully)
                            binding.generatedImageView.visibility = View.VISIBLE
                            binding.generateButton.setEnabled(true) // Show button to generate another

                            Glide.with(this@GenerateActivity)
                                .load(uiState.imageUrl)
                                .placeholder(R.drawable.placeholder_image)
                                .into(binding.generatedImageView)

                            showSnackbar(getString(R.string.image_generated_successfully))
                        }
                        is GenerateViewModel.UiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.generationStatusTextView.text = getString(R.string.error_generating_image, uiState.message)
                            binding.generatedImageView.visibility = View.GONE
                            binding.generateButton.setEnabled(true) // Allow retry
                            showSnackbar(getString(R.string.an_error_occurred, uiState.message), true)
                        }
                    }
                }
            }
        }

        binding.generateButton.setOnClickListener {
            // Re-trigger generation with the same image URI or allow user to select new
            val imageUriString = intent.getStringExtra("imageUri")
            val imageUri = Uri.parse(imageUriString)
            if (imageUri != null) {
                viewModel.generateImage(imageUri, contentResolver)
            } else {
                showSnackbar(getString(R.string.error_no_image_selected), true)
                finish()
            }
        }
    }
}