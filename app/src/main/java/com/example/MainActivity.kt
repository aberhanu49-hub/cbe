package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.ui.CbeAppContent
import com.example.ui.CbeViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: CbeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize the central CBE state model
        viewModel = ViewModelProvider(this)[CbeViewModel::class.java]
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CbeAppContent(viewModel = viewModel)
            }
        }
    }
}
