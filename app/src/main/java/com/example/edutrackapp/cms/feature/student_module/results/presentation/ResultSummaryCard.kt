package com.example.edutrackapp.cms.feature.student_module.results.presentation


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.example.edutrackapp.Domain.Model.StudentResultUi

@Composable
fun ResultSummaryCard(results: List<StudentResultUi>, themeColor: Color) {

    val totalObtained = results.sumOf { it.marksObtained.toIntOrNull() ?: 0 }
    val totalMax = results.size * 100
    val percentage = if (totalMax > 0) (totalObtained.toFloat() / totalMax) * 100 else 0f

    val overallGrade = getGrade(percentage.toInt())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = themeColor),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Text("Overall Performance", color = Color.White.copy(0.8f))

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "$totalObtained / $totalMax",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = percentage / 100f,
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                trackColor = Color.White.copy(0.3f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${"%.1f".format(percentage)}%", color = Color.White)
                Text("Grade: $overallGrade", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}