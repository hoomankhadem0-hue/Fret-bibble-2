package com.whoman.fretbible.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.whoman.fretbible.ui.theme.*

@Composable
fun ScreenHeader(
    kicker: String,
    title: String,
    subtitle: String? = null,
    action: (@Composable (() -> Unit))? = null
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(kicker, color = Lime, style = MaterialTheme.typography.labelMedium)
            Text(title, color = TextPrimary, style = MaterialTheme.typography.headlineMedium)
            subtitle?.let { Text(it, color = TextSecondary, style = MaterialTheme.typography.bodyMedium) }
        }
        action?.invoke()
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, color = TextMuted, style = MaterialTheme.typography.labelMedium, modifier = modifier)
}

@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    elevated: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = if (elevated) ElevatedSurface else Surface,
        border = BorderStroke(1.dp, Border.copy(alpha = .65f))
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

@Composable
fun MetricPill(label: String, value: String, accent: Boolean = true) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ElevatedSurface.copy(alpha = .9f),
        border = BorderStroke(1.dp, Border.copy(alpha = .7f))
    ) {
        Column(Modifier.padding(horizontal = 11.dp, vertical = 7.dp), horizontalAlignment = Alignment.End) {
            Text(label, color = TextMuted, style = MaterialTheme.typography.labelSmall)
            Text(value, color = if (accent) Lime else TextPrimary, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
fun PrimaryAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Lime,
            contentColor = Background,
            disabledContainerColor = Border,
            disabledContentColor = TextSecondary
        )
    ) { Text(text, style = MaterialTheme.typography.labelLarge) }
}

@Composable
fun SecondaryAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Border),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
    ) { Text(text, style = MaterialTheme.typography.labelLarge) }
}

@Composable
fun SettingRow(title: String, value: String, onClick: (() -> Unit)? = null) {
    val row: @Composable RowScope.() -> Unit = {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
            Text(value, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
        Text(
            if (onClick != null) "EDIT" else "FIXED",
            color = if (onClick != null) Lime else TextMuted,
            style = MaterialTheme.typography.labelSmall
        )
    }
    if (onClick != null) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(14.dp),
            color = ElevatedSurface.copy(alpha = .55f),
            border = BorderStroke(1.dp, Border.copy(alpha = .55f)),
            modifier = Modifier.fillMaxWidth()
        ) { Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), content = row) }
    } else {
        Row(Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 10.dp), content = row)
    }
}

@Composable
fun MiniStat(label: String, value: String, modifier: Modifier = Modifier, accent: Boolean = false) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Surface,
        border = BorderStroke(1.dp, Border.copy(alpha = .6f))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, color = TextMuted, style = MaterialTheme.typography.labelSmall)
            Text(value, color = if (accent) Lime else TextPrimary, style = MaterialTheme.typography.titleLarge)
        }
    }
}
