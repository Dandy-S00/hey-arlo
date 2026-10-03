package com.example.arlo.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arlo.data.GeminiManager
import com.example.arlo.data.ResourceIntelligenceManager
import com.example.arlo.model.ArloState
import com.example.arlo.model.CuratedResource
import com.example.arlo.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ResourcesScreen(
    state: ArloState,
    resourceManager: ResourceIntelligenceManager,
    geminiManager: GeminiManager,
    onSaveToVault: (url: String, title: String, notes: String, tags: List<String>) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val resources by resourceManager.resourcesFlow.collectAsState()
    val isGenerating by resourceManager.isGeneratingFlow.collectAsState()

    var selectedCategoryFilter by remember { mutableStateOf("All") }

    val top3 = remember(resources, state) {
        if (resources.isNotEmpty()) resources.take(3) else resourceManager.getTop3ForState(state)
    }

    val categories = listOf("All", "Habits & Routine", "Tech & Dev", "Health & Wellness", "Learning & Growth")

    val filteredResources = remember(top3, selectedCategoryFilter) {
        if (selectedCategoryFilter == "All") top3
        else top3.filter { it.targetGoalCategory.equals(selectedCategoryFilter, ignoreCase = true) }
    }

    Scaffold(
        containerColor = ArloDarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = ArloPrimaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("💡", fontSize = 18.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Resource Radar",
                            style = MaterialTheme.typography.headlineMedium,
                            color = ArloTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Top 3 non-obvious hidden gems tailored for your current goal phase",
                        style = MaterialTheme.typography.labelSmall,
                        color = ArloTextMuted
                    )
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            resourceManager.discoverFreshWithGemini(state, geminiManager)
                            Toast.makeText(context, "Scouted fresh gems with Gemini AI ✦", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isGenerating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ArloPrimaryContainer,
                        contentColor = ArloPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArloPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("refresh_gems_button")
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = ArloPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("✦ Scout Gems", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text(cat, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ArloPrimaryContainer,
                            selectedLabelColor = ArloPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Top 3 Resources List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                itemsIndexed(filteredResources, key = { _, r -> r.id }) { index, resource ->
                    ResourceCard(
                        rank = index + 1,
                        resource = resource,
                        onOpenUrl = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resource.url)).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open link: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onSaveToLinksi = {
                            onSaveToVault(
                                resource.url,
                                resource.title,
                                "Curated by Arlo for phase '${resource.recommendedPhase}':\n${resource.summary}\n\nWhy relevant: ${resource.whyRelevantToYourPhase}",
                                resource.tags + listOf("CuratedGem", resource.targetGoalCategory)
                            )
                            resourceManager.markSaved(resource.id)
                            Toast.makeText(context, "Saved '${resource.title}' to Linksi Vault! 🔖", Toast.LENGTH_SHORT).show()
                        },
                        onCopyUrl = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Resource", resource.url))
                            Toast.makeText(context, "Copied link to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ResourceCard(
    rank: Int,
    resource: CuratedResource,
    onOpenUrl: () -> Unit,
    onSaveToLinksi: () -> Unit,
    onCopyUrl: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("resource_card_$rank"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ArloDarkSurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (rank == 1) ArloWarmGold.copy(alpha = 0.7f) else ArloBorder
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Rank badge & Hidden Gem indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = when (rank) {
                            1 -> ArloWarmGold
                            2 -> ArloPrimary
                            else -> ArloSecondary
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "#$rank",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ArloPrimaryContainer
                    ) {
                        Text(
                            text = resource.targetGoalCategory,
                            style = MaterialTheme.typography.labelSmall,
                            color = ArloPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (resource.isHiddenGem) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ArloWarmGold.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "HIDDEN GEM ✦",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArloWarmGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ArloDarkSurface
                ) {
                    Text(
                        text = resource.recommendedPhase,
                        style = MaterialTheme.typography.labelSmall,
                        color = ArloTextSecondary,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title & Source
            Text(
                text = resource.title,
                style = MaterialTheme.typography.titleMedium,
                color = ArloTextPrimary,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Source: ${resource.sourceName}",
                style = MaterialTheme.typography.labelSmall,
                color = ArloTextMuted,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Summary
            Text(
                text = resource.summary,
                style = MaterialTheme.typography.bodySmall,
                color = ArloTextPrimary,
                lineHeight = 18.sp,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Why Relevant to Your Phase Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ArloDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ArloPrimary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("🐾", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "WHY ARLO PICKED THIS FOR YOUR CURRENT STEP",
                            style = MaterialTheme.typography.labelSmall,
                            color = ArloPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = resource.whyRelevantToYourPhase,
                            style = MaterialTheme.typography.bodySmall,
                            color = ArloTextSecondary,
                            lineHeight = 16.sp,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Tags
            if (resource.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(resource.tags) { tag ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ArloDarkSurfaceVariant
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArloTextMuted,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = ArloBorder.copy(alpha = 0.5f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(6.dp))

            // Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onOpenUrl) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp), tint = ArloPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open", color = ArloPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    TextButton(onClick = onSaveToLinksi) {
                        Icon(
                            if (resource.isSavedToVault) Icons.Default.Check else Icons.Default.BookmarkAdd,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (resource.isSavedToVault) ArloSuccess else ArloWarmGold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (resource.isSavedToVault) "Saved in Linksi" else "Save to Linksi",
                            color = if (resource.isSavedToVault) ArloSuccess else ArloWarmGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(onClick = onCopyUrl, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy link", tint = ArloTextSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
