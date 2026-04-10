package com.univpm.unirun.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.univpm.unirun.ui.theme.KineticSurface

@Composable
fun KineticBentoCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(KineticSurface, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        content()
    }
}
