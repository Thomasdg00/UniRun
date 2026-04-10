package com.univpm.unirun.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun KineticInputField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    UniRunInputField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier
    )
}
