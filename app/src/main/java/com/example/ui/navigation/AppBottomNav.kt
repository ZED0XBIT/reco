package com.example.ui.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.theme.MonoWhite
import com.example.ui.theme.Neutral500
import com.example.ui.theme.RecordRed
import com.example.ui.theme.RecordRedPressed
import com.example.viewmodel.NavTab

@Composable
fun AppBottomNav(
    currentTab: NavTab,
    lang: AppLanguage,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Subtle Hairline Separator (iOS Navigation Bar Divider)
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline,
            thickness = 0.5.dp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Home
            BottomNavItem(
                icon = Icons.Outlined.Home,
                label = AppStrings.navHome(lang),
                isSelected = (currentTab == NavTab.HOME),
                testTag = "nav_item_home",
                onClick = { onTabSelected(NavTab.HOME) }
            )

            // 2. Public Feed
            BottomNavItem(
                icon = Icons.Outlined.Public,
                label = AppStrings.navPublicFeed(lang),
                isSelected = (currentTab == NavTab.FEED),
                testTag = "nav_item_feed",
                onClick = { onTabSelected(NavTab.FEED) }
            )

            // 3. Primary Center Record Button (The ONLY Red Element)
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val buttonScale by animateFloatAsState(
                targetValue = if (isPressed) 0.92f else 1.0f,
                label = "nav_rec_scale"
            )

            Box(
                modifier = Modifier
                    .scale(buttonScale)
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (isPressed) RecordRedPressed else RecordRed)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onTabSelected(NavTab.RECORD) }
                    .testTag("nav_item_record"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Mic,
                    contentDescription = AppStrings.navRecord(lang),
                    tint = MonoWhite,
                    modifier = Modifier.size(26.dp)
                )
            }

            // 4. My Audios
            BottomNavItem(
                icon = Icons.Outlined.FolderOpen,
                label = AppStrings.navMyRecordings(lang),
                isSelected = (currentTab == NavTab.MY_AUDIOS),
                testTag = "nav_item_my_audios",
                onClick = { onTabSelected(NavTab.MY_AUDIOS) }
            )

            // 5. Account
            BottomNavItem(
                icon = Icons.Outlined.Person,
                label = AppStrings.navAccount(lang),
                isSelected = (currentTab == NavTab.ACCOUNT),
                testTag = "nav_item_account",
                onClick = { onTabSelected(NavTab.ACCOUNT) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) MaterialTheme.colorScheme.onSurface else Neutral500,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Neutral500
        )
    }
}
