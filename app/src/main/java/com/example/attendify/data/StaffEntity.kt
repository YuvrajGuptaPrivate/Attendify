package com.example.attendify.data


import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "staff")
data class StaffEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: String,
    val name: String,
    val faceEmbedding: FloatArray? = null,   // null until enrolled
    val faceEnrolledAt: Long? = null
) {
    // Room doesn't need this, but FloatArray breaks data class equals()/hashCode
    // by identity instead of content — override so tests/UI diffing behaves sanely.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StaffEntity) return false
        return id == other.id && employeeId == other.employeeId && name == other.name &&
                faceEnrolledAt == other.faceEnrolledAt &&
                (faceEmbedding?.contentEquals(other.faceEmbedding ?: floatArrayOf()) ?: (other.faceEmbedding == null))
    }
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + employeeId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (faceEmbedding?.contentHashCode() ?: 0)
        return result
    }
}


@Dao
interface StaffDao {
    @Query("SELECT * FROM staff ORDER BY name ASC")
    fun getAllStaff(): Flow<List<StaffEntity>>

    @Query("SELECT * FROM staff WHERE id = :staffId")
    fun getStaffById(staffId: Long): Flow<StaffEntity?>

    @Query("SELECT COUNT(*) FROM staff")
    suspend fun getCount(): Int

    @Insert
    suspend fun insert(staff: StaffEntity): Long

    @Update
    suspend fun update(staff: StaffEntity)

    @Query("UPDATE staff SET faceEmbedding = :embedding, faceEnrolledAt = :enrolledAt WHERE id = :staffId")
    suspend fun updateFaceEmbedding(staffId: Long, embedding: String, enrolledAt: Long)
}