package com.app.pictravelly.core.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ImageStorageManager {

    private const val PHOTOS_FOLDER = "spot_photos"
    private const val EXPORTS_FOLDER = "exports"

    fun getPhotosDirectory(context: Context): File {
        val dir = File(context.filesDir, PHOTOS_FOLDER)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun createCameraImageFile(context: Context): Pair<File, Uri> {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val randomPart = UUID.randomUUID().toString().take(6)
        val file = File(getPhotosDirectory(context), "IMG_${timeStamp}_$randomPart.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Pair(file, uri)
    }

    fun copyUriToInternalStorage(context: Context, sourceUri: Uri): Uri? {
        if (sourceUri.authority == "${context.packageName}.fileprovider") {
            return sourceUri
        }

        return try {
            val mimeType = context.contentResolver.getType(sourceUri)
            val extension = when (mimeType) {
                "image/png" -> ".png"
                "image/webp" -> ".webp"
                else -> ".jpg"
            }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val randomPart = UUID.randomUUID().toString().take(6)
            val targetFile = File(getPhotosDirectory(context), "IMG_${timeStamp}_$randomPart$extension")

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                targetFile
            )
        } catch (e: Exception) {
            null
        }
    }

    fun createPhotosZipBackup(context: Context, imageUris: List<String>): File? {
        return try {
            val exportsDir = File(context.cacheDir, EXPORTS_FOLDER)
            if (!exportsDir.exists()) {
                exportsDir.mkdirs()
            }
            exportsDir.listFiles()?.forEach { it.delete() }

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val zipFile = File(exportsDir, "PicTravelly_Backup_$timeStamp.zip")

            var count = 0
            val processedFileNames = mutableSetOf<String>()

            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zipOut ->
                for (uriString in imageUris.distinct()) {
                    try {
                        val uri = Uri.parse(uriString)
                        val stream = if (uri.scheme == "file" || uri.scheme == null) {
                            val localFile = File(uri.path ?: uriString)
                            if (localFile.exists()) localFile.inputStream() else null
                        } else {
                            context.contentResolver.openInputStream(uri)
                        }

                        if (stream != null) {
                            val originalName = uri.lastPathSegment?.substringAfterLast('/')
                                ?: "photo_${count + 1}.jpg"
                            val entryName = "foto_${count + 1}_$originalName"
                            processedFileNames.add(originalName)

                            stream.use { input ->
                                zipOut.putNextEntry(ZipEntry(entryName))
                                input.copyTo(zipOut)
                                zipOut.closeEntry()
                            }
                            count++
                        }
                    } catch (_: Exception) {}
                }

                val internalDir = getPhotosDirectory(context)
                internalDir.listFiles()?.forEach { file ->
                    if (file.isFile && !processedFileNames.contains(file.name)) {
                        try {
                            file.inputStream().use { input ->
                                zipOut.putNextEntry(ZipEntry("foto_${count + 1}_${file.name}"))
                                input.copyTo(zipOut)
                                zipOut.closeEntry()
                            }
                            count++
                        } catch (_: Exception) {}
                    }
                }
            }

            if (count == 0) {
                zipFile.delete()
                null
            } else {
                zipFile
            }
        } catch (e: Exception) {
            null
        }
    }

    fun shareZipFile(context: Context, zipFile: File) {
        val zipUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            zipFile
        )
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, zipUri)
            putExtra(Intent.EXTRA_SUBJECT, "PicTravelly - Backup de Fotos")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Exportar fotos do diário")
        if (context !is Activity) {
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
