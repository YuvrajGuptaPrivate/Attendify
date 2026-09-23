package com.example.attendify.data


import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "attendance",
    foreignKeys = [
        ForeignKey(
            entity = StaffEntity::class,
            parentColumns = ["id"],
            childColumns = ["staffId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val staffId: Long,
    val timestamp: Long,
    val latitude: Double?,
    val longitude: Double?,
    val selfiePath: String,
    val matchScore: Float
)




@Dao
interface AttendanceDao {
    @Insert
    suspend fun insert(attendance: AttendanceEntity): Long

    @Query("SELECT * FROM attendance WHERE staffId = :staffId ORDER BY timestamp DESC")
    fun getAttendanceForStaff(staffId: Long): Flow<List<AttendanceEntity>>
}