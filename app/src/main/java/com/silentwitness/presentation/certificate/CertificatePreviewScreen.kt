package com.silentwitness.presentation.certificate

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.silentwitness.domain.models.LogEntry
import com.silentwitness.presentation.theme.SwCard
import com.silentwitness.presentation.theme.SwMuted
import com.silentwitness.presentation.theme.SwSurface
import com.silentwitness.presentation.theme.SwText
import com.silentwitness.utils.categoryLabel
import com.silentwitness.utils.formatDate
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CertificatePreviewScreen(
    onBack: () -> Unit,
    viewModel: CertificatePreviewViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Certificate preview") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SwSurface,
                    titleContentColor = SwText
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = SwCard)) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "CERTIFICATE OF RECORD",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = SwMuted.copy(alpha = 0.4f))
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "This document records the incidents logged with Silent Witness. It is provided as a factual record of the entries made by the user.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Certificate ID: ${uiState.certificateId}",
                        style = MaterialTheme.typography.labelMedium,
                        color = SwMuted
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Period: ${uiState.entries.firstOrNull()?.date?.formatDate() ?: "—"} to ${uiState.entries.lastOrNull()?.date?.formatDate() ?: "—"}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "Entries recorded: ${uiState.entries.size}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "Generated: ${uiState.generatedAt.formatDate()}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(16.dp))
                    uiState.entries.forEachIndexed { index, entry ->
                        Text(
                            "${index + 1}. ${entry.date.formatDate()} — ${categoryLabel(entry.category)}: ${entry.description}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "This certificate is not legal advice. For use in legal proceedings, consult a professional.",
                style = MaterialTheme.typography.bodyMedium,
                color = SwMuted
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { generateCertificatePdf(context, uiState.entries) },
                enabled = uiState.entries.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save & share as PDF") }
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun generateCertificatePdf(context: Context, entries: List<LogEntry>) {
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = document.startPage(pageInfo)
    val canvas = page.canvas

    val titlePaint = Paint().apply {
        color = Color.BLACK
        textSize = 22f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }
    val bodyPaint = Paint().apply {
        color = Color.BLACK
        textSize = 12f
        textAlign = Paint.Align.LEFT
    }
    val smallPaint = Paint().apply {
        color = Color.DKGRAY
        textSize = 10f
        textAlign = Paint.Align.CENTER
    }

    var y = 90f
    canvas.drawText("CERTIFICATE OF RECORD", 297f, y, titlePaint)
    y += 40f
    canvas.drawText(
        "Factual record of incidents logged with Silent Witness",
        297f,
        y,
        smallPaint
    )
    y += 40f

    val first = entries.firstOrNull()?.date
    val last = entries.lastOrNull()?.date
    bodyPaint.textSize = 12f
    bodyPaint.textAlign = Paint.Align.LEFT
    canvas.drawText(
        "Period: ${first?.formatDate() ?: "—"} to ${last?.formatDate() ?: "—"}",
        50f,
        y,
        bodyPaint
    )
    y += 22f
    canvas.drawText("Entries recorded: ${entries.size}", 50f, y, bodyPaint)
    y += 22f
    canvas.drawText("Generated: ${LocalDateTime.now().formatDate()}", 50f, y, bodyPaint)
    y += 30f

    var index = 1
    for (entry in entries) {
        if (y > 800f) break
        canvas.drawText(
            "$index. ${entry.date.formatDate()} - ${categoryLabel(entry.category)}",
            50f,
            y,
            bodyPaint
        )
        y += 16f
        y = drawWrappedText(canvas, entry.description, 50f, y, 495f, 14f, bodyPaint) + 8f
        index++
    }

    document.finishPage(page)

    val file = File(context.getExternalFilesDir(null), "silent_witness_certificate.pdf")
    try {
        FileOutputStream(file).use { document.writeTo(it) }
    } finally {
        document.close()
    }
    sharePdf(context, file)
}

private fun drawWrappedText(
    canvas: Canvas,
    text: String,
    left: Float,
    top: Float,
    maxWidth: Float,
    lineHeight: Float,
    paint: Paint
): Float {
    var y = top
    val words = text.split(" ")
    val line = StringBuilder()
    words.forEach { word ->
        val test = if (line.isEmpty()) word else "$line $word"
        if (paint.measureText(test) > maxWidth && line.isNotEmpty()) {
            canvas.drawText(line.toString(), left, y, paint)
            y += lineHeight
            line.setLength(0)
            line.append(word)
        } else {
            if (line.isNotEmpty()) line.append(' ')
            line.append(word)
        }
    }
    if (line.isNotEmpty()) canvas.drawText(line.toString(), left, y, paint)
    return y + lineHeight
}

private fun sharePdf(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_SUBJECT, "Silent Witness certificate")
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share certificate"))
}
