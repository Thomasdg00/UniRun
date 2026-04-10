package com.univpm.unirun.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.univpm.unirun.ui.theme.KineticSurface
import com.univpm.unirun.ui.theme.KineticTypography
import com.univpm.unirun.ui.theme.KineticOnBackground
import com.univpm.unirun.ui.theme.KineticPrimary

data class BottomNavItem(
    val label: String,
    val id: String
)

@Composable
fun KineticBottomNav(
    items: List<BottomNavItem>,
    currentId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(KineticSurface)
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEach { item ->
                Column(
                    modifier = Modifier
                        .clickable { onSelect(item.id) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = item.label,
                        style = KineticTypography.labelLarge,
                        color = if (item.id == currentId) KineticPrimary else KineticOnBackground
                    )
                    if (item.id == currentId) {
                        KineticNavActiveItem()
                    }
                }
            }
        }
    }
}
