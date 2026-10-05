package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.GovernorateInfo
import com.example.data.model.TunisianGovernorates
import com.example.i18n.AppLanguage
import com.example.ui.theme.MonoWhite
import com.example.ui.theme.Neutral500
import com.example.ui.theme.RecordRed
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPinPickerSheet(
    initialLat: Double?,
    initialLng: Double?,
    governorate: GovernorateInfo?,
    lang: AppLanguage,
    onConfirmLocation: (lat: Double, lng: Double) -> Unit,
    onClearLocation: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    // Default coordinates: selected governorate center, or Tunis center
    val defaultLat = governorate?.centerLat ?: 36.8065
    val defaultLng = governorate?.centerLng ?: 10.1815

    var currentLat by remember { mutableDoubleStateOf(initialLat ?: defaultLat) }
    var currentLng by remember { mutableDoubleStateOf(initialLng ?: defaultLng) }
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }

    // Optional GPS permission launcher (never forced)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            try {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val lastLoc = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (lastLoc != null) {
                    currentLat = lastLoc.latitude
                    currentLng = lastLoc.longitude
                }
            } catch (e: SecurityException) {
                // Graceful fallback
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = when (lang) {
                            AppLanguage.ARABIC -> "تحديد الموقع الدقيق على الخريطة"
                            AppLanguage.FRENCH -> "Pointer l'emplacement exact"
                            AppLanguage.ENGLISH -> "Pin exact location on map"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when (lang) {
                            AppLanguage.ARABIC -> "اختياري — اسحب الخريطة لتثبيت الإحداثيات"
                            AppLanguage.FRENCH -> "Optionnel — Déplacez la carte pour pointer"
                            AppLanguage.ENGLISH -> "Optional — Drag map to drop pin"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Neutral500
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Map Canvas Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                    .pointerInput(zoomLevel) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            // Pan sensitivity calibrated for degrees of lat/long
                            val sensitivity = 0.0008 / zoomLevel
                            currentLat -= (dragAmount.y * sensitivity)
                            currentLng += (dragAmount.x * sensitivity)

                            // Clamp bounds to roughly Tunisia region
                            currentLat = currentLat.coerceIn(30.0, 38.0)
                            currentLng = currentLng.coerceIn(7.5, 12.5)
                        }
                    }
                    .testTag("interactive_map_canvas"),
                contentAlignment = Alignment.Center
            ) {
                // Procedural Vector Map Canvas displaying coordinates grid & terrain lines
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height

                    // Grid lines (Latitude / Longitude meridian grid)
                    val gridColor = Color(0x22888888)
                    val step = 40f * zoomLevel
                    var x = (w / 2) % step
                    while (x < w) {
                        drawLine(
                            color = gridColor,
                            start = Offset(x, 0f),
                            end = Offset(x, h),
                            strokeWidth = 1f
                        )
                        x += step
                    }

                    var y = (h / 2) % step
                    while (y < h) {
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f
                        )
                        y += step
                    }

                    // Stylized road & topography vectors
                    val path = Path().apply {
                        moveTo(0f, h * 0.7f)
                        quadraticTo(w * 0.4f, h * 0.5f, w, h * 0.3f)
                        moveTo(w * 0.3f, 0f)
                        quadraticTo(w * 0.5f, h * 0.5f, w * 0.7f, h)
                    }
                    drawPath(
                        path = path,
                        color = Color(0x33AAAAAA),
                        style = Stroke(width = 2.5f)
                    )

                    // Secondary routes
                    val path2 = Path().apply {
                        moveTo(w * 0.1f, h * 0.2f)
                        lineTo(w * 0.85f, h * 0.8f)
                    }
                    drawPath(
                        path = path2,
                        color = Color(0x1A888888),
                        style = Stroke(width = 1.5f)
                    )
                }

                // Map Pin (Fixed in Center, pointing to the exact center location)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "Pin Location",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(36.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface)
                    )
                }

                // Map Controls Overlay (Top Right: Zoom in/out, Bottom Left: Center on Governorate, Bottom Right: Optional GPS)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(0.5.dp, MaterialTheme.colorScheme.outline, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { zoomLevel = (zoomLevel * 1.3f).coerceAtMost(3.0f) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = "Zoom In", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(0.5.dp, MaterialTheme.colorScheme.outline, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { zoomLevel = (zoomLevel / 1.3f).coerceAtLeast(0.6f) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Outlined.Remove, contentDescription = "Zoom Out", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Recenter & GPS Action Buttons on bottom
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Recenter on Governorate
                    OutlinedButton(
                        onClick = {
                            currentLat = defaultLat
                            currentLng = defaultLng
                            zoomLevel = 1.0f
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(34.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Outlined.Explore, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = governorate?.localizedName(lang) ?: "Tunisie",
                            fontSize = 11.sp
                        )
                    }

                    // Optional GPS
                    OutlinedButton(
                        onClick = {
                            val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            if (fineGranted || coarseGranted) {
                                try {
                                    val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                                    val lastLoc = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                                        ?: lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                                    if (lastLoc != null) {
                                        currentLat = lastLoc.latitude
                                        currentLng = lastLoc.longitude
                                    }
                                } catch (e: SecurityException) { }
                            } else {
                                permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(34.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Outlined.MyLocation, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (lang) {
                                AppLanguage.ARABIC -> "موقعي الحالي"
                                AppLanguage.FRENCH -> "Ma position"
                                AppLanguage.ENGLISH -> "My location"
                            },
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Display Coordinates Box (Monochrome)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = when (lang) {
                            AppLanguage.ARABIC -> "الإحداثيات الجغرافية المحددة"
                            AppLanguage.FRENCH -> "Coordonnées sélectionnées"
                            AppLanguage.ENGLISH -> "Selected Coordinates"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral500
                    )
                    Text(
                        text = String.format(Locale.US, "%.5f° N, %.5f° E", currentLat, currentLng),
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (initialLat != null && initialLng != null) {
                    IconButton(onClick = onClearLocation) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Clear",
                            tint = Neutral500,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Confirmation Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onClearLocation()
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = when (lang) {
                            AppLanguage.ARABIC -> "تخطي (ولاية فقط)"
                            AppLanguage.FRENCH -> "Gouvernorat seul"
                            AppLanguage.ENGLISH -> "Governorate only"
                        },
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Button(
                    onClick = {
                        onConfirmLocation(currentLat, currentLng)
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("confirm_map_pin_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Icon(imageVector = Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (lang) {
                            AppLanguage.ARABIC -> "تأكيد الموقع"
                            AppLanguage.FRENCH -> "Confirmer"
                            AppLanguage.ENGLISH -> "Confirm Pin"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
