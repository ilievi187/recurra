package io.github.ilievi187.recurra

import io.github.ilievi187.recurra.domain.BillingCycle
import io.github.ilievi187.recurra.domain.DetectedDocumentType
import io.github.ilievi187.recurra.domain.DocumentParser
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class DocumentParserTest {

    @Test
    fun parsesBulgarianWarrantyInvoiceAccurately() {
        val sampleBg = """
            ТЕХНОПОЛИС БЪЛГАРИЯ ЕАД
            ФАКТУРА № 0048291042
            Дата: 14.11.2026
            Артикул: Sony WH-1000XM5 Wireless Headphones
            Сериен №: SN-XM5-994821
            Гаранция: 24 месеца
            Обща сума: 649,90 лв.
        """.trimIndent()

        val parsed = DocumentParser.parse(sampleBg, defaultCurrency = "EUR", referenceDate = LocalDate(2026, 11, 21))

        assertEquals(DetectedDocumentType.WARRANTY, parsed.detectedType)
        assertEquals("ТЕХНОПОЛИС БЪЛГАРИЯ ЕАД", parsed.vendor)
        assertEquals("Sony WH-1000XM5 Wireless Headphones", parsed.title)
        assertEquals("0048291042", parsed.invoiceNumber)
        assertEquals("SN-XM5-994821", parsed.serialNumber)
        assertEquals("2026-11-14", parsed.purchaseDate)
        assertEquals(24, parsed.warrantyMonths)
        assertEquals("2028-11-14", parsed.expiryDate)
        assertEquals("BGN", parsed.currency)
        assertEquals(649.90, parsed.amount!!, 0.001)
    }

    @Test
    fun parsesEnglishSubscriptionInvoiceAccurately() {
        val sampleEn = """
            Proton AG
            Invoice # PR-2026-8841
            Date: 2026-11-05
            Plan: Proton Unlimited Monthly Plan
            Auto-renewing subscription
            Total Due: €9.99
        """.trimIndent()

        val parsed = DocumentParser.parse(sampleEn, defaultCurrency = "USD", referenceDate = LocalDate(2026, 11, 21))

        assertEquals(DetectedDocumentType.SUBSCRIPTION, parsed.detectedType)
        assertEquals(BillingCycle.MONTHLY, parsed.billingCycle)
        assertEquals("Proton Unlimited Monthly Plan", parsed.title)
        assertEquals("EUR", parsed.currency)
        assertEquals(9.99, parsed.amount!!, 0.001)
        assertEquals("2026-11-05", parsed.purchaseDate)
        assertEquals("PR-2026-8841", parsed.invoiceNumber)
    }
}
