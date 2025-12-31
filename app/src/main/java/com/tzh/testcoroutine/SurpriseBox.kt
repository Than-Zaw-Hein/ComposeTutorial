@Composable
fun SurpriseBox() {
    var isOpen by remember { mutableStateOf(false) }

    // Create a transition to sync all animations
    val transition = updateTransition(targetState = isOpen, label = "BoxTransition")

    // 1. Lid Rotation Animation
    val lidRotation by transition.animateFloat(
        transitionSpec = { spring(stiffness = Spring.StiffnessLow) },
        label = "LidRotation"
    ) { state -> if (state) -120f else 0f }

    // 2. Item Scale Animation
    val itemScale by transition.animateFloat(
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioMediumBouncy) },
        label = "ItemScale"
    ) { state -> if (state) 1f else 0f }

    // 3. Item Offset (Rising up)
    val itemOffset by transition.animateDp(
        label = "ItemOffset"
    ) { state -> if (state) (-100).dp else 0.dp }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null // Remove ripple for cleaner feel
                ) { isOpen = !isOpen },
            contentAlignment = Alignment.BottomCenter
        ) {

            // --- The Surprise Item ---
            Text(
                text = "🎁", // Replace with an Image or Icon
                fontSize = 80.sp,
                modifier = Modifier
                    .offset(y = itemOffset)
                    .scale(itemScale)
            )

            // --- The Box Body ---
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color(0xFFFBC02D), RoundedCornerShape(4.dp))
                    .zIndex(2f) // Ensure body is in front of the item initially
            )

            // --- The Box Lid ---
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-100).dp) // Move to top of body
                    .graphicsLayer {
                        rotationZ = lidRotation
                        transformOrigin = TransformOrigin(0f, 1f) // Pivot from bottom-left
                    }
                    .size(110.dp, 30.dp)
                    .background(Color(0xFFF9A825), RoundedCornerShape(4.dp))
                    .zIndex(3f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(if (isOpen) "Click to Close" else "Click to Open!", fontWeight = FontWeight.Bold)
    }
}
