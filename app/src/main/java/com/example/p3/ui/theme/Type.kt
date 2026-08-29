package com.example.p3.ui.theme




import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.p3.R
val TinosFont = FontFamily(
    Font(
        R.font.tinosregular,
        FontWeight.Normal,
        FontStyle.Normal
    ),
    Font(
        R.font.tinosbold,
        FontWeight.Bold,
        FontStyle.Normal
    ),
    Font(
        R.font.tinositalic,
        FontWeight.Normal,
        FontStyle.Italic
    )
)

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = TinosFont,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    ),

    bodyLarge = TextStyle(
        fontFamily = TinosFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),

    labelLarge = TextStyle(
        fontFamily = TinosFont,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
    )
)