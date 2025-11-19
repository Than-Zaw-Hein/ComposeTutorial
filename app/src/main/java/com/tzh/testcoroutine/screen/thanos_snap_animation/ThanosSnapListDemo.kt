package com.tzh.testcoroutine.screen.thanos_snap_animation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThanosSnapListDemo() {
    // 5. Manage the list of items
    val items = remember {
        (1..10).map { ListItem(it, "Item #$it") }.toMutableStateList()
    }
    // 6. Manage the items currently in the middle of a snap (deletion)
    val snappingItems = remember { mutableStateMapOf<Int, Boolean>() }

    // Create a mutable list of all items (both present and snapping) for the keys
    val allItemKeys = remember(items, snappingItems) {
        (items.map { it.id } + snappingItems.keys).distinct()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Thanos Snap Animation Demo",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues = padding)) {
            items(
                items = allItemKeys,
                key = { it } // Use a stable key (the item's ID)
            ) { itemId ->
                // Find the item object. Null if it's already deleted and only snapping.
                val item = items.find { it.id == itemId }

                val isSnapping = snappingItems.containsKey(itemId)
                val isPresent = item != null

                // Only show the row if the item is present OR is currently snapping
                if (isPresent || isSnapping) {
                    // Determine the content to show inside the snap effect
                    val itemContent: @Composable () -> Unit = {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .animateItem(),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        // Trigger snap on click
                                        if (!isSnapping) {
                                            snappingItems[itemId] = true
                                        }
                                    }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = item?.text ?: "Snapping...", // Show item text if present
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    ThanosSnapEffect(
                        isSnapping = isSnapping,
                        onAnimationFinished = {
                            // 7. Remove the item from both lists after animation finishes
                            snappingItems.remove(itemId)
                            items.remove(item)
                        }
                    ) {
                        itemContent()
                    }
                }
            }
        }
    }
}
