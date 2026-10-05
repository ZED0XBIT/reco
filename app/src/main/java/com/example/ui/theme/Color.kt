package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// PROTECT YOURSELF — ABSOLUTE MONOCHROMATIC PALETTE
// Strictly White, Grays, Charcoal, and Black.
// Exception: Red is reserved EXCLUSIVELY for recording actions and active states.
// ============================================================================

// Pure Extremes
val MonoWhite = Color(0xFFFFFFFF)
val MonoBlack = Color(0xFF000000)

// Light Theme Neutrals (iOS Luxury Minimal)
val Neutral50 = Color(0xFFF9F9FB)    // Pristine app background
val Neutral100 = Color(0xFFF2F2F7)   // Grouped background / subtle cards
val Neutral150 = Color(0xFFEBEBF0)   // Card fills
val Neutral200 = Color(0xFFE5E5EA)   // Hairline borders / dividers
val Neutral300 = Color(0xFFD1D1D6)   // Inactive tracks / subtle strokes
val Neutral400 = Color(0xFFAEB0B6)   // Subtle placeholders / icons
val Neutral500 = Color(0xFF8E8E93)   // Secondary labels
val Neutral600 = Color(0xFF636366)   // Tertiary text / muted accents
val Neutral700 = Color(0xFF48484A)   // Dark gray
val Neutral800 = Color(0xFF2C2C2E)   // Charcoal
val Neutral850 = Color(0xFF1C1C1E)   // Dark card surface
val Neutral900 = Color(0xFF141416)   // Dark secondary surface
val Neutral950 = Color(0xFF0A0A0C)   // Dark OLED background

// The SOLE Exception Accent: Apple-inspired Refined Recording Red
val RecordRed = Color(0xFFE11D48)       // Deep refined scarlet red
val RecordRedPressed = Color(0xFFBE123C)
val RecordRedLight = Color(0xFFFFF1F2)  // Ultra subtle tint for active recording pill in light mode
val RecordRedDark = Color(0xFF4C0519)   // Ultra subtle tint for active recording pill in dark mode
