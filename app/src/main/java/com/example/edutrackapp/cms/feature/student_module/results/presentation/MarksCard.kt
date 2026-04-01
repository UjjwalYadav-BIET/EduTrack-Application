package com.example.edutrackapp.cms.feature.student_module.results.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edutrackapp.Domain.Model.StudentResultUi

@Composable
fun MarksCard(result: StudentResultUi) {

    val marks = result.marksObtained.toIntOrNull()
    val isAbsent = result.marksObtained == "AB"

    val color = when {
        isAbsent -> Color.Gray
        marks == null -> Color.Gray
        marks < 35 -> Color.Red
        else -> Color(0xFF4CAF50)
    }

    val grade = marks?.let { getGrade(it) } ?: "--"

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(result.subject, fontWeight = FontWeight.Bold)
                    Text(result.testName, fontSize = 12.sp, color = Color.Gray)
                }

                Text(
                    text = if (isAbsent) "AB" else result.marksObtained,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = color
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!isAbsent && marks != null) {
                LinearProgressIndicator(
                    progress = (marks / 100f),
                    modifier = Modifier.fillMaxWidth(),
                    color = color
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Grade: $grade", fontSize = 12.sp)

                Text(
                    text = if (isAbsent) "Absent"
                    else if (isPass(marks ?: 0)) "Pass"
                    else "Fail",
                    fontSize = 12.sp,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
