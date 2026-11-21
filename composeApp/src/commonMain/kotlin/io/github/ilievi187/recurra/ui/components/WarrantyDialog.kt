package io.github.ilievi187.recurra.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ilievi187.recurra.domain.FinanceCalculator
import io.github.ilievi187.recurra.domain.WarrantyItem
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus

@Composable
fun WarrantyDialog(
    initial: WarrantyItem?,
    defaultCurrency: String,
    onDismiss: () -> Unit,
    onSave: (WarrantyItem) -> Unit,
) {
    val today = FinanceCalculator.today()
    var productName by remember { mutableStateOf(initial?.productName.orEmpty()) }
    var brand by remember { mutableStateOf(initial?.brand.orEmpty()) }
    var purchaseDate by remember { mutableStateOf(initial?.purchaseDate ?: today.toString()) }
    var expiryDate by remember {
        mutableStateOf(initial?.expiryDate ?: today.plus(DatePeriod(years = 2)).toString())
    }
    var priceText by remember { mutableStateOf(initial?.purchasePrice?.takeIf { it > 0 }?.toString().orEmpty()) }
    var currency by remember { mutableStateOf(initial?.currency ?: defaultCurrency) }
    var vendor by remember { mutableStateOf(initial?.vendor.orEmpty()) }
    var serialNumber by remember { mutableStateOf(initial?.serialNumber.orEmpty()) }
    var receiptRef by remember { mutableStateOf(initial?.receiptReference.orEmpty()) }
    var notes by remember { mutableStateOf(initial?.notes.orEmpty()) }
    val attachmentPath = initial?.attachmentPath.orEmpty()
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                when {
                    initial == null -> "Add Warranty / Asset"
                    initial.id.isBlank() -> "Review Scanned Warranty"
                    else -> "Edit Warranty"
                },
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it; errorText = null },
                    label = { Text("Product Name *") },
                    placeholder = { Text("e.g., ThinkPad T14 Gen 5") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Brand") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = vendor,
                        onValueChange = { vendor = it },
                        label = { Text("Store / Vendor") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = purchaseDate,
                        onValueChange = { purchaseDate = it; errorText = null },
                        label = { Text("Purchased (YYYY-MM-DD) *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { expiryDate = it; errorText = null },
                        label = { Text("Expires (YYYY-MM-DD) *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Purchase Price") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = currency,
                        onValueChange = { currency = it.uppercase().take(4) },
                        label = { Text("Currency") },
                        singleLine = true,
                        modifier = Modifier.weight(0.6f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = serialNumber,
                        onValueChange = { serialNumber = it },
                        label = { Text("Serial Number") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = receiptRef,
                        onValueChange = { receiptRef = it },
                        label = { Text("Invoice / Receipt #") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (attachmentPath.isNotBlank()) {
                    Text(
                        text = "Attached receipt: ${attachmentPath.substringAfterLast('/').substringAfterLast('\\')}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                if (errorText != null) {
                    Text(errorText!!, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pDate = FinanceCalculator.parseDateOrNull(purchaseDate)
                    val eDate = FinanceCalculator.parseDateOrNull(expiryDate)
                    when {
                        productName.isBlank() -> errorText = "Enter a product name."
                        pDate == null -> errorText = "Purchase date must be YYYY-MM-DD."
                        eDate == null -> errorText = "Expiry date must be YYYY-MM-DD."
                        else -> {
                            onSave(
                                WarrantyItem(
                                    id = initial?.id.orEmpty(),
                                    productName = productName.trim(),
                                    brand = brand.trim(),
                                    purchaseDate = pDate.toString(),
                                    expiryDate = eDate.toString(),
                                    purchasePrice = priceText.trim().replace(",", ".").toDoubleOrNull() ?: 0.0,
                                    currency = currency.ifBlank { defaultCurrency }.uppercase(),
                                    vendor = vendor.trim(),
                                    serialNumber = serialNumber.trim(),
                                    receiptReference = receiptRef.trim(),
                                    notes = notes.trim(),
                                    attachmentPath = attachmentPath,
                                ),
                            )
                        }
                    }
                },
            ) {
                Text(if (initial == null || initial.id.isBlank()) "Save" else "Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
