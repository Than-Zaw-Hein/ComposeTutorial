package com.tzh.testcoroutine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tzh.testcoroutine.screen.thanos_snap_animation.ThanosSnapListDemo
import com.tzh.testcoroutine.ui.theme.TestCoroutineTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestCoroutineTheme {
                ThanosSnapListDemo()
//                CanvasArtScreen()
            }
        }
    }
}
