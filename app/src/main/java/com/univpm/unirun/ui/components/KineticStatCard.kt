package com.univpm.unirun.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.univpm.unirun.ui.theme.KineticPrimary
import com.univpm.unirun.ui.theme.KineticSurface
import com.univpm.unirun.ui.theme.KineticTypography
import com.univpm.unirun.ui.theme.KineticOnBackground

@Composable
fun KineticStatCard(
    title: String,
    value: String,
    accentColor: Color = KineticPrimary,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Row(
        modifier = modifier
            .background(KineticSurface, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .background(accentColor, RoundedCornerShape(2.dp))
                .padding(vertical = 12.dp)
        )
        
        Column(
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Text(
                text = title,
                style = KineticTypography.labelLarge,
                color = KineticOnBackground
            )
            Text(
                text = value,
                style = KineticTypography.displayLarge,
                color = accentColor
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = KineticTypography.bodyLarge,
                    color = KineticOnBackground.copy(alpha = 0.7f)
                )
            }
        }
    }
}
