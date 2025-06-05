package com.puffyai.puffyai.ui.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

/**
 * Base Activity to provide common functionality like showing Snackbars.
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * Shows a Snackbar message.
     * @param message The message to display.
     * @param isError True if the message is an error, false otherwise.
     */
    fun showSnackbar(message: String, isError: Boolean = false) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        if (isError) {
            // Optionally set a different background color for errors
            // snackbar.view.setBackgroundColor(ContextCompat.getColor(this, R.color.error_color))
        }
        snackbar.show()
    }
}