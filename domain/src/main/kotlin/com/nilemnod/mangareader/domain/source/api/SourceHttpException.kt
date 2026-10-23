package com.nilemnod.mangareader.domain.source.api

import java.io.IOException

class SourceHttpException(val code: Int) : IOException("HTTP error $code")
