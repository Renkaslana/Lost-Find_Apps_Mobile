package com.campus.lostfound.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object WhatsAppUtil {
    /**
     * Format nomor telepon Indonesia ke format internasional
     * Contoh: 08123456789 -> 628123456789
     *         0812-3456-7890 -> 6281234567890
     *         628123456789 -> 628123456789 (sudah benar)
     */
    fun formatPhoneNumber(phoneNumber: String): String {
        // Hapus semua karakter non-digit
        val cleanNumber = phoneNumber.replace(Regex("[^0-9]"), "")
        
        // Jika nomor dimulai dengan 0, ganti dengan 62
        return when {
            cleanNumber.startsWith("0") -> {
                "62${cleanNumber.substring(1)}"
            }
            cleanNumber.startsWith("62") -> {
                cleanNumber
            }
            cleanNumber.startsWith("+62") -> {
                cleanNumber.substring(1) // Hapus tanda +
            }
            else -> {
                // Jika tidak dimulai dengan 0 atau 62, anggap sudah format internasional
                // atau tambahkan 62 jika panjangnya 10-12 digit (nomor Indonesia)
                if (cleanNumber.length in 10..12) {
                    "62$cleanNumber"
                } else {
                    cleanNumber
                }
            }
        }
    }
    
    /**
     * Validasi format nomor telepon Indonesia
     */
    fun isValidIndonesianPhoneNumber(phoneNumber: String): Boolean {
        val cleanNumber = phoneNumber.replace(Regex("[^0-9]"), "")
        return when {
            cleanNumber.startsWith("0") -> cleanNumber.length in 10..13
            cleanNumber.startsWith("62") -> cleanNumber.length in 11..14
            cleanNumber.startsWith("+62") -> cleanNumber.length in 12..15
            else -> false
        }
    }
    
    fun openWhatsApp(context: Context, phoneNumber: String, itemName: String, type: String) {
        try {
            val formattedNumber = formatPhoneNumber(phoneNumber)
            val message = "Halo, saya melihat laporan $type \"$itemName\" di Campus Lost & Found. Apakah barang ini masih tersedia?"
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/$formattedNumber?text=${Uri.encode(message)}")
                setPackage("com.whatsapp")
            }
            
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // Fallback to web WhatsApp
                val webIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://wa.me/$formattedNumber?text=${Uri.encode(message)}")
                }
                context.startActivity(webIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal membuka WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }
}

