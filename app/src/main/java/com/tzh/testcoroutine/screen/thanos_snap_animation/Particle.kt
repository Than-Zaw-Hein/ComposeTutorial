package com.tzh.testcoroutine.screen.thanos_snap_animation

import androidx.compose.ui.graphics.Color

// 1. Data class for an individual particle
data class Particle(
    var x: Float,
    var y: Float,
    var size: Float,
    var color: Color,
    var velocityX: Float,
    var velocityY: Float,
    var alpha: Float = 1f
)

