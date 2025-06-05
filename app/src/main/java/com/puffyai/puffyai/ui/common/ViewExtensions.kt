package com.puffyai.puffyai.ui.common

import android.view.View

/**
 * Extension function to set visibility of a View.
 */
fun View.setVisible(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.GONE
}

/**
 * Extension function to enable/disable a View.
 */
fun View.setEnabled(enabled: Boolean) {
    this.isEnabled = enabled
    this.alpha = if (enabled) 1.0f else 0.5f // Optional: visual feedback for disabled state
}