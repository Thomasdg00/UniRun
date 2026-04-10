package com.univpm.unirun.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

@Composable
fun KineticTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KineticDarkColors,
        typography = KineticTypography,
        content = {
            Surface {
                content()
            }
        }
    )
}
