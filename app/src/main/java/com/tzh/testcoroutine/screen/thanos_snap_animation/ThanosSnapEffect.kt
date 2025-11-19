package com.tzh.testcoroutine.screen.thanos_snap_animation


import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.delay
import kotlin.random.Random

// The duration of the snap animation in milliseconds
const val SNAP_DURATION = 1500L
// The number of particles to generate
const val PARTICLE_COUNT = 300
/**
 * A composable that applies the Thanos Snap disintegration effect to its content.
 *
 * @param isSnapping A state variable that, when true, triggers the animation.
 * @param onAnimationFinished A callback function to execute after the animation completes.
 * @param content The composable content to be displayed and snapped.
 */
@Composable
fun ThanosSnapEffect(
    isSnapping: Boolean,
    onAnimationFinished: () -> Unit,
    content: @Composable () -> Unit
) {
    // 2. State for holding the captured image (Bitmap) and the generated particles
    var capturedImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var particles by remember { mutableStateOf(emptyList<Particle>()) }
    var contentSize by remember { mutableStateOf(Size.Zero) }

    // 3. Animation time value (0f to 1f)
    val animatedTime by animateFloatAsState(
        targetValue = if (isSnapping) 1f else 0f,
        animationSpec = tween(SNAP_DURATION.toInt(), easing = LinearEasing),
        label = "snapTime"
    )

    // 4. LaunchedEffect to trigger particle generation and cleanup
    LaunchedEffect(isSnapping) {
        if (isSnapping && capturedImage != null) {
            // Generate particles when the snap starts
            particles = generateParticlesLR(capturedImage!!, contentSize)
            // Wait for the animation to complete
            delay(SNAP_DURATION)
            onAnimationFinished()
            // Reset state for potential reuse (though typically not reused for deleted items)
            particles = emptyList()
            capturedImage = null
        }
    }

    // A box to hold the content and the canvas overlay
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                contentSize = Size(
                    width = coordinates.size.width.toFloat(),
                    height = coordinates.size.height.toFloat()
                )
            }
    ) {
        if (isSnapping && capturedImage != null) {
            // If snapping, draw the animated particles over a blank space
            SnapCanvas(
                particles = particles,
                snapTime = animatedTime,
                contentSize = contentSize
            )
        } else if (!isSnapping) {
            // If not snapping, draw the original content and capture its image
            val captureModifier = Modifier.drawWithContent {
                // Draw the actual content
                drawContent()

                // Capture the content as a Bitmap before the first frame of the snap
                if (capturedImage == null && !isSnapping) {
                    capturedImage = captureContentAsBitmap(size)
                }
            }
            Box(modifier = captureModifier) {
                content()
            }
        }
        // When isSnapping is true but capturedImage is null, we briefly display nothing
        // which gives the illusion of content disappearing before the particles show.
    }
}

/**
 * Draws the animated particles on a Canvas.
 */
@Composable
private fun SnapCanvas(
    particles: List<Particle>,
    snapTime: Float,
    contentSize: Size
) {
    // We use the full size of the original item for the canvas
    val density = LocalDensity.current
    val minHeightDp = with(density) { contentSize.height.toDp() }
    val minWidthDp = with(density) { contentSize.width.toDp() }

    Canvas(
        modifier = Modifier
            .width(minWidthDp)
            .height(minHeightDp)
    ) {
        particles.forEach { particle ->
            // Update particle position and alpha based on the animated time
            val t = snapTime
            val x = particle.x + particle.velocityX * t
            val y =
                particle.y + particle.velocityY * t + 0.5f * 9.8f * t * t * 100 // Gravity effect
            val alpha = particle.alpha * (1f - t) // Fade out

            drawCircle(
                color = particle.color,
                radius = particle.size,
                center = Offset(x, y),
                alpha = alpha
            )
        }
    }
}

/**
 * Helper function to generate particles from the captured image.
 */
private fun generateParticles(imageBitmap: ImageBitmap, contentSize: Size): List<Particle> {
    val pixels = IntArray(imageBitmap.width * imageBitmap.height)
    imageBitmap.readPixels(pixels)

    val particleList = mutableListOf<Particle>()
    val random = Random(System.currentTimeMillis())

    // Scale factors to map bitmap coordinates back to Compose's density-independent pixel (DP) space
    val scaleX = contentSize.width / imageBitmap.width
    val scaleY = contentSize.height / imageBitmap.height

    for (i in 0 until PARTICLE_COUNT) {
        // Randomly pick a pixel position from the bitmap
        val pixelIndex = random.nextInt(pixels.size)
        val pixelColor = pixels[pixelIndex]

        // Get the color and check if it's transparent/black (skip transparent)
        if (pixelColor == 0) continue

        // Calculate (x, y) coordinates corresponding to the pixel index
        val pixelX = pixelIndex % imageBitmap.width
        val pixelY = pixelIndex / imageBitmap.width

        // Convert pixel coordinates to Composable coordinates
        val startX = pixelX * scaleX
        val startY = pixelY * scaleY

        // Generate random velocity to make particles fly in different directions
        val angle = random.nextFloat() * 2 * Math.PI.toFloat()
        val speed = random.nextFloat() * 100f + 50f
        val velocityX = speed * kotlin.math.cos(angle)
        val velocityY = speed * kotlin.math.sin(angle) - 150f // Bias upward initially

        particleList.add(
            Particle(
                x = startX,
                y = startY,
                size = random.nextFloat() * 4f + 2f, // Particle size
                color = Color(pixelColor),
                velocityX = velocityX,
                velocityY = velocityY,
                alpha = random.nextFloat() * 0.5f + 0.5f // Random initial alpha
            )
        )
    }
    return particleList
}

private fun generateParticlesLR(imageBitmap: ImageBitmap, contentSize: Size): List<Particle> {
    val pixels = IntArray(imageBitmap.width * imageBitmap.height)
    imageBitmap.readPixels(pixels)

    val particleList = mutableListOf<Particle>()
    val random = Random(System.currentTimeMillis())

    val scaleX = contentSize.width / imageBitmap.width
    val scaleY = contentSize.height / imageBitmap.height

    // --- New Constants for Left-to-Right Bias ---
    val BASE_HORIZONTAL_SPEED = 200f
    val RANDOM_SPEED_JITTER = 100f
    val VERTICAL_BIAS = -150f // Initial upward push

    for (i in 0 until PARTICLE_COUNT) {
        val pixelIndex = random.nextInt(pixels.size)
        val pixelColor = pixels[pixelIndex]

        if (pixelColor == 0) continue

        val pixelX = pixelIndex % imageBitmap.width
        val pixelY = pixelIndex / imageBitmap.width

        val startX = pixelX * scaleX
        val startY = pixelY * scaleY

        // 1. Calculate Velocity X (Horizontal)
        // Ensure velocity is always positive (moving right)
        val velocityX = BASE_HORIZONTAL_SPEED + random.nextFloat() * RANDOM_SPEED_JITTER

        // 2. Calculate Velocity Y (Vertical)
        // Add an initial upward push (negative value) and some randomness
        val velocityY = VERTICAL_BIAS + random.nextFloat() * 200f // Randomness between -150 and +50

        particleList.add(
            Particle(
                x = startX,
                y = startY,
                size = random.nextFloat() * 4f + 2f,
                color = Color(pixelColor),
                velocityX = velocityX, // Always positive -> moves right
                velocityY = velocityY,
                alpha = random.nextFloat() * 0.5f + 0.5f
            )
        )
    }
    return particleList
}

/**
 * Utility to capture content as a Bitmap using Modifier.graphicsLayer.
 *
 * NOTE: This is a simplified capture for demonstration. In a real app,
 * you might need a more robust solution for complex layouts or cross-platform concerns.
 */
private fun captureContentAsBitmap(size: Size): ImageBitmap {
    // Create a new ImageBitmap with the size of the content
    val width = size.width.toInt().coerceAtLeast(1)
    val height = size.height.toInt().coerceAtLeast(1)

    // Using a placeholder graphic as actual full Bitmap capture from Composable content
    // requires a more complex `AndroidComposeView` setup which is too complex for a single file example.
    // For this example, we generate a simple gradient placeholder that looks like content.
    val bitmap = ImageBitmap(width, height, ImageBitmapConfig.Argb8888)
    val canvas = android.graphics.Canvas(bitmap.asAndroidBitmap())
    val paint = Paint().apply {
        shader = LinearGradientShader(
            Offset(0f, 0f),
            Offset(width.toFloat(), height.toFloat()),
            listOf(Color.Red, Color.Blue, Color.Yellow, Color.Green),
            listOf(0f, 0.3f, 0.6f, 1f),
            tileMode = TileMode.Clamp
        )
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint.asFrameworkPaint())

    // In a real application, you would use a `PixelCopy` or similar method
    // to copy the rendered Composable content into the bitmap.
    return bitmap
}