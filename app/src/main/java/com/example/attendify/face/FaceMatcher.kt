package com.example.attendify.face



object FaceMatcher {

    const val MATCH_THRESHOLD = 0.6f

    fun similarity(a: FloatArray, b: FloatArray): Float {
        if (a.size != b.size || a.isEmpty()) return -1f
        var dot = 0f
        var normA = 0f
        var normB = 0f
        for (i in a.indices) {
            dot += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        if (normA == 0f || normB == 0f) return -1f
        return dot / (kotlin.math.sqrt(normA) * kotlin.math.sqrt(normB))
    }
}