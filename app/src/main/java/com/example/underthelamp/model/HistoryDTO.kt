package com.example.underthelamp.model

import com.google.firebase.Timestamp

data class HistoryDTO (
    var history: String? = null, // 검색 기록
    var timestamp: Timestamp? = null    // 검색 시간
)