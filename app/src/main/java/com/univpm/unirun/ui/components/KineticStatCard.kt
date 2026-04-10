package com.univpm.unirun.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun KineticStatCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    StatCard(
        title = title,
        value = value,
        accentColor = accentColor,
        modifier = modifier,
        subtitle = subtitle
    )
}
