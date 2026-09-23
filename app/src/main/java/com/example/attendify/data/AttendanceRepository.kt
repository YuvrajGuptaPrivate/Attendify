package com.example.attendify.data


import kotlinx.coroutines.flow.Flow

class AttendanceRepository(
    private val staffDao: StaffDao,
    private val attendanceDao: AttendanceDao
) {
    fun getAllStaff(): Flow<List<StaffEntity>> = staffDao.getAllStaff()

    fun getStaffById(staffId: Long): Flow<StaffEntity?> = staffDao.getStaffById(staffId)

    suspend fun addStaff(employeeId: String, name: String): Long =
        staffDao.insert(StaffEntity(employeeId = employeeId, name = name))

    suspend fun enrollFace(staffId: Long, embedding: FloatArray) {
        val converters = Converters()
        val serialized = converters.fromFloatArray(embedding) ?: ""
        staffDao.updateFaceEmbedding(staffId, serialized, System.currentTimeMillis())
    }

    suspend fun seedDemoStaffIfEmpty() {
        if (staffDao.getCount() == 0) {
            staffDao.insert(StaffEntity(employeeId = "EMP001", name = "Rahul Sharma"))
        }
    }

    fun getAttendanceForStaff(staffId: Long): Flow<List<AttendanceEntity>> =
        attendanceDao.getAttendanceForStaff(staffId)

    suspend fun recordAttendance(
        staffId: Long,
        latitude: Double?,
        longitude: Double?,
        selfiePath: String,
        matchScore: Float
    ) {
        attendanceDao.insert(
            AttendanceEntity(
                staffId = staffId,
                timestamp = System.currentTimeMillis(),
                latitude = latitude,
                longitude = longitude,
                selfiePath = selfiePath,
                matchScore = matchScore
            )
        )
    }
}