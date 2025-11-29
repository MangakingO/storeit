package com.example.storeit.ui.components

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun BottomBarButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val alpha = if (enabled) 1f else 0.38f

    Column(
        modifier = modifier
            .clickable(
                enabled = enabled,
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current.takeIf { enabled }
            )
            .padding(vertical = 6.dp)
            .fillMaxHeight()
            .alpha(alpha),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
    }
}