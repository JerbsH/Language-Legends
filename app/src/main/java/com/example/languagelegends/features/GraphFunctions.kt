package com.example.languagelegends.features

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate

// All the functions needed for showing point graph

@Composable
fun ShowGraph(languageInfoList: List<Pair<String, Int>>, onDismissRequest: () -> Unit) {
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.secondary)
                .padding(16.dp)

        ) {
            ValuesGraph(languageInfoList)
        }
    }
}

@Composable
private fun ValuesGraph(languageInfoList: List<Pair<String, Int>>) {
    AndroidView(
        factory = { ctx ->
            BarChart(ctx).apply {
                val uniqueLanguageInfoList =
                    languageInfoList.distinctBy { it.first } // Remove duplicate languages
                val entries = uniqueLanguageInfoList.mapIndexed { index, (language, points) ->
                    BarEntry(
                        index.toFloat(),
                        points.toFloat(),
                    )
                }
                data = BarData(BarDataSet(entries, "Points").apply {
                    colors = ColorTemplate.MATERIAL_COLORS.toList() // Set colors for bars
                })
                xAxis.apply {
                    valueFormatter =
                        IndexAxisValueFormatter(uniqueLanguageInfoList.map { it.first }) // Set unique language names as labels on x-axis
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    labelCount = uniqueLanguageInfoList.size
                    labelRotationAngle = 0f
                }
                axisRight.isEnabled = false
                axisLeft.axisMinimum = 0f
                description.isEnabled = false
                legend.isEnabled = false
                setFitBars(true)
                invalidate()
            }
        }, modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}