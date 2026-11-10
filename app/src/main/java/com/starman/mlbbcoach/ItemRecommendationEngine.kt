package com.starman.mlbbcoach

data class ItemAdviceResult(
    val primaryItem: MlbbItem,
    val primaryReason: String,
    val priority: String,
    val alternativeItem: MlbbItem,
    val alternativeReason: String
)

object ItemRecommendationEngine {

    fun recommendForThreat(threatCategory: String, userRole: String): ItemAdviceResult {
        if (threatCategory == "UNKNOWN") {
            val fallback = ItemDatabase.getById("tough_boots")
            return ItemAdviceResult(
                primaryItem = fallback,
                primaryReason = "Maklumat visual belum jelas (UNKNOWN). Utamakan mobiliti & pengurangan CC.",
                priority = "LOW",
                alternativeItem = ItemDatabase.getById("immortality"),
                alternativeReason = "Pilihan keselamatan umum sementara menunggu imbasan papan skor."
            )
        }

        return when (threatCategory) {
            "HEAL_REGEN" -> {
                if (userRole == "MID_MAGE") {
                    ItemAdviceResult(
                        primaryItem = ItemDatabase.getById("glowing_wand"),
                        primaryReason = "Potong 50% HP Regen & Shield musuh melalui skill Magic.",
                        priority = "HIGH",
                        alternativeItem = ItemDatabase.getById("divine_glaive"),
                        alternativeReason = "Tambah Magic Penetration jika musuh membina Magic Defense."
                    )
                } else if (userRole == "ROAM_TANK") {
                    ItemAdviceResult(
                        primaryItem = ItemDatabase.getById("dominance_ice"),
                        primaryReason = "Sekat HP Regen & Attack Speed musuh di barisan hadapan.",
                        priority = "HIGH",
                        alternativeItem = ItemDatabase.getById("antique_cuirass"),
                        alternativeReason = "Kurangkan 24% Physical Skill Damage daripada Fighter lawan."
                    )
                } else {
                    ItemAdviceResult(
                        primaryItem = ItemDatabase.getById("sea_halberd"),
                        primaryReason = "Pasif Lifebane memotong 50% HP Regen & Shield musuh (Yu Zhong/Esmeralda).",
                        priority = "HIGH",
                        alternativeItem = ItemDatabase.getById("demon_hunter_sword"),
                        alternativeReason = "Hancurkan bar HP tinggi Fighter/Tank lawan dengan cepat."
                    )
                }
            }
            "ATK_SPEED" -> {
                if (userRole == "GOLD_MM") {
                    ItemAdviceResult(
                        primaryItem = ItemDatabase.getById("wind_of_nature"),
                        primaryReason = "Kebal 100% terhadap Physical Damage selama 2 saat apabila diserang Moskov/Aulus.",
                        priority = "HIGH",
                        alternativeItem = ItemDatabase.getById("sea_halberd"),
                        alternativeReason = "Serta-merta potong Lifesteal Marksman lawan."
                    )
                } else {
                    ItemAdviceResult(
                        primaryItem = ItemDatabase.getById("dominance_ice"),
                        primaryReason = "Kurangkan Attack Speed penyerang kepada 80% dan potong Lifesteal.",
                        priority = "HIGH",
                        alternativeItem = ItemDatabase.getById("blade_armor"),
                        alternativeReason = "Pantulkan Basic Attack dan kurangkan 20% Critical Damage."
                    )
                }
            }
            "MAGIC_BURST" -> {
                ItemAdviceResult(
                    primaryItem = ItemDatabase.getById("athenas_shield"),
                    primaryReason = "Kurangkan 25% Magic Damage selama 3 saat daripada kombo burst Vexana/Eudora.",
                    priority = "HIGH",
                    alternativeItem = ItemDatabase.getById("radiant_armor"),
                    alternativeReason = "Pertahanan Magic berlapis terhadap serangan Magic berterusan."
                )
            }
            else -> {
                ItemAdviceResult(
                    primaryItem = ItemDatabase.getById("malefic_roar"),
                    primaryReason = "Tembusi Physical Defense tebal Tank musuh sehingga 60%.",
                    priority = "MEDIUM",
                    alternativeItem = ItemDatabase.getById("demon_hunter_sword"),
                    alternativeReason = "Berikan kerosakan tambahan berdasarkan baki HP sasaran."
                )
            }
        }
    }
}
