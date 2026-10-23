package com.nilemnod.mangareader.core.download

import com.nilemnod.mangareader.core.metadata.comicinfo.ComicInfo
import com.nilemnod.mangareader.core.metadata.comicinfo.ComicInfoPublishingStatus
import com.nilemnod.mangareader.domain.chapter.model.Chapter
import com.nilemnod.mangareader.domain.manga.model.Manga
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

/**
 * Creates a ComicInfo instance based on the manga and chapter metadata.
 */
fun getComicInfo(
    manga: Manga,
    chapter: Chapter,
    urls: List<String>,
    categories: List<String>?,
    sourceName: String,
): ComicInfo {
    val date = chapter.dateUpload?.toLocalDateTime(TimeZone.currentSystemDefault())

    return ComicInfo(
        title = ComicInfo.Title(chapter.name),
        series = ComicInfo.Series(manga.title),
        number = chapter.chapterNumber.takeIf { it >= 0 }?.let {
            if ((it.rem(1) == 0.0)) {
                ComicInfo.Number(it.toInt().toString())
            } else {
                ComicInfo.Number(it.toString())
            }
        },
        web = ComicInfo.Web(urls.joinToString(" ")),
        summary = manga.description?.let { ComicInfo.Summary(it) },
        writer = manga.author?.let { ComicInfo.Writer(it) },
        penciller = manga.artist?.let { ComicInfo.Penciller(it) },
        translator = chapter.scanlator?.let { ComicInfo.Translator(it) },
        genre = manga.genre?.let { ComicInfo.Genre(it.joinToString()) },
        publishingStatus = ComicInfo.PublishingStatusTachiyomi(
            ComicInfoPublishingStatus.toComicInfoValue(manga.status),
        ),
        categories = categories?.let { ComicInfo.CategoriesTachiyomi(it.joinToString()) },
        source = ComicInfo.SourceMihon(sourceName),
        inker = null,
        colorist = null,
        letterer = null,
        coverArtist = null,
        tags = null,
        year = date?.year?.let { ComicInfo.Year(it) },
        month = date?.month?.number?.let { ComicInfo.Month(it) },
        day = date?.day?.let { ComicInfo.Day(it) },
    )
}
