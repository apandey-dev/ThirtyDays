package com.minimal.thirtydays

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.minimal.thirtydays.ui.screens.HomeScreen
import com.minimal.thirtydays.ui.theme.ThirtyDaysTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ThirtyDaysTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    HomeScreen(
                        onDayClick = { dayItem ->
                            // Interaction hook for future card actions
                        }
                    )
                }
            }
        }
    }
}
