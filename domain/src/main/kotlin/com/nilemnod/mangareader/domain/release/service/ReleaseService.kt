package com.nilemnod.mangareader.domain.release.service

import com.nilemnod.mangareader.domain.release.interactor.GetApplicationRelease
import com.nilemnod.mangareader.domain.release.model.Release

interface ReleaseService {

    suspend fun latest(arguments: GetApplicationRelease.Arguments): Release?
}
