package com.univpm.unirun.ui.components

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import com.univpm.unirun.ui.theme.KineticPrimaryContainer
import com.univpm.unirun.ui.theme.KineticOnPrimary

@Composable
fun KineticButton(onClick: () -> Unit, modifier: Modifier = Modifier, text: String) {
    Button(onClick = onClick, modifier = modifier, colors = ButtonDefaults.buttonColors(containerColor = KineticPrimaryContainer, contentColor = KineticOnPrimary)) {
        Text(text)
    }
}
