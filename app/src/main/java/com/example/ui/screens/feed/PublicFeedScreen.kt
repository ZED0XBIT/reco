package com.example.ui.screens.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioIntegrityUtils
import com.example.data.model.IncidentEntity
import com.example.data.model.RecordingEntity
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.theme.Neutral500
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PublicFeedScreen(
    viewModel: MainViewModel,
    lang: AppLanguage,
    onOpenRecordingDetail: (RecordingEntity) -> Unit,
    onOpenIncidentDetail: (IncidentEntity) -> Unit = {}
) {
    val recordings by viewModel.filteredPublicRecordings.collectAsState()
    val allIncidents by viewModel.allPublicIncidents.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCat by viewModel.selectedCategoryFilter.collectAsState()

    var feedSubTab by remember { mutableIntStateOf(0) } // 0 = Incidents, 1 = Audios

    val isPlaying by viewModel.audioPlayerManager.isPlaying.collectAsState()
    val currentPlayingId by viewModel.audioPlayerManager.currentRecordingId.collectAsState()

    val filteredIncidents = remember(allIncidents, searchQuery, selectedCat) {
        allIncidents.filter { inc ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                inc.incidentId.contains(searchQuery, ignoreCase = true) ||
                inc.title.contains(searchQuery, ignoreCase = true) ||
                inc.governorate.contains(searchQuery, ignoreCase = true) ||
                inc.description.contains(searchQuery, ignoreCase = true)
            }
            val matchesCategory = if (selectedCat == null) true else {
                inc.category.contains(selectedCat ?: "", ignoreCase = true)
            }
            matchesSearch && matchesCategory
        }
    }

    val categories = listOf(
        null to when (lang) {
            AppLanguage.ARABIC -> "الكل"
            AppLanguage.FRENCH -> "Tous"
            AppLanguage.ENGLISH -> "All"
        },
        "Police interaction" to AppStrings.categoryPolice(lang),
        "Government service" to AppStrings.categoryGovService(lang),
        "Public administration" to AppStrings.categoryAdministration(lang),
        "Public transport" to AppStrings.categoryTransport(lang),
        "Public-space incident" to AppStrings.categoryPublicSpace(lang),
        "Other" to AppStrings.categoryOther(lang)
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Section Heading
            Text(
                text = AppStrings.navPublicFeed(lang),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = when (lang) {
                    AppLanguage.ARABIC -> "أدلة صوتية ووقائع معلنة للعموم"
                    AppLanguage.FRENCH -> "Enregistrements citoyens certifiés"
                    AppLanguage.ENGLISH -> "Public certified evidence recordings"
                },
                style = MaterialTheme.typography.bodySmall,
                color = Neutral500
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Minimalist Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = {
                    Text(
                        when (lang) {
                            AppLanguage.ARABIC -> "بحث بالعنوان، المعرف، أو الحساب..."
                            AppLanguage.FRENCH -> "Rechercher par titre, ID..."
                            AppLanguage.ENGLISH -> "Search title, evidence ID..."
                        },
                        color = Neutral500,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = Neutral500,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(
                                imageVector = Icons.Outlined.Clear,
                                contentDescription = "Clear",
                                tint = Neutral500,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("feed_search_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // SubTab Switcher: [الوقائع الموثقة (N)] [التسجيلات الصوتية (M)]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (feedSubTab == 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { feedSubTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (lang) {
                            AppLanguage.ARABIC -> "الوقائع الموثقة (${filteredIncidents.size})"
                            AppLanguage.FRENCH -> "Incidents (${filteredIncidents.size})"
                            AppLanguage.ENGLISH -> "Incidents (${filteredIncidents.size})"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (feedSubTab == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (feedSubTab == 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { feedSubTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (lang) {
                            AppLanguage.ARABIC -> "التسجيلات الصوتية (${recordings.size})"
                            AppLanguage.FRENCH -> "Audios (${recordings.size})"
                            AppLanguage.ENGLISH -> "Audios (${recordings.size})"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (feedSubTab == 1) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Monochrome Category Filter Pills
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { (id, label) ->
                    val isSelected = (selectedCat == id)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                0.5.dp,
                                if (isSelected) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(18.dp)
                            )
                            .clickable { viewModel.selectedCategoryFilter.value = id }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                            .testTag("feed_filter_${id ?: "all"}")
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.surface
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (feedSubTab == 0) {
                // Incidents List
                if (filteredIncidents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.Shield,
                                contentDescription = null,
                                tint = Neutral500,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = when (lang) {
                                    AppLanguage.ARABIC -> "لا توجد وقائع معلنة مطابقة"
                                    AppLanguage.FRENCH -> "Aucun incident trouvé"
                                    AppLanguage.ENGLISH -> "No documented incidents found"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                color = Neutral500
                            )
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filteredIncidents) { inc ->
                            PublicIncidentCard(
                                incident = inc,
                                lang = lang,
                                onClick = { onOpenIncidentDetail(inc) }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        item {
                            Spacer(modifier = Modifier.height(48.dp))
                        }
                    }
                }
            } else {
                // Recordings List
                if (recordings.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.GraphicEq,
                                contentDescription = null,
                                tint = Neutral500,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = when (lang) {
                                    AppLanguage.ARABIC -> "لا توجد تسجيلات مطابقة"
                                    AppLanguage.FRENCH -> "Aucun enregistrement trouvé"
                                    AppLanguage.ENGLISH -> "No recordings found"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                color = Neutral500
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(recordings) { rec ->
                            val isCurrentlyPlaying = (currentPlayingId == rec.recordingId && isPlaying)

                            EditorialMediaCard(
                                recording = rec,
                                isPlaying = isCurrentlyPlaying,
                                onPlayToggle = {
                                    viewModel.audioPlayerManager.loadAndPlay(rec.recordingId, rec.filePath)
                                },
                                onClick = { onOpenRecordingDetail(rec) }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        item {
                            Spacer(modifier = Modifier.height(48.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PublicIncidentCard(
    incident: IncidentEntity,
    lang: AppLanguage,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.getDefault())
    val dateStr = dateFormat.format(Date(incident.incidentDateTime))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(16.dp)
            .testTag("feed_incident_card_${incident.incidentId}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = incident.incidentId,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = incident.governorate,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = incident.category,
                    fontSize = 11.sp,
                    color = Neutral500
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = incident.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (incident.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = incident.description,
                style = MaterialTheme.typography.bodySmall,
                color = Neutral500,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$dateStr • ${incident.evidenceCount} ${when(lang) {
                    AppLanguage.ARABIC -> "ملفات أدلة"
                    AppLanguage.FRENCH -> "pièces"
                    AppLanguage.ENGLISH -> "evidence files"
                }}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = Neutral500
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = when (lang) {
                        AppLanguage.ARABIC -> "عرض الواقعة"
                        AppLanguage.FRENCH -> "Détails"
                        AppLanguage.ENGLISH -> "View"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun EditorialMediaCard(
    recording: RecordingEntity,
    isPlaying: Boolean,
    onPlayToggle: () -> Unit,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    val dateStr = dateFormat.format(Date(recording.publicationTimestamp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(18.dp)
            .testTag("recording_card_${recording.recordingId}")
    ) {
        // Subtle Monochrome Header: Category & Reference ID
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = recording.category.uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.labelSmall,
                color = Neutral500,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Text(
                text = recording.recordingId,
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = Neutral500
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
            text = recording.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Uploader & Date
        Text(
            text = "@${recording.ownerUsername} · $dateStr",
            style = MaterialTheme.typography.bodySmall,
            color = Neutral500
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Minimalist Media Bar: Play Control + Waveform + Duration
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Monochrome Play/Pause
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface)
                    .clickable { onPlayToggle() }
                    .testTag("feed_play_button_${recording.recordingId}"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Mini waveform preview in gray
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(22) { i ->
                    val heights = listOf(4, 10, 14, 8, 16, 12, 6, 14, 10, 16, 8, 12, 18, 6, 14, 10, 16, 8, 12, 6, 10, 4)
                    val h = heights[i % heights.size].dp
                    Box(
                        modifier = Modifier
                            .width(2.5.dp)
                            .height(h)
                            .clip(RoundedCornerShape(1.dp))
                            .background(MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = AudioIntegrityUtils.formatDuration(recording.durationMs),
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
