package com.starcode.locusapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NavBarItem(
    icon: ImageVector,
    iconSelected: ImageVector? = null, // Parámetro opcional para el icono en estado seleccionado
    label: String,
    color: Color,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val iconoAMostrar = if (isSelected && iconSelected != null) iconSelected else icon
    
    Column(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = iconoAMostrar,
            contentDescription = label,
            tint = if (isSelected) color else color.copy(alpha = 0.4f),
            modifier = Modifier.size(26.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) color else color.copy(alpha = 0.4f)
        )
    }
}