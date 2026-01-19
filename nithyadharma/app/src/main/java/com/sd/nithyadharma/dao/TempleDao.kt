package com.sd.nithyadharma.dao

import android.util.Log
import androidx.room.Dao
import androidx.room.Query
import com.sd.nithyadharma.model.TempleItem
import kotlinx.coroutines.flow.Flow
import com.sd.nithyadharma.util.Constants
import kotlin.math.*

private const val TABLE_NAME =  Constants.TABLE_NAME

@Dao
interface TempleDao {

    @Query("SELECT * FROM $TABLE_NAME")
    fun getAllTemples(): Flow<List<TempleItem>>

    @Query("SELECT * FROM $TABLE_NAME WHERE temple_id = :templeId LIMIT 1")
    suspend fun getTempleById(templeId: String): TempleItem?

    @Query("UPDATE $TABLE_NAME SET visit_dt = :visitedDate WHERE temple_id = :templeId")
    suspend fun updateTempleVisitedDate(templeId: String, visitedDate: String?)

    @Query("""
        SELECT * FROM $TABLE_NAME
        WHERE CAST(SUBSTR(latlong, 1, INSTR(latlong, ',') - 1) AS REAL) BETWEEN :minLat AND :maxLat
          AND CAST(SUBSTR(latlong, INSTR(latlong, ',') + 1) AS REAL) BETWEEN :minLon AND :maxLon
    """)
    suspend fun getTemplesInBoundingBox(
        minLat: Double, maxLat: Double, minLon: Double, maxLon: Double
    ): List<TempleItem>

    suspend fun getNearbyTemples(centerLat: Double, centerLon: Double, radiusMiles: Double): List<TempleItem> {
        val R_MILES = 3958.8
        val latRad = Math.toRadians(centerLat)
        val degLatKm = 111.0
        val degLonKm = 111.0 * cos(latRad)

        val deltaLat = radiusMiles / (R_MILES * (Math.PI / 180.0))
        val deltaLon = radiusMiles / (R_MILES * cos(latRad) * (Math.PI / 180.0))

        val minLat = centerLat - deltaLat
        val maxLat = centerLat + deltaLat
        val minLon = centerLon - deltaLon
        val maxLon = centerLon + deltaLon

        val clampedMinLon = (minLon + 540) % 360 - 180
        val clampedMaxLon = (maxLon + 540) % 360 - 180

        val sqlQuery = """
            SELECT * FROM $TABLE_NAME
            WHERE CAST(SUBSTR(latlong, 1, INSTR(latlong, ',') - 1) AS REAL) BETWEEN $minLat AND $maxLat
              AND CAST(SUBSTR(latlong, INSTR(latlong, ',') + 1) AS REAL) BETWEEN $minLon AND $maxLon
        """.trimIndent()
        Log.d("TempleDao", "Executing Bounding Box SQL with Parameters: minLat=$minLat, maxLat=$maxLat, minLon=$minLon, maxLon=$maxLon")

        val templesInBoundingBox = getTemplesInBoundingBox(minLat, maxLat, clampedMinLon, clampedMaxLon)
//        Log.d("TempleDao", "Fetched ${templesInBoundingBox.size} nearby temples from DAO")

        return templesInBoundingBox.filter { temple ->
            try {
                val (templeLat, templeLon) = temple.latlong.split(",").map(String::trim).map(String::toDouble)
                val distance = haversineDistanceMiles(centerLat, centerLon, templeLat, templeLon)
                distance <= radiusMiles
            } catch (e: Exception) {
                println("Error parsing latlong for temple ${temple.name}: ${e.message}")
                false
            }
        }
    }

    companion object {
        private fun haversineDistanceMiles(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val R = 3958.8
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2).pow(2.0) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2).pow(2.0)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return R * c
        }
    }
}
