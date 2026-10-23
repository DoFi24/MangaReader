package com.nilemnod.mangareader.core.backup

import com.nilemnod.mangareader.core.backup.models.Backup
import kotlinx.serialization.protobuf.schema.ProtoBufSchemaGenerator

fun generateBackupSchema(): String = ProtoBufSchemaGenerator.generateSchemaText(Backup.serializer().descriptor)
