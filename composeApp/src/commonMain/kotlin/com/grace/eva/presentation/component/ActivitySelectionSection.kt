package com.grace.eva.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.grace.eva.presentation.screen.ActivitySelectionButton
import com.grace.eva.presentation.statistics.ActivityStat
import com.grace.eva.presentation.viewmodel.TrackerViewModel

@Composable
fun ActivitySelectionSection(
    selectedActivityName: String?,
    onActivitySelected: (String?) -> Unit,
    activityStats: List<ActivityStat>,
    buttonRows: List<List<ActivityStat>>,
    viewModel: TrackerViewModel
) {

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Статистика этапов",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        OutlinedButton(
            onClick = { onActivitySelected(null) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = if (selectedActivityName == null) {
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            } else {
                null
            }
        ) {
            Text("Все этапы")
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(158.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                buttonRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        row.forEach { stat ->
                            ActivitySelectionButton(
                                name = stat.name,
                                isSelected = selectedActivityName == stat.name,
                                onClick = { onActivitySelected(stat.name) },
                                modifier = Modifier.weight(1f),
                                viewModel = viewModel
                            )
                        }

                        if (row.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}