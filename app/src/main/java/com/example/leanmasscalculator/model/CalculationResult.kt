package com.example.leanmasscalculator.model

data class CalculationResult(
    val id: Long = 0,       // ID SQLite ( auto - incremente )
    val firestoreId: String = "",       // ID du document Firestore
    val userId: String = "",        // UID Firebase de l ’ utilisateur
    val weight: Double = 0.0,        // Poids en kg
    val height: Double = 0.0,       // Taille en cm
    val sex: String = "",        // " H " = Homme , " F " = Femme
    val lbm: Double = 0.0,      // Masse maigre calculée en ( kg )
    val date: Long = System.currentTimeMillis(),         // Timestamp ms
    val isSatisfactory: Boolean = false      // true si LBM >= norme
)

//le mot clé: data class en kotlin génère automatiquement equals(), HashCode(), toString(), et copy()
//Idéal pour les objets qui représentent des données pures sans comportement métier