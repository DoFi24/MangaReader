package com.nilemnod.mangareader.feature.reader.legacy.model

class InsertPage(val parent: ReaderPage) : ReaderPage(parent.index, parent.url, parent.imageUrl) {

    override var chapter: ReaderChapter = parent.chapter

    init {
        status = State.Ready
        stream = parent.stream
    }
}
