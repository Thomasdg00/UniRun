package com.univpm.unirun.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.univpm.unirun.ui.theme.KineticPrimary

@Composable
fun KineticNavActiveItem(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(24.dp)
            .height(3.dp)
            .background(KineticPrimary, RoundedCornerShape(1.5.dp))
    )
}
