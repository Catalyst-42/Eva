package com.grace.eva.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Tracker(
    val saves: List<String> = emptyList(),
    val currentSaveId: String? = null,
    val remoteServerUrl: String = DEFAULT_REMOTE_SERVER_URL,
)

const val DEFAULT_REMOTE_SERVER_URL = "http://10.0.2.2:8000"

data class ConnectionCheckResult(
    val title: String,
    val isSuccess: Boolean,
    val message: String,
)
