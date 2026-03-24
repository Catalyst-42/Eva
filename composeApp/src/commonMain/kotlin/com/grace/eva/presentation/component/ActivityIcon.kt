package com.grace.eva.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grace.eva.domain.model.ActivityTemplate
import com.grace.eva.util.parseColor

@Composable
fun ActivityIcon(template: ActivityTemplate, size: Dp = 16.dp) {
    val circleModifier = if (template.isHidden) {
        Modifier.size(size)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = CircleShape
            )
    } else {
        Modifier
            .size(size)
            .clip(MaterialTheme.shapes.small)
            .background(
                parseColor(template.color) ?:
                MaterialTheme.colorScheme.surfaceVariant)
    }

    Box(circleModifier)
}

@Composable
@Preview(showBackground = true)
fun ActivityIconPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ActivityIcon(
            template = ActivityTemplate()
        )

        ActivityIcon(
            template = ActivityTemplate(isHidden = true)
        )

        ActivityIcon(
            template = ActivityTemplate(),
            size = 8.dp
        )
    }

}
