package com.nilemnod.mangareader.domain.source.api

import com.nilemnod.mangareader.domain.source.model.LOCAL_SOURCE_ID

fun ContentSource.isLocalOrStub(): Boolean = id == LOCAL_SOURCE_ID || isStub
