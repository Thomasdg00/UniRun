package com.univpm.unirun.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun KineticButton(onClick: () -> Unit, modifier: Modifier = Modifier, text: String) {
    UniRunButton(
        text = text,
        onClick = onClick,
        modifier = modifier
    )
}
