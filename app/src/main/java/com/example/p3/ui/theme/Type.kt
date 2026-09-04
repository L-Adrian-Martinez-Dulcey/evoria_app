package com.example.p3.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.p3.R

val Tinos = FontFamily(
    Font(R.font.tinosregular, FontWeight.Normal),
    Font(R.font.tinosbold, FontWeight.Bold),
    Font(R.font.tinositalic, FontWeight.Normal, FontStyle.Italic)
)

val Typography = Typography(
    displayLarge = TextStyle(fontFamily = Tinos),
    displayMedium = TextStyle(fontFamily = Tinos),
    displaySmall = TextStyle(fontFamily = Tinos),
    headlineLarge = TextStyle(
        fontFamily = Tinos,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    ),
    headlineMedium = TextStyle(fontFamily = Tinos),
    headlineSmall = TextStyle(fontFamily = Tinos),
    titleLarge = TextStyle(fontFamily = Tinos),
    titleMedium = TextStyle(fontFamily = Tinos),
    titleSmall = TextStyle(fontFamily = Tinos),
    bodyLarge = TextStyle(
        fontFamily = Tinos,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(fontFamily = Tinos),
    bodySmall = TextStyle(fontFamily = Tinos),
    labelLarge = TextStyle(
        fontFamily = Tinos,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
    ),
    labelMedium = TextStyle(fontFamily = Tinos),
    labelSmall = TextStyle(fontFamily = Tinos)
)
