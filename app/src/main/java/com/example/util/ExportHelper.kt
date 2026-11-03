package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.model.RoomMember
import com.example.model.RoomSplit
import com.example.model.Transaction
import com.example.model.UrgentDebt
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

object ExportHelper {

    fun exportDataToCsv(
        context: Context,
        transactions: List<Transaction>,
        roomSplits: List<RoomSplit>,
        roomMembers: List<RoomMember>,
        urgentDebts: List<UrgentDebt>
    ) {
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        val file = File(exportDir, "bao_cao_chi_tieu_${System.currentTimeMillis()}.csv")

        FileOutputStream(file).use { fos ->
            // Write UTF-8 BOM for Microsoft Excel Vietnamese support
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                // Section 1: Transactions
                writer.append("=== LỊCH SỬ THU CHI CÁ NHÂN ===\n")
                writer.append("Thời gian,Loại,Danh mục,Tiêu đề,Số tiền (VNĐ),Ghi chú\n")
                for (t in transactions) {
                    val typeStr = if (t.type == "INCOME") "Thu nhập" else "Chi tiêu"
                    val dateStr = CurrencyFormatter.formatDate(t.timestamp)
                    val safeTitle = escapeCsv(t.title)
                    val safeCat = escapeCsv(t.category)
                    val safeNote = escapeCsv(t.note)
                    writer.append("\"$dateStr\",\"$typeStr\",\"$safeCat\",\"$safeTitle\",${t.amount.toLong()},\"$safeNote\"\n")
                }
                writer.append("\n\n")

                // Section 2: Room splits
                writer.append("=== LỊCH SỬ CHIA TIỀN PHÒNG / ĐI CHỢ ===\n")
                writer.append("Thời gian,Tiêu đề đợt mua,Tổng tiền (VNĐ),Số người ăn,Tiền mỗi người (VNĐ),Thành viên tham gia\n")
                val memberMap = roomMembers.associateBy { it.id }
                for (split in roomSplits) {
                    val dateStr = CurrencyFormatter.formatDate(split.date)
                    val safeTitle = escapeCsv(split.title)
                    val memberNames = split.participantIds.mapNotNull { memberMap[it]?.name }.joinToString("; ")
                    writer.append("\"$dateStr\",\"$safeTitle\",${split.totalAmount.toLong()},${split.participantIds.size},${split.splitAmountPerPerson.toLong()},\"$memberNames\"\n")
                }
                writer.append("\n\n")

                // Section 3: Room members current debt balance
                writer.append("=== DANH SÁCH THÀNH VIÊN VÀ CÔNG NỢ PHÒNG ===\n")
                writer.append("Họ tên,Số điện thoại,Số tiền đang nợ (VNĐ)\n")
                for (m in roomMembers) {
                    val safeName = escapeCsv(m.name)
                    val safePhone = escapeCsv(m.phone)
                    writer.append("\"$safeName\",\"$safePhone\",${m.totalOwed.toLong()}\n")
                }
                writer.append("\n\n")

                // Section 4: Urgent debts
                writer.append("=== DANH SÁCH KHOẢN NỢ KHẨN CẤP ===\n")
                writer.append("Tiêu đề khoản nợ,Phân loại,Đối tác/Người liên hệ,Số tiền (VNĐ),Hạn thanh toán,Trạng thái\n")
                for (d in urgentDebts) {
                    val safeTitle = escapeCsv(d.title)
                    val safeContact = escapeCsv(d.contactName)
                    val typeStr = if (d.isPayable) "Tôi cần trả" else "Người khác nợ tôi"
                    val dueStr = d.dueDate?.let { CurrencyFormatter.formatDateOnly(it) } ?: "Không có hạn"
                    val statusStr = if (d.isSettled) "ĐÃ THANH TOÁN" else "CHƯA THANH TOÁN"
                    writer.append("\"$safeTitle\",\"$typeStr\",\"$safeContact\",${d.amount.toLong()},\"$dueStr\",\"$statusStr\"\n")
                }
                writer.flush()
            }
        }

        // Share via Android Intent
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Báo cáo Chi Tiêu & Sổ Nợ Phòng")
            putExtra(Intent.EXTRA_TEXT, "Đính kèm file CSV báo cáo chi tiêu và sổ nợ phòng.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Chia sẻ hoặc lưu tệp CSV báo cáo")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"")
    }
}
