package io.github.ilievi187.recurra.data

/**
 * Platform-specific local storage, export/import directory, and browser launcher.
 */
expect object VaultStorage {
    fun readVaultJson(): String?
    fun writeVaultJson(json: String)
    fun vaultDirectoryPath(): String
    fun writeExportFile(fileName: String, content: String): String
    fun readExportFile(filePath: String): String
    fun listExportFiles(): List<String>
    fun openExternalUrl(url: String): Boolean
}
