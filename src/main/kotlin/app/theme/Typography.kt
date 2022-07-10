package app.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val openSansFontFamily = FontFamily(
    Font("fonts/OpenSans/OpenSans-Regular.ttf", FontWeight.Normal, FontStyle.Normal),
    Font("fonts/OpenSans/OpenSans-Italic.ttf", FontWeight.Normal, FontStyle.Italic),
    Font("fonts/OpenSans/OpenSans-Medium.ttf", FontWeight.Medium, FontStyle.Normal),
    Font("fonts/OpenSans/OpenSans-MediumItalic.ttf", FontWeight.Medium, FontStyle.Italic),
    Font("fonts/OpenSans/OpenSans-SemiBold.ttf", FontWeight.SemiBold, FontStyle.Normal),
    Font("fonts/OpenSans/OpenSans-SemiBoldItalic.ttf", FontWeight.SemiBold, FontStyle.Italic),
    Font("fonts/OpenSans/OpenSans-Bold.ttf", FontWeight.Bold, FontStyle.Normal),
    Font("fonts/OpenSans/OpenSans-BoldItalic.ttf", FontWeight.Bold, FontStyle.Italic),
)

const val LETTER_SPACING = 0.5

@Composable
fun typography() = Typography(
    defaultFontFamily = openSansFontFamily,
    h1 = TextStyle(
        fontSize = 32.sp,
        color = MaterialTheme.colors.primaryTextColor,
        letterSpacing = LETTER_SPACING.sp,
    ),
    h2 = TextStyle(
        fontSize = 24.sp,
        color = MaterialTheme.colors.primaryTextColor,
        letterSpacing = LETTER_SPACING.sp,
    ),
    h3 = TextStyle(
        fontSize = 20.sp,
        color = MaterialTheme.colors.primaryTextColor,
        letterSpacing = LETTER_SPACING.sp,
    ),
    h4 = TextStyle(
        fontSize = 16.sp,
        color = MaterialTheme.colors.primaryTextColor,
        letterSpacing = LETTER_SPACING.sp,
    ),
    body1 = TextStyle(
        fontSize = 14.sp,
        color = MaterialTheme.colors.primaryTextColor,
        letterSpacing = LETTER_SPACING.sp,
    ),
    body2 = TextStyle(
        fontSize = 12.sp,
        color = MaterialTheme.colors.primaryTextColor,
        letterSpacing = LETTER_SPACING.sp,
    )
)