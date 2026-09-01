package com.example.core.common

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object FileExporter {

    fun exportTextFile(
        context: Context,
        fileName: String,
        content: String,
        mimeType: String = "text/plain"
    ): String {
        return try {
            val file: File
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/BioSeqHub")
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(content.toByteArray(Charsets.UTF_8))
                    }
                }
            }

            // Also write to app-accessible external downloads directory for direct file path access & sharing
            val downloadsDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "BioSeqHub"
            ).apply { if (!exists()) mkdirs() }

            file = File(downloadsDir, fileName)
            FileOutputStream(file).use { fos ->
                fos.write(content.toByteArray(Charsets.UTF_8))
            }

            // Scan file so Android Downloads app indexes it immediately
            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath),
                arrayOf(mimeType),
                null
            )

            // Trigger Share/Open Intent
            shareFile(context, file, mimeType)

            val sizeKb = String.format("%.1f", file.length() / 1024.0)
            Toast.makeText(context, "Downloaded $fileName ($sizeKb KB) to Downloads folder", Toast.LENGTH_LONG).show()
            "Successfully exported and downloaded '$fileName' ($sizeKb KB) to Downloads/BioSeqHub"
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to internal storage
            try {
                val fallbackFile = File(context.cacheDir, fileName)
                fallbackFile.writeText(content)
                shareFile(context, fallbackFile, mimeType)
                Toast.makeText(context, "Exported $fileName to app cache", Toast.LENGTH_LONG).show()
                "Exported '$fileName' to cache: ${fallbackFile.absolutePath}"
            } catch (ex: Exception) {
                "Export failed: ${e.localizedMessage}"
            }
        }
    }

    fun exportZipArchive(
        context: Context,
        zipFileName: String,
        fileContents: Map<String, String>
    ): String {
        return try {
            val downloadsDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "BioSeqHub"
            ).apply { if (!exists()) mkdirs() }

            val zipFile = File(downloadsDir, zipFileName)
            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                fileContents.forEach { (entryName, textContent) ->
                    val zipEntry = ZipEntry(entryName)
                    zos.putNextEntry(zipEntry)
                    zos.write(textContent.toByteArray(Charsets.UTF_8))
                    zos.closeEntry()
                }
            }

            MediaScannerConnection.scanFile(
                context,
                arrayOf(zipFile.absolutePath),
                arrayOf("application/zip"),
                null
            )

            shareFile(context, zipFile, "application/zip")

            val sizeKb = String.format("%.1f", zipFile.length() / 1024.0)
            Toast.makeText(context, "Downloaded $zipFileName ($sizeKb KB) to Downloads folder", Toast.LENGTH_LONG).show()
            "Successfully exported research archive '$zipFileName' ($sizeKb KB) to Downloads/BioSeqHub"
        } catch (e: Exception) {
            e.printStackTrace()
            "Export archive failed: ${e.localizedMessage}"
        }
    }

    private fun shareFile(context: Context, file: File, mimeType: String) {
        try {
            val uri: Uri = try {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } catch (e: Exception) {
                Uri.fromFile(file)
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "BioSeq Hub Export: ${file.name}")
                putExtra(Intent.EXTRA_TEXT, "Attached exported bioinformatics file from BioSeq Hub.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share or Open Exported File").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
