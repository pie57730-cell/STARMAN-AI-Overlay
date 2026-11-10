package com.starman.mlbbcoach

data class MlbbItem(
    val id: String,
    val name: String,
    val category: String,
    val summary: String
)

object ItemDatabase {

    private val items = mutableMapOf(
        "sea_halberd" to MlbbItem(
            id = "sea_halberd",
            name = "Sea Halberd",
            category = "Physical Anti-Heal",
            summary = "Kurangkan 50% Shield & HP Regen musuh serta tambah damage lawan HP tinggi."
        ),
        "glowing_wand" to MlbbItem(
            id = "glowing_wand",
            name = "Glowing Wand",
            category = "Magic Anti-Heal",
            summary = "Bakar HP musuh dan aktifkan kesan Anti-Heal 50% untuk serangan Magic."
        ),
        "dominance_ice" to MlbbItem(
            id = "dominance_ice",
            name = "Dominance Ice",
            category = "Defense Anti-Heal & Anti-Attack Speed",
            summary = "Kurangkan Attack Speed penyerang kepada 80% dan potong 50% Regen/Shield."
        ),
        "athenas_shield" to MlbbItem(
            id = "athenas_shield",
            name = "Athena's Shield",
            category = "Anti-Magic Burst",
            summary = "Kurangkan 25% Magic Damage selama 3 saat apabila menerima serangan Burst Magic."
        ),
        "radiant_armor" to MlbbItem(
            id = "radiant_armor",
            name = "Radiant Armor",
            category = "Continuous Magic Defense",
            summary = "Tingkatkan Magic Defense secara berlapis terhadap kerosakan Magic berterusan."
        ),
        "antique_cuirass" to MlbbItem(
            id = "antique_cuirass",
            name = "Antique Cuirass",
            category = "Anti-Physical Skill",
            summary = "Kurangkan Physical Damage musuh sehingga 24% apabila terkena skill fizikal."
        ),
        "blade_armor" to MlbbItem(
            id = "blade_armor",
            name = "Blade Armor",
            category = "Anti-Crit & Basic Attack",
            summary = "Pantulkan Basic Attack kepada penyerang dan kurangkan 20% Critical Damage."
        ),
        "wind_of_nature" to MlbbItem(
            id = "wind_of_nature",
            name = "Wind of Nature",
            category = "Physical Immunity",
            summary = "Aktifkan kebal 100% terhadap semua Physical Damage selama 2 saat untuk Marksman."
        ),
        "demon_hunter_sword" to MlbbItem(
            id = "demon_hunter_sword",
            name = "Demon Hunter Sword",
            category = "Anti-HP Tank",
            summary = "Berikan kerosakan tambahan berdasarkan peratus baki HP sasaran."
        ),
        "malefic_roar" to MlbbItem(
            id = "malefic_roar",
            name = "Malefic Roar",
            category = "Physical Penetration",
            summary = "Menembusi Physical Defense tinggi Tank musuh sehingga 60%."
        ),
        "divine_glaive" to MlbbItem(
            id = "divine_glaive",
            name = "Divine Glaive",
            category = "Magic Penetration",
            summary = "Menembusi Magic Defense tinggi musuh."
        ),
        "tough_boots" to MlbbItem(
            id = "tough_boots",
            name = "Tough Boots",
            category = "Crowd Control Reduction",
            summary = "Kurangkan tempoh Crowd Control (Stun/Slow/Terrify) sebanyak 30%."
        ),
        "immortality" to MlbbItem(
            id = "immortality",
            name = "Immortality",
            category = "Second Life Defense",
            summary = "Hidup semula selepas 2.5 saat apabila terbunuh dalam pertempuran."
        )
    )

    fun getById(id: String): MlbbItem {
        return items[id] ?: items.getValue("sea_halberd")
    }
}
