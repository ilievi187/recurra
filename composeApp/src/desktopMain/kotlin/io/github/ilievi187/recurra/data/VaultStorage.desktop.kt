package io.github.ilievi187.recurra.data

import java.awt.Desktop
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardCopyOption

actual object VaultStorage {

    private fun resolveDataDir(): File {
        val overrideDir = System.getProperty("recurra.data.dir") ?: System.getenv("RECURRA_DATA_DIR")
        val dir = if (!overrideDir.isNullOrBlank()) {
            File(overrideDir)
        } else {
            val os = System.getProperty("os.name").lowercase()
            when {
                os.contains("win") -> {
                    val appData = System.getenv("APPDATA") ?: System.getProperty("user.home")
                    File(appData, "Recurra")
                }
                os.contains("mac") -> {
                    File(System.getProperty("user.home"), "Library/Application Support/Recurra")
                }
                else -> {
                    val xdg = System.getenv("XDG_DATA_HOME")
                    if (!xdg.isNullOrBlank()) {
                        File(xdg, "recurra")
                    } else {
                        File(System.getProperty("user.home"), ".local/share/recurra")
                    }
                }
            }
        }
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun vaultFile(): File = File(resolveDataDir(), "vault.json")

    private fun exportsDir(): File = File(resolveDataDir(), "exports").apply {
        if (!exists()) mkdirs()
    }

    actual fun readVaultJson(): String? {
        val file = vaultFile()
        if (!file.exists()) return null
        return file.readText(Charsets.UTF_8)
    }

    actual fun writeVaultJson(json: String) {
        val target = vaultFile()
        val tmp = File(target.parentFile, "vault.json.tmp")
        tmp.writeText(json, Charsets.UTF_8)
        Files.move(
            tmp.toPath(),
            target.toPath(),
            StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.ATOMIC_MOVE,
        )
    }

    actual fun vaultDirectoryPath(): String = resolveDataDir().absolutePath

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
            val uri = URI(url)
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(uri)
                true
            } else {
                val os = System.getProperty("os.name").lowercase()
                when {
                    os.contains("linux") -> {
                        ProcessBuilder("xdg-open", url).start()
                        true
                    }
                    os.contains("win") -> {
                        ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url).start()
                        true
                    }
                    else -> false
                }
            }
        }.getOrDefault(false)
    }
}
