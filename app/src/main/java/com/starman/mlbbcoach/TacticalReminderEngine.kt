package com.starman.mlbbcoach

data class TacticalReminder(
    val command: String,
    val message: String,
    val reason: String,
    val riskLevel: String,
    val confidence: String,
    val nextAction: String
)

object TacticalReminderEngine {

    private val commandMap = mapOf(
        "ATTACK" to TacticalReminder(
            command = "ATTACK",
            message = "Pertimbangkan serangan apabila peluang yang kelihatan memihak kepada pasukan.",
            reason = "Pasukan mempunyai kelebihan momentum atau musuh utama sedang cooldown.",
            riskLevel = "MEDIUM",
            confidence = "HIGH",
            nextAction = "Kunci damage dealer musuh dan terus ambil objektif selepas menang."
        ),
        "ROTATE" to TacticalReminder(
            command = "ROTATE",
            message = "Bantu rakan di Mid Lane.",
            reason = "Gelombang minion telah dibersihkan dan rakan memerlukan sokongan jumlah pemain.",
            riskLevel = "LOW",
            confidence = "HIGH",
            nextAction = "Bergerak melalui laluan hutan sendiri bersama Roamer."
        ),
        "DEFEND" to TacticalReminder(
            command = "DEFEND",
            message = "Lindungi turret dan elakkan pertarungan berisiko.",
            reason = "Pasukan sedang ditekan atau ketinggalan dari segi ekonomi Gold.",
            riskLevel = "MEDIUM",
            confidence = "HIGH",
            nextAction = "Bersihkan gelombang minion menggunakan skill jarak jauh di bawah Turret."
        ),
        "RETREAT" to TacticalReminder(
            command = "RETREAT",
            message = "Undur apabila keadaan yang kelihatan menunjukkan risiko tinggi.",
            reason = "HP rendah atau musuh tidak kelihatan di minimap (risiko serang hendap).",
            riskLevel = "HIGH",
            confidence = "HIGH",
            nextAction = "Kembali ke Turret terdekat dan lakukan Recall dengan selamat."
        ),
        "OBJECTIVE" to TacticalReminder(
            command = "OBJECTIVE",
            message = "Pertimbangkan Turtle, Lord atau turret berdasarkan maklumat yang kelihatan.",
            reason = "Masa objektif neutral aktif dan kawasan sungai boleh dikawal.",
            riskLevel = "MEDIUM",
            confidence = "MEDIUM",
            nextAction = "Kawal semak di sekeliling pit 20 saat awal dan sediakan Retribution."
        ),
        "FARM" to TacticalReminder(
            command = "FARM",
            message = "Utamakan farm apabila pasukan memerlukan gold.",
            reason = "Item teras dan item counter utama masih belum lengkap untuk teamfight 5v5.",
            riskLevel = "LOW",
            confidence = "HIGH",
            nextAction = "Ambil minion dan kem hutan berdekatan tanpa melintasi sungai seorang diri."
        ),
        "WAIT" to TacticalReminder(
            command = "WAIT",
            message = "Tunggu maklumat atau peluang yang lebih baik.",
            reason = "Kedudukan musuh dan status spell lawan belum dapat disahkan dengan jelas.",
            riskLevel = "LOW",
            confidence = "MEDIUM",
            nextAction = "Tunggu Roamer membuka penglihatan semak sebelum bergerak."
        )
    )

    fun getTacticalCommand(key: String): TacticalReminder {
        return commandMap[key.uppercase()] ?: commandMap.getValue("WAIT")
    }
}
