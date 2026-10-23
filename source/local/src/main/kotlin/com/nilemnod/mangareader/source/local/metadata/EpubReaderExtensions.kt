package com.nilemnod.mangareader.source.local.metadata

import com.nilemnod.mangareader.core.archive.EpubReader
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class EpubMetadata(
    val author: String?,
    val description: String?,
    val chapterName: String?,
    val scanlator: String?,
    val dateUpload: Long?,
)

fun EpubReader.readMetadata(): EpubMetadata {
    val ref = getPackageHref()
    val doc = getPackageDocument(ref)

    val title = doc.getElementsByTag("dc:title").first()
    val publisher = doc.getElementsByTag("dc:publisher").first()
    val creator = doc.getElementsByTag("dc:creator").first()
    val description = doc.getElementsByTag("dc:description").first()
    var date = doc.getElementsByTag("dc:date").first()
    if (date == null) {
        date = doc.select("meta[property=dcterms:modified]").first()
    }

    val scanlator = publisher?.text() ?: creator?.text()

    var dateUpload: Long? = null
    if (date != null) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
        try {
            dateUpload = dateFormat.parse(date.text())?.time
        } catch (e: ParseException) {
            // Empty
        }
    }

    return EpubMetadata(
        author = creator?.text(),
        description = description?.text(),
        chapterName = title?.text(),
        scanlator = scanlator,
        dateUpload = dateUpload,
    )
}
