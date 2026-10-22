package com.nilemnod.mangareader.app.data.backup.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import com.nilemnod.mangareader.core.common.extensions.toInstantOrNull
import com.nilemnod.mangareader.domain.history.model.History

@Serializable
data class BackupHistory(
    @ProtoNumber(1) var url: String,
    @ProtoNumber(2) var lastRead: Long,
    @ProtoNumber(3) var readDuration: Long = 0,
) {
    fun getHistoryImpl(): History {
        return History.create().copy(
            readAt = lastRead.toInstantOrNull(),
            readDuration = readDuration,
        )
    }
}
