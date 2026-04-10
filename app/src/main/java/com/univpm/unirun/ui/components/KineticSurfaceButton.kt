package com.univpm.unirun.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun KineticSurfaceButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    UniRunButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        variant = UniRunButtonVariant.Surface,
        leadingContent = leadingIcon
    )
}
