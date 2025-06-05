package com.puffyai.puffyai.ui.purchase

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.puffyai.puffyai.R
import com.puffyai.puffyai.databinding.ActivityStoreBinding
import com.puffyai.puffyai.ui.common.BaseActivity
import com.puffyai.puffyai.ui.common.setEnabled
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PurchaseActivity : BaseActivity() {

    private lateinit var binding: ActivityStoreBinding
    private val viewModel: PurchaseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()

        viewModel.queryAvailableProducts()
    }

    private fun setupListeners() {
        binding.purchase5Button.setOnClickListener {
            viewModel.purchaseProduct("puffy_5_generations")
        }

        binding.purchase10Button.setOnClickListener {
            viewModel.purchaseProduct("puffy_10_generations")
        }

        binding.purchase20Button.setOnClickListener {
            viewModel.purchaseProduct("puffy_20_generations")
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    when (uiState) {
                        PurchaseViewModel.UiState.Idle -> {
                            binding.progressBar.visibility = View.GONE
                            // Enable buttons if needed
                        }
                        is PurchaseViewModel.UiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.purchase5Button.setEnabled(false)
                            binding.purchase10Button.setEnabled(false)
                            binding.purchase20Button.setEnabled(false)
                        }
                        is PurchaseViewModel.UiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            binding.purchase5Button.setEnabled(true)
                            binding.purchase10Button.setEnabled(true)
                            binding.purchase20Button.setEnabled(true)
                            showSnackbar(uiState.message)
                        }
                        is PurchaseViewModel.UiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.purchase5Button.setEnabled(true)
                            binding.purchase10Button.setEnabled(true)
                            binding.purchase20Button.setEnabled(true)
                            showSnackbar(uiState.message, true)
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.availableProducts.collect { products ->
                    // Update UI with product details (e.g., prices)
                    products.forEach { product ->
                        when (product.productId) {
                            "puffy_5_generations" -> binding.purchase5Button.text = "Buy 5 Generations (${product.oneTimePurchaseOfferDetails?.formattedPrice})"
                            "puffy_10_generations" -> binding.purchase10Button.text = "Buy 10 Generations (${product.oneTimePurchaseOfferDetails?.formattedPrice})"
                            "puffy_20_generations" -> binding.purchase20Button.text = "Buy 20 Generations (${product.oneTimePurchaseOfferDetails?.formattedPrice})"
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentCredits.collect { credits ->
                    binding.currentCreditsTextView.text = "Current Credits: $credits"
                }
            }
        }
    }
}