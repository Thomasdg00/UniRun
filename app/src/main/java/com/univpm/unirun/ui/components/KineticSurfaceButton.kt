package com.univpm.unirun.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.univpm.unirun.ui.theme.KineticPrimary
import com.univpm.unirun.ui.theme.KineticSurface
import com.univpm.unirun.ui.theme.KineticTypography

@Composable
fun KineticSurfaceButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = modifier
            .border(1.dp, KineticPrimary, RoundedCornerShape(8.dp))
            .background(KineticSurface, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (leadingIcon != null) {
            Box(modifier = Modifier.padding(end = 8.dp)) {
                leadingIcon()
            }
        }
        Text(
            text = text,
            style = KineticTypography.labelLarge,
            color = KineticPrimary
        )
    }
}
