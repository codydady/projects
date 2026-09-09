package com.sd.nithyadharma.util

import com.sd.nithyadharma.model.HoroscopeAttr
import com.sd.nithyadharma.model.HoroscopeAttr.Planet
import com.sd.nithyadharma.model.NDLanguage
import com.sd.nithyadharma.model.PanchangaAttr.Rasi
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

// ============================================================================
// 1. DOMAIN MODELS & ENUMS
// ============================================================================

@Serializable
enum class OutcomeCategory(val title: String, val leadPlanet: Planet) {
    HEALTH("rp_health_vitality", Planet.SUN),
    PEACE_OF_MIND("rp_pom_mood", Planet.MOON),
    PROPERTY_COURAGE("rp_property_courage", Planet.MARS),
    EDUCATION_COMMERCE("rp_edu_commerce", Planet.MERCURY),
    FINANCE_LUCK("rp_finance_luck", Planet.JUPITER),
    RELATIONSHIPS("rp_reltn_comfort", Planet.VENUS),
    CAREER_EMPLOYMENT("rp_empl_career", Planet.SATURN)
}
//enum class OutcomeCategory(val title: String, val leadPlanet: Planet) {
//    HEALTH("Health & Vitality", Planet.SUN),
//    PEACE_OF_MIND("Peace of Mind & Mood", Planet.MOON),
//    PROPERTY_COURAGE("Property & Courage", Planet.MARS),
//    EDUCATION_COMMERCE("Education & Commerce", Planet.MERCURY),
//    FINANCE_LUCK("Finance & Luck", Planet.JUPITER),
//    RELATIONSHIPS("Relationships & Comforts", Planet.VENUS),
//    CAREER_EMPLOYMENT("Employment & Career", Planet.SATURN)
//}

@Serializable
enum class PlanetaryFriendshipStatus {
    FRIEND, NEUTRAL, ENEMY
}

// Extension to get the ruling Planet (Lord) for each Rasi in PanchangaAttr.Rasi
@Serializable
val Rasi.lord: Planet
    get() = when (this) {
        Rasi.MESHA, Rasi.VRISHCHIKA -> Planet.MARS
        Rasi.VRISHABHA, Rasi.THULAA -> Planet.VENUS
        Rasi.MITHUNA, Rasi.KANYA -> Planet.MERCURY
        Rasi.KARKATAKA -> Planet.MOON
        Rasi.SIMHA -> Planet.SUN
        Rasi.DHANUS, Rasi.MEENA -> Planet.JUPITER
        Rasi.MAKARA, Rasi.KUMBHA -> Planet.SATURN
    }

// Map of current planetary positions (Planet -> Sign/Rasi it occupies today)
@Serializable
data class DailyPlanetaryPositions(
    val positions: Map<Planet, Rasi>
)

@Serializable
data class OutcomeResult(
    val category: OutcomeCategory,
    val leadingPlanet: Planet,
    val transitHouseFromUser: Int,
    val starRating: Double, // 0.0 to 5.0
    val summary: String
)

@Serializable
data class DailyRasiPalanReport(
    val userRasi: Rasi,
    val isChandrashtama: Boolean,
    val overallDayRating: Double,
    val outcomes: List<OutcomeResult>
)

// ============================================================================
// 2. ASTROLOGICAL RULES & MATRIX DATA
// ============================================================================

object AstrologyMatrix {

    // Favorable relative house transits from Janma Rasi (Gocharam)
    val favorableHouses = mapOf(
        Planet.SUN to setOf(3, 6, 10, 11),
        Planet.MOON to setOf(1, 3, 6, 7, 10, 11),
        Planet.MARS to setOf(3, 6, 11),
        Planet.MERCURY to setOf(2, 4, 6, 8, 10, 11),
        Planet.JUPITER to setOf(2, 5, 7, 9, 11),
        Planet.VENUS to setOf(1, 2, 3, 4, 5, 8, 9, 11, 12),
        Planet.SATURN to setOf(3, 6, 11)
    )

    // Unfavorable relative house transits (Trik / Kantaka)
    val unfavorableHouses = mapOf(
        Planet.SUN to setOf(4, 7, 8, 12),
        Planet.MOON to setOf(4, 8, 12),
        Planet.MARS to setOf(4, 7, 8, 12),
        Planet.MERCURY to setOf(3, 12),
        Planet.JUPITER to setOf(4, 6, 8, 12),
        Planet.VENUS to setOf(6, 7),
        Planet.SATURN to setOf(4, 7, 8, 12)
    )

    // Exaltation (Ucha) and Own Signs (Swa Kshetra) for Dignity Modifiers
    val exaltedSigns = mapOf(
        Planet.SUN to Rasi.MESHA,
        Planet.MOON to Rasi.VRISHABHA,
        Planet.MARS to Rasi.MAKARA,
        Planet.MERCURY to Rasi.KANYA,
        Planet.JUPITER to Rasi.KARKATAKA,
        Planet.VENUS to Rasi.MEENA,
        Planet.SATURN to Rasi.THULAA
    )

    val ownSigns = mapOf(
        Planet.SUN to setOf(Rasi.SIMHA),
        Planet.MOON to setOf(Rasi.KARKATAKA),
        Planet.MARS to setOf(Rasi.MESHA, Rasi.VRISHCHIKA),
        Planet.MERCURY to setOf(Rasi.MITHUNA, Rasi.KANYA),
        Planet.JUPITER to setOf(Rasi.DHANUS, Rasi.MEENA),
        Planet.VENUS to setOf(Rasi.VRISHABHA, Rasi.THULAA),
        Planet.SATURN to setOf(Rasi.MAKARA, Rasi.KUMBHA)
    )

    // Natural Planetary Relationships (PlanetaryFriendshipStatus Matrix)
    fun getFriendship(planet: Planet, targetLord: Planet): PlanetaryFriendshipStatus {
        if (planet == targetLord) return PlanetaryFriendshipStatus.FRIEND
        return when (planet) {
            Planet.SUN -> when (targetLord) {
                Planet.MOON, Planet.MARS, Planet.JUPITER -> PlanetaryFriendshipStatus.FRIEND
                Planet.MERCURY -> PlanetaryFriendshipStatus.NEUTRAL
                Planet.VENUS, Planet.SATURN -> PlanetaryFriendshipStatus.ENEMY
                else -> PlanetaryFriendshipStatus.NEUTRAL
            }
            Planet.MOON -> when (targetLord) {
                Planet.SUN, Planet.MERCURY -> PlanetaryFriendshipStatus.FRIEND
                Planet.MARS, Planet.JUPITER, Planet.VENUS, Planet.SATURN -> PlanetaryFriendshipStatus.NEUTRAL
                else -> PlanetaryFriendshipStatus.NEUTRAL
            }
            Planet.MARS -> when (targetLord) {
                Planet.SUN, Planet.MOON, Planet.JUPITER -> PlanetaryFriendshipStatus.FRIEND
                Planet.VENUS, Planet.SATURN -> PlanetaryFriendshipStatus.NEUTRAL
                Planet.MERCURY -> PlanetaryFriendshipStatus.ENEMY
                else -> PlanetaryFriendshipStatus.NEUTRAL
            }
            Planet.MERCURY -> when (targetLord) {
                Planet.SUN, Planet.VENUS -> PlanetaryFriendshipStatus.FRIEND
                Planet.MARS, Planet.JUPITER, Planet.SATURN -> PlanetaryFriendshipStatus.NEUTRAL
                Planet.MOON -> PlanetaryFriendshipStatus.ENEMY
                else -> PlanetaryFriendshipStatus.NEUTRAL
            }
            Planet.JUPITER -> when (targetLord) {
                Planet.SUN, Planet.MOON, Planet.MARS -> PlanetaryFriendshipStatus.FRIEND
                Planet.SATURN -> PlanetaryFriendshipStatus.NEUTRAL
                Planet.MERCURY, Planet.VENUS -> PlanetaryFriendshipStatus.ENEMY
                else -> PlanetaryFriendshipStatus.NEUTRAL
            }
            Planet.VENUS -> when (targetLord) {
                Planet.MERCURY, Planet.SATURN -> PlanetaryFriendshipStatus.FRIEND
                Planet.MARS, Planet.JUPITER -> PlanetaryFriendshipStatus.NEUTRAL
                Planet.SUN, Planet.MOON -> PlanetaryFriendshipStatus.ENEMY
                else -> PlanetaryFriendshipStatus.NEUTRAL
            }
            Planet.SATURN -> when (targetLord) {
                Planet.MERCURY, Planet.VENUS -> PlanetaryFriendshipStatus.FRIEND
                Planet.JUPITER -> PlanetaryFriendshipStatus.NEUTRAL
                Planet.SUN, Planet.MOON, Planet.MARS -> PlanetaryFriendshipStatus.ENEMY
                else -> PlanetaryFriendshipStatus.NEUTRAL
            }
            else -> PlanetaryFriendshipStatus.NEUTRAL
        }
    }
}

// ============================================================================
// 3. RASI PALAN CALCULATION ENGINE
// ============================================================================

object RasiPalanEngine {

    /**
     * Calculates house position of a planet relative to the user's Rasi (1 to 12).
     */
    fun calculateHouseOffset(userRasi: Rasi, planetRasi: Rasi): Int {
        return ((planetRasi.ordinal - userRasi.ordinal + 12) % 12) + 1
    }

    /**
     * Determines the Moon Multiplier based on the Moon's daily transit house.
     */
    private fun getMoonMultiplier(moonHouse: Int): Double {
        return when (moonHouse) {
            3, 6, 10, 11 -> 1.15   // Favorable Moon: Boosts all outcomes
            1, 2, 5, 7, 9 -> 1.00  // Neutral Moon
            4, 12 -> 0.85          // Minor fatigue/delay
            8 -> 0.50              // Chandrashtama: Halves overall effectiveness
            else -> 1.00
        }
    }

    /**
     * Calculates the factor score (0.0 - 5.0) for a specific outcome domain.
     */
    private fun evaluateOutcome(
        userRasi: Rasi,
        category: OutcomeCategory,
        positions: DailyPlanetaryPositions,
        moonMultiplier: Double
    ): OutcomeResult {
        val leadPlanet = category.leadPlanet
        val planetSign = positions.positions[leadPlanet]
            ?: error("Missing position for planet: $leadPlanet")

        val house = calculateHouseOffset(userRasi, planetSign)

        // 1. Base Score from Transit House
        var score = when {
            AstrologyMatrix.favorableHouses[leadPlanet]?.contains(house) == true -> 4.2
            AstrologyMatrix.unfavorableHouses[leadPlanet]?.contains(house) == true -> 1.8
            else -> 3.0
        }

        // 2. Dignity Modifier (Exalted / Own Sign)
        if (AstrologyMatrix.exaltedSigns[leadPlanet] == planetSign) {
            score += 0.8
        } else if (AstrologyMatrix.ownSigns[leadPlanet]?.contains(planetSign) == true) {
            score += 0.5
        }

        // 3. PlanetaryFriendshipStatus Modifier with User's Rasi Lord
        val userRasiLord = userRasi.lord
        when (AstrologyMatrix.getFriendship(leadPlanet, userRasiLord)) {
            PlanetaryFriendshipStatus.FRIEND -> score += 0.3
            PlanetaryFriendshipStatus.NEUTRAL -> score += 0.0
            PlanetaryFriendshipStatus.ENEMY -> score -= 0.4
        }

        // 4. Superimpose Moon Multiplier
        score *= moonMultiplier

        // Clamp final score strictly between 0.5 and 5.0 stars
        val finalRating = (score.coerceIn(0.5, 5.0) * 10).roundToInt() / 10.0

        // Generate text summary
        val summary = generateSummary(category, finalRating, moonMultiplier == 0.50)

        return OutcomeResult(
            category = category,
            leadingPlanet = leadPlanet,
            transitHouseFromUser = house,
            starRating = finalRating,
            summary = summary
        )
    }

    fun generateRasiPalanReport(userRasi: Rasi, positions: DailyPlanetaryPositions): DailyRasiPalanReport {
        val moonSign = positions.positions[Planet.MOON] ?: error("Moon position required")
        val moonHouse = calculateHouseOffset(userRasi, moonSign)
        val isChandrashtama = (moonHouse == 8)
        val moonMultiplier = getMoonMultiplier(moonHouse)

        val outcomes = OutcomeCategory.entries.map { category ->
            evaluateOutcome(userRasi, category, positions, moonMultiplier)
        }

        val overallRating = (outcomes.map { it.starRating }.average() * 10).roundToInt() / 10.0

        return DailyRasiPalanReport(
            userRasi = userRasi,
            isChandrashtama = isChandrashtama,
            overallDayRating = overallRating,
            outcomes = outcomes
        )
    }

    private fun generateSummary(
        category: OutcomeCategory,
        rating: Double,
        isChandrashtama: Boolean
    ): String {
        if (isChandrashtama) {
            return "chandrashtama_warning"
        }
        return when {
            rating >= 4.0 -> "rating_high"
            rating >= 2.8 -> "rating_moderate"
            else -> "rating_low"
        }
    }
}

// ============================================================================
// 4. TEST EXECUTION
// ============================================================================

fun main() {
//    val todayPositions = DailyPlanetaryPositions(
//        positions = mapOf(
//            Planet.SUN to Rasi.KARKATAKA,
//            Planet.MOON to Rasi.KANYA,
//            Planet.MARS to Rasi.SIMHA,
//            Planet.MERCURY to Rasi.KARKATAKA,
//            Planet.JUPITER to Rasi.VRISHABHA,
//            Planet.VENUS to Rasi.SIMHA,
//            Planet.SATURN to Rasi.KUMBHA,
//            Planet.RAAHU to Rasi.MEENA,
//            Planet.KETHU to Rasi.KANYA
//        )
//    )
//
//    println("==================================================================")
//    println("          DAILY RASI PALAN ENGINE OUTPUT COMPARISON               ")
//    println("==================================================================\n")
//
//    // Test 1: Vrishabha -> Sun in 3rd House (Enemy Venus Lord)
//    val vrishabhaReport = RasiPalanEngine.generateRasiPalanReport(Rasi.VRISHABHA, todayPositions)
//    printReport(vrishabhaReport)

    // 1. Create instance
    val calculator = HoroscopeCalculator()

    // 2. Call default function (uses getCurrentTime() automatically)
    val currentPlanetaryPositions = calculator.calcPlanetaryPositions()

    val nreport = RasiPalanEngine.generateRasiPalanReport(Rasi.VRISHABHA, currentPlanetaryPositions)
    printReport(nreport)
    println("\n------------------------------------------------------------------\n")

//    // Test 2: Mithuna -> Sun in 2nd House (Friendly Mercury Lord)
//    val mithunaReport = RasiPalanEngine.generateReport(Rasi.MITHUNA, todayPositions)
//    printReport(mithunaReport)
}

fun printReport(report: DailyRasiPalanReport) {
    println("USER RASI: ${report.userRasi.name}")
    println("Chandrashtama Alert: ${if (report.isChandrashtama) "YES (Caution Required)" else "No"}")
    println("Overall Day Rating: ${report.overallDayRating} / 5.0 Stars\n")
    println("OUTCOME MATRIX BREAKDOWN:")

    report.outcomes.forEach { outcome ->
        val stars = "★".repeat(outcome.starRating.toInt()) + if (outcome.starRating % 1 >= 0.5) "½" else ""
        val planetDisplayName = HoroscopeAttr.planetName(outcome.leadingPlanet, NDLanguage.EN)
        println(
            "• %-22s [%-3s] | Rating: %-4s (%-3.1f / 5.0) | House: %2d | %s".format(
                outcome.category.title,
                planetDisplayName,
                stars,
                outcome.starRating,
                outcome.transitHouseFromUser,
                outcome.summary
            )
        )
    }
}