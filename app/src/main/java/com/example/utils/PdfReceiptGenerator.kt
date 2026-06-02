package com.example.utils

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.example.data.TransactionRecord
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat

object PdfReceiptGenerator {

    private val decFormat = DecimalFormat("#,##0.00")

    fun generateReceiptPdf(context: Context, transaction: TransactionRecord): File {
        val pdfDocument = PdfDocument()
        
        // standard high res A4-like canvas (1240 x 1754 px)
        val pageWidth = 1200
        val pageHeight = 1600
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Initialize paints
        val textPaint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
            textSize = 22f
        }
        
        val titlePaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val purplePaint = Paint().apply {
            color = 0xFF851C80.toInt() // CBE Plum Purple
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val greyBorderPaint = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }

        val thinLinePaint = Paint().apply {
            color = Color.GRAY
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        // 1. Draw Header Background (Purple Banner)
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 180f, purplePaint)

        // Draw Golden Accent line
        val goldPaint = Paint().apply {
            color = 0xFFC08A50.toInt()
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRect(0f, 180f, pageWidth.toFloat(), 188f, goldPaint)

        // 2. Draw Logos in Header
        // Let's draw a golden circular wheat logo on the left
        val circPaint = Paint().apply {
            color = 0xFFC08A50.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 5f
            isAntiAlias = true
        }
        canvas.drawCircle(100f, 90f, 45f, circPaint)
        // draw CBE inner wheat symbol schematically
        val wheatPaint = Paint().apply {
            color = 0xFFC08A50.toInt()
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawOval(90f, 70f, 110f, 110f, wheatPaint)
        
        val logoTextPaint = Paint().apply {
            color = 0xFFC08A50.toInt()
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("CBE", 82f, 95f, logoTextPaint)

        // Draw Texts in Header
        canvas.drawText("Commercial Bank of Ethiopia", 280f, 65f, titlePaint)
        canvas.drawText("VAT Invoice / Customer Receipt", 330f, 110f, Paint(titlePaint).apply { textSize = 24f })
        canvas.drawText("CBEBirr", 480f, 150f, Paint(titlePaint).apply { textSize = 20f })

        // Draw CBE Birr logo on the top right
        canvas.drawRoundRect(1020f, 40f, 1140f, 140f, 15f, 15f, Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        })
        val darkPurplePaint = Paint().apply {
            color = 0xFF5C075C.toInt()
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("CBE", 1030f, 85f, Paint(darkPurplePaint).apply { textSize = 15f })
        canvas.drawText("Birr", 1035f, 125f, darkPurplePaint)

        // 3. Two Column Details (Company Address vs Customer Information)
        val colY = 240f
        val headingPaint = Paint().apply {
            color = Color.BLACK
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        // Dividers & Column headers
        canvas.drawRect(40f, colY, 500f, colY + 30f, purplePaint)
        canvas.drawText("Company Address & Other Information", 50f, colY + 22f, Paint(titlePaint).apply { textSize = 16f })

        canvas.drawRect(680f, colY, 1140f, colY + 30f, purplePaint)
        canvas.drawText("Customer Information", 690f, colY + 22f, Paint(titlePaint).apply { textSize = 16f })

        // Column 1 - Company Info
        var leftY = colY + 60f
        val detailsSize = 18f
        val detailsPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = detailsSize
            isAntiAlias = true
        }

        val companyData = listOf(
            "Country: Ethiopia",
            "City: Addis Ababa",
            "Address: Ras Desta Damtew St, 01, Kirkos",
            "Postal Code: 255",
            "SWIFT Code: CBETETAA",
            "Email: info@cbe.com.et",
            "Tel: 251-551-50-04",
            "Fax: 251-551-45-22",
            "TIN: 0000006966",
            "VAT Invoice No: IN-CB-${transaction.transactionId}",
            "VAT Registration No: 011140",
            "VAT Registration Date: 01/01/2003"
        )
        for (line in companyData) {
            canvas.drawText(line, 45f, leftY, detailsPaint)
            leftY += 28f
        }

        // Column 2 - Customer Info
        var rightY = colY + 60f
        val customerData = listOf(
            "Customer Name: ${transaction.senderName}",
            "Region: Addis Ababa",
            "City: Addis Ababa",
            "Sub city: Kirkos",
            "Wereda/kebele: 03",
            "VAT Registration No: -",
            "VAT Registration Date: -",
            "TIN (TAX ID): -",
            "Branch: Addis Ababa Branch",
            "TIN (TAX ID): -"
        )
        for (line in customerData) {
            canvas.drawText(line, 685f, rightY, detailsPaint)
            rightY += 28f
        }

        // 4. Transaction Information block
        val infoY = 640f
        canvas.drawRect(40f, infoY, 1140f, infoY + 5f, purplePaint)
        canvas.drawText("Transaction Information", 500f, infoY - 10f, headingPaint)

        var transY = infoY + 40f
        val boldLabelsPaint = Paint().apply {
            color = Color.BLACK
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val transactionRows = listOf(
            "Debit Account" to "251945954856 - ${transaction.senderName}",
            "Credit Account" to "${transaction.receiverAccount}",
            "Receiver Name" to transaction.receiverName,
            "Order ID" to transaction.orderId,
            "Transaction Status" to "Completed",
            "Reference" to transaction.reason
        )

        for ((label, valText) in transactionRows) {
            canvas.drawText(label, 60f, transY, detailsPaint)
            canvas.drawText(valText, 650f, transY, boldLabelsPaint)
            transY += 35f
        }

        // DRAW BEAUTIFUL PURPLE CIRCULAR CBE STAMP IN THE MID-RIGHT REGION
        val stampX = 480f
        val stampY = infoY + 110f
        val stampPaint = Paint().apply {
            color = 0x664A148C.toInt() // High-fidelity translucent purple CBE stamp
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        canvas.drawCircle(stampX, stampY, 100f, stampPaint)
        canvas.drawCircle(stampX, stampY, 93f, stampPaint)
        
        // draw star icons in stamp
        val starPaint = Paint(stampPaint).apply {
            style = Paint.Style.FILL
            textSize = 18f
        }
        canvas.drawText("★", stampX - 90f, stampY, starPaint)
        canvas.drawText("★", stampX + 75f, stampY, starPaint)

        // Draw CBE emblem inside circular stamp
        canvas.drawCircle(stampX, stampY, 45f, stampPaint)
        // draw wheat schematic inside stamp
        canvas.drawOval(stampX - 10f, stampY - 30f, stampX + 10f, stampY + 10f, stampPaint)

        // Draw curved text inside stamp schematically
        val stampTextPaint = Paint().apply {
            color = 0x884A148C.toInt()
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("የኢትዮጵያ ንግድ ባንክ", stampX - 60f, stampY - 60f, stampTextPaint)
        canvas.drawText("COMMERCIAL BANK OF ETHIOPIA", stampX - 90f, stampY + 65f, stampTextPaint)
        canvas.drawText("* 9.1.6 *", stampX - 25f, stampY + 80f, stampTextPaint)

        // 5. Transaction Details Table
        val tableY = 920f
        canvas.drawRect(40f, tableY, 1140f, tableY + 50f, purplePaint)
        canvas.drawText("Receipt Number", 60f, tableY + 33f, Paint(titlePaint).apply { textSize = 20f })
        canvas.drawText("Transaction Date", 460f, tableY + 33f, Paint(titlePaint).apply { textSize = 20f })
        canvas.drawText("Amount", 940f, tableY + 33f, Paint(titlePaint).apply { textSize = 20f })

        // Data Row
        val dataRowY = tableY + 90f
        canvas.drawText(transaction.transactionId, 60f, dataRowY, boldLabelsPaint)
        canvas.drawText("${transaction.date} ${transaction.time}", 460f, dataRowY, boldLabelsPaint)
        canvas.drawText("${decFormat.format(transaction.amount)}", 940f, dataRowY, boldLabelsPaint)
        
        // Horizontal line under table row
        canvas.drawLine(40f, dataRowY + 30f, 1140f, dataRowY + 30f, greyBorderPaint)

        // Standard payment break-down on right side
        var amountBreakdownY = dataRowY + 70f
        val labelBreakdownPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 20f
            isAntiAlias = true
        }
        val valueBreakdownPaint = Paint().apply {
            color = Color.BLACK
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val breakdown = listOf(
            "Paid amount" to decFormat.format(transaction.amount),
            "Service Charge" to decFormat.format(transaction.serviceCharge),
            "VAT" to decFormat.format(transaction.vat),
            "Tip" to decFormat.format(transaction.tip)
        )

        for ((label, valText) in breakdown) {
            canvas.drawText(label, 680f, amountBreakdownY, labelBreakdownPaint)
            canvas.drawText(valText, 940f, amountBreakdownY, valueBreakdownPaint)
            amountBreakdownY += 32f
        }

        // Draw Total Paid Amount
        canvas.drawRect(670f, amountBreakdownY, 1140f, amountBreakdownY + 4f, purplePaint)
        amountBreakdownY += 35f
        canvas.drawText("Total Paid Amount", 680f, amountBreakdownY, Paint(boldLabelsPaint).apply { textSize = 22f })
        canvas.drawText(decFormat.format(transaction.amount + transaction.serviceCharge + transaction.vat + transaction.tip), 940f, amountBreakdownY, Paint(boldLabelsPaint).apply { textSize = 22f })

        // 6. Total Amount in Words & Reason (Bottom Left)
        var bottomInfoY = dataRowY + 90f
        val amountInWord = getAmountInWords(transaction.amount)
        
        canvas.drawText("Total Amount in word", 60f, bottomInfoY, Paint(boldLabelsPaint).apply { isFakeBoldText = true })
        canvas.drawText("$amountInWord ETB and zero Cents", 340f, bottomInfoY, detailsPaint)

        bottomInfoY += 35f
        canvas.drawText("Payment Reason", 60f, bottomInfoY, Paint(boldLabelsPaint).apply { isFakeBoldText = true })
        canvas.drawText(transaction.reason, 340f, bottomInfoY, detailsPaint)

        bottomInfoY += 35f
        canvas.drawText("Payment Channel", 60f, bottomInfoY, Paint(boldLabelsPaint).apply { isFakeBoldText = true })
        canvas.drawText("API", 340f, bottomInfoY, detailsPaint)

        // 7. Footer banner and slogan
        val footerSloganY = 1430f
        canvas.drawRect(200f, footerSloganY, 1000f, footerSloganY + 3f, purplePaint)
        
        val sloganPaint = Paint().apply {
            color = 0xFF5C075C.toInt()
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("The Bank you can always rely on!", 380f, footerSloganY - 15f, sloganPaint)

        val copyrightPaint = Paint().apply {
            color = Color.GRAY
            textSize = 18f
            isAntiAlias = true
        }
        canvas.drawText("© 2026 Commercial Bank of Ethiopia. All rights reserved", 360f, footerSloganY + 35f, copyrightPaint)

        // 8. Custom QR Code drawn in Bottom-Right
        drawMockQrCode(canvas, 920f, 1280f, 200f)

        pdfDocument.finishPage(page)

        // Save PDF file in externally accessible downloads directory
        val fileName = "CBE_Receipt_${transaction.transactionId}.pdf"
        val downloadDir = context.getExternalFilesDir(null) ?: context.cacheDir
        val file = File(downloadDir, fileName)

        val fos = FileOutputStream(file)
        pdfDocument.writeTo(fos)
        fos.flush()
        fos.close()
        pdfDocument.close()

        return file
    }

    private fun drawMockQrCode(canvas: Canvas, x: Float, y: Float, size: Float) {
        val paint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            isAntiAlias = false
        }
        
        val gridCount = 29
        val dotSize = size / gridCount

        // Clean white background for the barcode card
        paint.color = Color.WHITE
        canvas.drawRect(x, y, x + size, y + size, paint)
        
        fun drawFinder(fx: Float, fy: Float) {
            paint.color = Color.BLACK
            canvas.drawRect(fx, fy, fx + dotSize * 7, fy + dotSize * 7, paint)
            paint.color = Color.WHITE
            canvas.drawRect(fx + dotSize, fy + dotSize, fx + dotSize * 6, fy + dotSize * 6, paint)
            paint.color = Color.BLACK
            canvas.drawRect(fx + dotSize * 2, fy + dotSize * 2, fx + dotSize * 5, fy + dotSize * 5, paint)
        }
        
        // 1. Draw three corner finders
        drawFinder(x, y)
        drawFinder(x + (gridCount - 7) * dotSize, y)
        drawFinder(x, y + (gridCount - 7) * dotSize)

        // 2. Draw alignment pattern in lower right area (center at 22, 22)
        val ax = x + 20 * dotSize
        val ay = y + 20 * dotSize
        paint.color = Color.BLACK
        canvas.drawRect(ax, ay, ax + dotSize * 5, ay + dotSize * 5, paint)
        paint.color = Color.WHITE
        canvas.drawRect(ax + dotSize, ay + dotSize, ax + dotSize * 4, ay + dotSize * 4, paint)
        paint.color = Color.BLACK
        canvas.drawRect(ax + dotSize * 2, ay + dotSize * 2, ax + dotSize * 3, ay + dotSize * 3, paint)
        
        // 3. Draw deterministic standard QR bits and timing patterns
        val random = java.util.Random(99)
        for (row in 0 until gridCount) {
            for (col in 0 until gridCount) {
                // Skip finders and associated quiet zones
                val isTopLeftFinderOrQuiet = row <= 7 && col <= 7
                val isTopRightFinderOrQuiet = row <= 7 && col >= (gridCount - 8)
                val isBottomLeftFinderOrQuiet = row >= (gridCount - 8) && col <= 7
                
                if (isTopLeftFinderOrQuiet || isTopRightFinderOrQuiet || isBottomLeftFinderOrQuiet) {
                    continue
                }

                // Skip bottom-right alignment pattern
                if (row >= 20 && row <= 24 && col >= 20 && col <= 24) {
                    continue
                }

                // Skip center overlay logo zone (center 11-17 inclusive)
                if (row >= 11 && row <= 17 && col >= 11 && col <= 17) {
                    continue
                }

                // Horizontal timing track (row 6)
                if (row == 6) {
                    if (col % 2 == 0) {
                        paint.color = Color.BLACK
                        canvas.drawRect(x + col * dotSize, y + row * dotSize, x + (col + 1) * dotSize, y + (row + 1) * dotSize, paint)
                    }
                    continue
                }

                // Vertical timing track (col 6)
                if (col == 6) {
                    if (row % 2 == 0) {
                        paint.color = Color.BLACK
                        canvas.drawRect(x + col * dotSize, y + row * dotSize, x + (col + 1) * dotSize, y + (row + 1) * dotSize, paint)
                    }
                    continue
                }

                // Balanced standard QR bit density
                val isDark = if (row % 3 == 0 && col % 3 == 0) {
                    random.nextFloat() > 0.35f
                } else {
                    random.nextFloat() > 0.50f
                }

                if (isDark) {
                    paint.color = Color.BLACK
                    canvas.drawRect(x + col * dotSize, y + row * dotSize, x + (col + 1) * dotSize, y + (row + 1) * dotSize, paint)
                }
            }
        }
        
        // 4. Draw central CBE Birr Plus logo card
        val centerSize = dotSize * 6.5f
        val cx = x + (size - centerSize) / 2
        val cy = y + (size - centerSize) / 2
        
        val radius = dotSize * 1.5f
        
        // Draw outer logo card container base (white)
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(cx, cy, cx + centerSize, cy + centerSize, radius, radius, paint)
        
        // Draw primary violet card body (slightly inset)
        val cardSize = centerSize - dotSize * 1.2f
        val ccx = cx + dotSize * 0.6f
        val ccy = cy + dotSize * 0.6f
        val mRadius = dotSize * 1.0f
        paint.color = 0xFF6A1A78.toInt() // Sweet CBE purple/violet base color matching user reference image
        canvas.drawRoundRect(ccx, ccy, ccx + cardSize, ccy + cardSize, mRadius, mRadius, paint)
        
        // Draw white border inside card
        val borderPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 0.35f * dotSize
            isAntiAlias = true
        }
        val innerMargin = 0.35f * dotSize
        val iRadius = dotSize * 0.7f
        canvas.drawRoundRect(
            ccx + innerMargin, 
            ccy + innerMargin, 
            ccx + cardSize - innerMargin, ccy + cardSize - innerMargin, 
            iRadius, iRadius, borderPaint
        )
        
        // Draw light shimmer background inside card
        val shinePaint = Paint().apply {
            color = 0x14FFFFFF // 0.08 alpha white
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val shinePath = Path().apply {
            moveTo(ccx, ccy)
            lineTo(ccx + cardSize * 0.45f, ccy)
            lineTo(ccx + cardSize * 0.22f, ccy + cardSize)
            lineTo(ccx, ccy + cardSize)
            close()
        }
        canvas.drawPath(shinePath, shinePaint)
        
        // Draw logo texts
        val pCbe = Paint().apply {
            color = Color.WHITE
            textSize = cardSize * 0.16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("CBE", ccx + cardSize * 0.12f, ccy + cardSize * 0.26f, pCbe)
        
        val pPlus = Paint().apply {
            color = 0xFFE9C5A2.toInt() // Gold color
            textSize = cardSize * 0.14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("Plus", ccx + cardSize * 0.62f, ccy + cardSize * 0.26f, pPlus)
        
        // "Birr"
        val pBirr = Paint().apply {
            color = Color.BLACK
            textSize = cardSize * 0.42f
            typeface = Typeface.create("sans-serif", Typeface.BOLD_ITALIC)
            isAntiAlias = true
        }
        val textWidth = pBirr.measureText("Birr")
        val bX = ccx + (cardSize - textWidth) / 2f - cardSize * 0.02f
        val bY = ccy + cardSize * 0.66f
        canvas.drawText("Birr", bX, bY, pBirr)
        
        // Amharic Slogan
        val pSlogan = Paint().apply {
            color = Color.WHITE
            textSize = cardSize * 0.12f
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            isAntiAlias = true
        }
        val sloganText = "ባለበት ሁሉ አለ"
        val sloganWidth = pSlogan.measureText(sloganText)
        val sX = ccx + (cardSize - sloganWidth) / 2f
        val sY = ccy + cardSize * 0.88f
        canvas.drawText(sloganText, sX, sY, pSlogan)
        
        // Golden crop flame/petal above 'i'
        val flamePaint = Paint().apply {
            color = 0xFFFFD54F.toInt() // Golden yellow
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val flameSize = cardSize * 0.2f
        val fxOffset = ccx + cardSize * 0.58f
        val fyOffset = ccy + cardSize * 0.35f
        val petalPath = Path().apply {
            moveTo(fxOffset + flameSize * 0.5f, fyOffset + flameSize)
            cubicTo(
                fxOffset, fyOffset + flameSize * 0.5f,
                fxOffset, fyOffset,
                fxOffset + flameSize * 0.5f, fyOffset
            )
            cubicTo(
                fxOffset + flameSize, fyOffset,
                fxOffset + flameSize, fyOffset + flameSize * 0.5f,
                fxOffset + flameSize * 0.5f, fyOffset + flameSize
            )
            close()
        }
        canvas.drawPath(petalPath, flamePaint)
    }

    private fun getAmountInWords(amount: Double): String {
        val intAmount = amount.toInt()
        return when (intAmount) {
            100 -> "One Hundred"
            150 -> "One Hundred and Fifty"
            200 -> "Two Hundred"
            300 -> "Three Hundred"
            400 -> "Four Hundred"
            490 -> "Four Hundred and Ninety"
            500 -> "Five Hundred"
            1000 -> "One Thousand"
            1500 -> "One Thousand Five Hundred"
            1800 -> "One Thousand Eight Hundred"
            4500 -> "Four Thousand Five Hundred"
            else -> {
                // simple fallback
                val hundreds = intAmount / 100
                val remainder = intAmount % 100
                if (hundreds > 0) {
                    val label = when(hundreds) {
                        1 -> "One"
                        2 -> "Two"
                        3 -> "Three"
                        4 -> "Four"
                        5 -> "Five"
                        else -> "Some"
                    }
                    if (remainder > 0) "$label Hundred and $remainder" else "$label Hundred"
                } else {
                    "Amount of $intAmount"
                }
            }
        }
    }
}
