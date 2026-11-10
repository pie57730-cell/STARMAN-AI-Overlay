package com.starman.mlbbcoach

data class AnalysisReport(
    val gamePhase: String,
    val teamAdvantage: String,
    val threatLevel: String,
    val itemAdvice: ItemAdviceResult,
    val tacticalReminder: TacticalReminder,
    val confidence: String
)

object AIAnalysisEngine {

    fun analyzeScreenText(rawScreenText: String, userRole: String): AnalysisReport {
        val normalized = rawScreenText.lowercase()

        if (normalized.isBlank()) {
            val unknownItem = ItemRecommendationEngine.recommendForThreat("UNKNOWN", userRole)
            val waitCmd = TacticalReminderEngine.getTacticalCommand("WAIT")
            return AnalysisReport(
                gamePhase = "UNKNOWN",
                teamAdvantage = "UNKNOWN",
                threatLevel = "UNKNOWN",
                itemAdvice = unknownItem,
                tacticalReminder = waitCmd,
                confidence = "LOW"
            )
        }

        // A. Match Phase Analyzer
        val gamePhase = when {
            normalized.contains("early") || normalized.contains("01:") || normalized.contains("02:") || normalized.contains("03:") || normalized.contains("04:") -> "EARLY GAME"
            normalized.contains("mid") || normalized.contains("06:") || normalized.contains("08:") || normalized.contains("10:") || normalized.contains("11:") -> "MID GAME"
            normalized.contains("late") || normalized.contains("13:") || normalized.contains("15:") || normalized.contains("18:") -> "LATE GAME"
            else -> "MID GAME"
        }

        // B. Team Advantage Analyzer
        val teamAdvantage = when {
            normalized.contains("ahead") || normalized.contains("victory") || normalized.contains("lord") -> "AHEAD"
            normalized.contains("behind") || normalized.contains("defend") -> "BEHIND"
            else -> "EVEN"
        }

        // C. Threat Analyzer
        val detectedThreatCategory = when {
            normalized.contains("yu zhong") || normalized.contains("esmeralda") || normalized.contains("estes") || normalized.contains("regen") -> "HEAL_REGEN"
            normalized.contains("moskov") || normalized.contains("aulus") || normalized.contains("miya") || normalized.contains("attack speed") -> "ATK_SPEED"
            normalized.contains("vexana") || normalized.contains("eudora") || normalized.contains("pharsa") || normalized.contains("burst") -> "MAGIC_BURST"
            normalized.contains("hylos") || normalized.contains("tigreal") || normalized.contains("tank") -> "HEAVY_TANK"
            else -> "HEAL_REGEN"
        }

        val threatLabel = when (detectedThreatCategory) {
            "HEAL_REGEN" -> "HIGH (SUSTAIN / REGEN)"
            "ATK_SPEED" -> "HIGH (ATK SPEED / CRIT)"
            "MAGIC_BURST" -> "CRITICAL (MAGIC BURST)"
            "HEAVY_TANK" -> "MEDIUM (ARMOR / TANK)"
            else -> "MEDIUM"
        }

        // D. Item Advisor
        val itemAdvice = ItemRecommendationEngine.recommendForThreat(detectedThreatCategory, userRole)

        // E. Tactical Advisor
        val recommendedCommandKey = when {
            teamAdvantage == "BEHIND" && gamePhase == "LATE GAME" -> "DEFEND"
            teamAdvantage == "BEHIND" -> "FARM"
            normalized.contains("turtle") || normalized.contains("lord") -> "OBJECTIVE"
            teamAdvantage == "AHEAD" -> "ATTACK"
            else -> "ROTATE"
        }
        val tacticalReminder = TacticalReminderEngine.getTacticalCommand(recommendedCommandKey)

        return AnalysisReport(
            gamePhase = gamePhase,
            teamAdvantage = teamAdvantage,
            threatLevel = threatLabel,
            itemAdvice = itemAdvice,
            tacticalReminder = tacticalReminder,
            confidence = tacticalReminder.confidence
        )
    }
}w
