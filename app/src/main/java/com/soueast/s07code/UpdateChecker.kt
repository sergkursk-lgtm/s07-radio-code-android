package com.soueast.s07code

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.URL

data class GithubRelease(
    val versionName: String,
    val downloadUrl: String,
    val releaseNotes: String
)

object UpdateChecker {

    private const val GITHUB_REPO = "sergkursk-lgtm/s07-radio-code-android"
    private const val API_URL = "https://api.github.com/repos/$GITHUB_REPO/releases/latest"

    suspend fun checkForUpdate(currentVersionName: String): GithubRelease? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(API_URL)
                val connection = url.openConnection()
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)

                val tagName = json.getString("tag_name").removePrefix("v")

                if (isVersionNewer(tagName, currentVersionName)) {
                    val assets = json.getJSONArray("assets")
                    var downloadUrl = ""

                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.getString("name")
                        if (name.endsWith(".apk")) {
                            downloadUrl = asset.getString("browser_download_url")
                            break
                        }
                    }

                    val body = json.optString("body", "Доступна новая версия приложения")

                    if (downloadUrl.isNotEmpty()) {
                        GithubRelease(
                            versionName = tagName,
                            downloadUrl = downloadUrl,
                            releaseNotes = body
                        )
                    } else {
                        null
                    }
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun isVersionNewer(latest: String, current: String): Boolean {
        val latestParts = latest.split(".").map { it.toIntOrNull() ?: 0 }
        val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }

        val maxSize = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until maxSize) {
            val latestPart = latestParts.getOrElse(i) { 0 }
            val currentPart = currentParts.getOrElse(i) { 0 }
            if (latestPart > currentPart) return true
            if (latestPart < currentPart) return false
        }
        return false
    }

    suspend fun downloadAndInstall(context: Context, release: GithubRelease) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL(release.downloadUrl)
                val connection = url.openConnection()
                connection.connectTimeout = 10000
                connection.readTimeout = 30000

                val fileName = "soueast_adb_code_${release.versionName}.apk"
                val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)

                connection.inputStream.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                withContext(Dispatchers.Main) {
                    installApk(context, file)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun installApk(context: Context, file: File) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                ),
                "application/vnd.android.package-archive"
            )
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    }
}
