package io.github.ilievi187.recurra.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.io.File

actual object VaultStorage {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun dataDir(): File {
        val dir = File(appContext.filesDir, "recurra")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun exportsDir(): File {
        val base = appContext.getExternalFilesDir("exports") ?: File(dataDir(), "exports")
        if (!base.exists()) base.mkdirs()
        return base
    }

    private fun vaultFile(): File = File(dataDir(), "vault.json")

    actual fun readVaultJson(): String? {
        val file = vaultFile()
        if (!file.exists()) return null
        return file.readText(Charsets.UTF_8)
    }

    actual fun writeVaultJson(json: String) {
        val target = vaultFile()
        val tmp = File(target.parentFile, "vault.json.tmp")
        tmp.writeText(json, Charsets.UTF_8)
        if (!tmp.renameTo(target)) {
            target.writeText(json, Charsets.UTF_8)
            tmp.delete()
        }
    }

    actual fun vaultDirectoryPath(): String = dataDir().absolutePath

    actual fun writeExportFile(fileName: String, content: String): String {
        val safeName = fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val file = File(exportsDir(), safeName)
        file.writeText(content, Charsets.UTF_8)
        return file.absolutePath
    }

    actual fun readExportFile(filePath: String): String {
        val file = File(filePath)
        require(file.exists() && file.isFile) { "File not found: $filePath" }
        return file.readText(Charsets.UTF_8)
    }

    actual fun listExportFiles(): List<String> {
        return exportsDir().listFiles()
            ?.filter { it.isFile }
            ?.sortedByDescending { it.lastModified() }
            ?.map { it.absolutePath }
            ?: emptyList()
    }

    actual fun openExternalUrl(url: String): Boolean {
        return runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            appContext.startActivity(intent)
            true
        }.getOrDefault(false)
    }
}
