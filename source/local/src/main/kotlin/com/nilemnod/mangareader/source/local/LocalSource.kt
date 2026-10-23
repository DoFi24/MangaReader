package com.nilemnod.mangareader.source.local

import android.content.Context
import com.hippo.unifile.UniFile
import com.nilemnod.mangareader.core.archive.archiveReader
import com.nilemnod.mangareader.core.archive.epubReader
import com.nilemnod.mangareader.core.common.i18n.stringResource
import com.nilemnod.mangareader.core.common.storage.extension
import com.nilemnod.mangareader.core.common.storage.nameWithoutExtension
import com.nilemnod.mangareader.core.common.util.lang.compareToCaseInsensitiveNaturalOrder
import com.nilemnod.mangareader.core.common.util.system.ImageUtil
import com.nilemnod.mangareader.core.common.util.system.logcat
import com.nilemnod.mangareader.core.metadata.comicinfo.COMIC_INFO_FILE
import com.nilemnod.mangareader.core.metadata.comicinfo.ComicInfo
import com.nilemnod.mangareader.core.metadata.comicinfo.copyFromComicInfo
import com.nilemnod.mangareader.core.metadata.comicinfo.toComicInfo
import com.nilemnod.mangareader.core.metadata.details.MangaDetails
import com.nilemnod.mangareader.domain.chapter.service.ChapterRecognition
import com.nilemnod.mangareader.domain.manga.model.Manga
import com.nilemnod.mangareader.domain.source.api.ContentSource
import com.nilemnod.mangareader.domain.source.api.FilterList
import com.nilemnod.mangareader.domain.source.api.ImageContent
import com.nilemnod.mangareader.domain.source.api.MangaListing
import com.nilemnod.mangareader.domain.source.api.MangaRefresh
import com.nilemnod.mangareader.domain.source.api.MangaStatus
import com.nilemnod.mangareader.domain.source.api.Page
import com.nilemnod.mangareader.domain.source.api.SourceChapter
import com.nilemnod.mangareader.domain.source.api.SourceFilter
import com.nilemnod.mangareader.domain.source.api.SourceManga
import com.nilemnod.mangareader.i18n.MR
import com.nilemnod.mangareader.source.local.image.LocalCoverManager
import com.nilemnod.mangareader.source.local.io.Archive
import com.nilemnod.mangareader.source.local.io.Format
import com.nilemnod.mangareader.source.local.io.LocalSourceFileSystem
import com.nilemnod.mangareader.source.local.metadata.readMetadata
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import logcat.LogPriority
import nl.adaptivity.xmlutil.core.AndroidXmlReader
import nl.adaptivity.xmlutil.serialization.XML
import uy.kohesive.injekt.injectLazy
import com.nilemnod.mangareader.domain.source.model.Source as DomainSource
import java.io.InputStream
import java.nio.charset.StandardCharsets
import kotlin.time.Duration.Companion.days

@Inject
@SingleIn(AppScope::class)
class LocalSource(
    private val context: Context,
    private val fileSystem: LocalSourceFileSystem,
    private val coverManager: LocalCoverManager,
) : ContentSource {

    private val json: Json by injectLazy()
    private val xml: XML by injectLazy()

    override val name: String = context.stringResource(MR.strings.local_source)

    override val id: Long = ID

    override val lang: String = "other"

    override val displayName: String
        get() = name

    override val supportsLatest: Boolean = true

    override val isUnmetered: Boolean = true

    override fun filters() = FilterList(listOf(orderBy(Order.TITLE, ascending = true)))

    private fun orderBy(order: Order, ascending: Boolean) = SourceFilter.Sort(
        name = context.stringResource(MR.strings.local_filter_order_by),
        values = listOf(context.stringResource(MR.strings.title), context.stringResource(MR.strings.date)),
        selection = SourceFilter.Sort.Selection(order.ordinal, ascending),
    )

    // Browse related
    override suspend fun popular(page: Int) = search(page, "", filters())

    override suspend fun latest(page: Int) = listing("", Order.DATE, ascending = false, onlyRecent = true)

    override suspend fun search(page: Int, query: String, filters: FilterList): MangaListing {
        val selection = filters.filterIsInstance<SourceFilter.Sort>().firstOrNull()?.selection
        val order = Order.entries.getOrElse(selection?.index ?: 0) { Order.TITLE }
        return listing(query, order, selection?.ascending ?: true, onlyRecent = false)
    }

    private suspend fun listing(
        query: String,
        order: Order,
        ascending: Boolean,
        onlyRecent: Boolean,
    ): MangaListing = withContext(Dispatchers.IO) {
        val lastModifiedLimit = if (onlyRecent) System.currentTimeMillis() - LATEST_THRESHOLD else 0L

        var mangaDirs = fileSystem.getFilesInBaseDirectory()
            // Filter out files that are hidden and is not a folder
            .filter { it.isDirectory && !it.name.orEmpty().startsWith('.') }
            .distinctBy { it.name }
            .filter {
                if (lastModifiedLimit == 0L && query.isBlank()) {
                    true
                } else if (lastModifiedLimit == 0L) {
                    it.name.orEmpty().contains(query, ignoreCase = true)
                } else {
                    it.lastModified() >= lastModifiedLimit
                }
            }

        mangaDirs = when (order) {
            Order.TITLE -> if (ascending) {
                mangaDirs.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name.orEmpty() })
            } else {
                mangaDirs.sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.name.orEmpty() })
            }
            Order.DATE -> if (ascending) {
                mangaDirs.sortedBy(UniFile::lastModified)
            } else {
                mangaDirs.sortedByDescending(UniFile::lastModified)
            }
        }

        val mangas = mangaDirs
            .map { mangaDir ->
                async {
                    SourceManga(
                        url = mangaDir.name.orEmpty(),
                        title = mangaDir.name.orEmpty(),
                        // Try to find the cover
                        thumbnailUrl = coverManager.find(mangaDir.name.orEmpty())?.uri?.toString(),
                    )
                }
            }
            .awaitAll()

        MangaListing(mangas, false)
    }

    override suspend fun refresh(
        manga: SourceManga,
        chapters: List<SourceChapter>,
        fetchDetails: Boolean,
        fetchChapters: Boolean,
    ): MangaRefresh = supervisorScope {
        val asyncManga = if (fetchDetails) async { getMangaDetails(manga) } else null
        val asyncChapters = if (fetchChapters) async { getChapterList(manga) } else null
        val details = asyncManga?.await() ?: manga
        val chapterList = asyncChapters?.await()
        if (chapterList == null) {
            MangaRefresh(details, chapters)
        } else {
            MangaRefresh(chapterList.applyTo(details), chapterList.chapters)
        }
    }

    // Manga details related
    private suspend fun getMangaDetails(manga: SourceManga): SourceManga = withContext(Dispatchers.IO) {
        var current = manga
        coverManager.find(current.url)?.let {
            current = current.copy(thumbnailUrl = it.uri.toString())
        }

        // Augment manga details based on metadata files
        try {
            val mangaDir = fileSystem.getMangaDirectory(current.url) ?: error("${current.url} is not a valid directory")
            val mangaDirFiles = mangaDir.listFiles().orEmpty()

            val comicInfoFile = mangaDirFiles
                .firstOrNull { it.name == COMIC_INFO_FILE }
            val noXmlFile = mangaDirFiles
                .firstOrNull { it.name == ".noxml" }
            val legacyJsonDetailsFile = mangaDirFiles
                .firstOrNull { it.extension == "json" }

            when {
                // Top level ComicInfo.xml
                comicInfoFile != null -> {
                    noXmlFile?.delete()
                    current = current.copyFromComicInfo(parseComicInfo(comicInfoFile.openInputStream()))
                }

                // Old custom JSON format
                // TODO: remove support for this entirely after a while
                legacyJsonDetailsFile != null -> {
                    json.decodeFromStream<MangaDetails>(legacyJsonDetailsFile.openInputStream()).run {
                        current = current.copy(
                            title = title ?: current.title,
                            author = author ?: current.author,
                            artist = artist ?: current.artist,
                            description = description ?: current.description,
                            genres = genre ?: current.genres,
                            status = status?.let { MangaStatus.fromCode(it.toLong()) } ?: current.status,
                        )
                    }
                    // Replace with ComicInfo.xml file
                    val comicInfo = current.toComicInfo()
                    mangaDir
                        .createFile(COMIC_INFO_FILE)
                        ?.openOutputStream()
                        ?.use {
                            val comicInfoString = xml.encodeToString(ComicInfo.serializer(), comicInfo)
                            it.write(comicInfoString.toByteArray())
                            legacyJsonDetailsFile.delete()
                        }
                }

                // Copy ComicInfo.xml from chapter archive to top level if found
                noXmlFile == null -> {
                    val chapterArchives = mangaDirFiles.filter(Archive::isSupported)

                    val copiedFile = copyComicInfoFileFromChapters(chapterArchives, mangaDir)
                    if (copiedFile != null) {
                        current = current.copyFromComicInfo(parseComicInfo(copiedFile.openInputStream()))
                    } else {
                        // Avoid re-scanning
                        mangaDir.createFile(".noxml")
                    }
                }
            }
        } catch (e: Throwable) {
            logcat(LogPriority.ERROR, e) { "Error setting manga details from local metadata for ${current.title}" }
        }

        return@withContext current
    }

    private fun <T> getComicInfoForChapter(chapter: UniFile, block: (InputStream) -> T): T? {
        return if (chapter.isDirectory) {
            chapter.findFile(COMIC_INFO_FILE)?.openInputStream()?.use(block)
        } else {
            chapter.archiveReader(context).use { reader ->
                reader.getInputStream(COMIC_INFO_FILE)?.use(block)
            }
        }
    }

    private fun copyComicInfoFileFromChapters(chapterArchives: List<UniFile>, folder: UniFile): UniFile? {
        for (chapter in chapterArchives) {
            val file = getComicInfoForChapter(chapter) f@{ stream ->
                return@f copyComicInfoFile(stream, folder)
            }
            if (file != null) return file
        }
        return null
    }

    private fun copyComicInfoFile(comicInfoFileStream: InputStream, folder: UniFile): UniFile? {
        return folder.createFile(COMIC_INFO_FILE)?.apply {
            openOutputStream().use { outputStream ->
                comicInfoFileStream.use { it.copyTo(outputStream) }
            }
        }
    }

    private fun parseComicInfo(stream: InputStream): ComicInfo {
        return AndroidXmlReader(stream, StandardCharsets.UTF_8.name()).use {
            xml.decodeFromReader<ComicInfo>(it)
        }
    }

    private fun SourceChapter.withComicInfo(stream: InputStream): SourceChapter {
        val comicInfo = parseComicInfo(stream)

        val updated = copy(
            name = comicInfo.title?.value ?: name,
            chapterNumber = comicInfo.number?.value?.toDoubleOrNull() ?: chapterNumber,
            scanlator = comicInfo.translator?.value ?: scanlator,
        )

        // only bother with partial dates if the year is not null, abandon date parsing otherwise
        val year = comicInfo.year?.value?.takeIf { it > 0 }?.toString()?.padStart(4, '0') ?: return updated
        val month = (comicInfo.month?.value?.coerceIn(1, 12) ?: 1).toString().padStart(2, '0')
        val day = (comicInfo.day?.value?.coerceIn(1, 31) ?: 1).toString().padStart(2, '0')

        val dateInstant = try {
            LocalDate.parse("$year-$month-$day").atStartOfDayIn(TimeZone.currentSystemDefault())
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
            null
        }
        return dateInstant?.let { updated.copy(dateUpload = it.toEpochMilliseconds()) } ?: updated
    }

    private class ChapterList(
        val chapters: List<SourceChapter>,
        val coverUrl: String?,
        val author: String?,
        val description: String?,
    ) {

        fun applyTo(manga: SourceManga): SourceManga = manga.copy(
            author = author ?: manga.author,
            description = description ?: manga.description,
            thumbnailUrl = if (manga.thumbnailUrl.isNullOrBlank()) coverUrl ?: manga.thumbnailUrl else manga.thumbnailUrl,
        )
    }

    // Chapters
    private suspend fun getChapterList(manga: SourceManga): ChapterList = withContext(Dispatchers.IO) {
        var author: String? = null
        var description: String? = null

        val chapters = fileSystem.getFilesInMangaDirectory(manga.url)
            // Only keep supported formats
            .filterNot { it.name.orEmpty().startsWith('.') }
            .filter { it.isDirectory || Archive.isSupported(it) || it.extension.equals("epub", true) }
            .map { chapterFile ->
                val name = if (chapterFile.isDirectory) {
                    chapterFile.name
                } else {
                    chapterFile.nameWithoutExtension
                }.orEmpty()
                var chapter = SourceChapter(
                    url = "${manga.url}/${chapterFile.name}",
                    name = name,
                    dateUpload = chapterFile.lastModified(),
                    chapterNumber = ChapterRecognition.parseChapterNumber(manga.title, name, -1.0),
                )

                try {
                    val format = Format.valueOf(chapterFile)
                    if (format is Format.Epub) {
                        format.file.epubReader(context).use { epub ->
                            val metadata = epub.readMetadata()
                            metadata.author?.let { author = it }
                            metadata.description?.let { description = it }
                            chapter = chapter.copy(
                                name = metadata.chapterName ?: chapter.name,
                                scanlator = metadata.scanlator ?: chapter.scanlator,
                                dateUpload = metadata.dateUpload ?: chapter.dateUpload,
                            )
                        }
                    } else {
                        getComicInfoForChapter(chapterFile) { stream ->
                            chapter = chapter.withComicInfo(stream)
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    logcat(LogPriority.ERROR, e) { "Failed to read metadata for ${chapterFile.name}" }
                }
                chapter
            }
            .sortedWith { c1, c2 ->
                c2.name.compareToCaseInsensitiveNaturalOrder(c1.name)
            }

        // Copy the cover from the first chapter found if not available
        val coverUrl = if (manga.thumbnailUrl.isNullOrBlank()) {
            chapters.lastOrNull()?.let { chapter -> updateCover(chapter, manga) }?.uri?.toString()
        } else {
            null
        }

        ChapterList(chapters, coverUrl, author, description)
    }

    // Unused stuff
    override suspend fun pages(chapter: SourceChapter): List<Page> = throw UnsupportedOperationException("Unused")

    override suspend fun openImage(
        page: Page,
        resumeFrom: Long,
        onProgress: (bytesRead: Long, contentLength: Long) -> Unit,
    ): ImageContent = throw UnsupportedOperationException("Unused")

    fun getFormat(chapter: SourceChapter): Format {
        try {
            val (mangaDirName, chapterName) = chapter.url.split('/', limit = 2)
            return fileSystem.getBaseDirectory()
                ?.findFile(mangaDirName)
                ?.findFile(chapterName)
                ?.let(Format.Companion::valueOf)
                ?: throw Exception(context.stringResource(MR.strings.chapter_not_found))
        } catch (e: Format.UnknownFormatException) {
            throw Exception(context.stringResource(MR.strings.local_invalid_format))
        } catch (e: Exception) {
            throw e
        }
    }

    private fun updateCover(chapter: SourceChapter, manga: SourceManga): UniFile? {
        return try {
            when (val format = getFormat(chapter)) {
                is Format.Directory -> {
                    val entry = format.file.listFiles()
                        ?.sortedWith { f1, f2 ->
                            f1.name.orEmpty().compareToCaseInsensitiveNaturalOrder(
                                f2.name.orEmpty(),
                            )
                        }
                        ?.find {
                            !it.isDirectory && ImageUtil.isImage(it.name) { it.openInputStream() }
                        }

                    entry?.let { coverManager.update(manga.url, it.openInputStream()) }
                }
                is Format.Archive -> {
                    format.file.archiveReader(context).use { reader ->
                        val entry = reader.useEntries { entries ->
                            entries
                                .sortedWith { f1, f2 -> f1.name.compareToCaseInsensitiveNaturalOrder(f2.name) }
                                .find { it.isFile && ImageUtil.isImage(it.name) { reader.getInputStream(it.name)!! } }
                        }

                        entry?.let { coverManager.update(manga.url, reader.getInputStream(it.name)!!) }
                    }
                }
                is Format.Epub -> {
                    format.file.epubReader(context).use { epub ->
                        val entry = epub.getImagesFromPages().firstOrNull()

                        entry?.let { coverManager.update(manga.url, epub.getInputStream(it)!!) }
                    }
                }
            }
        } catch (e: Throwable) {
            logcat(LogPriority.ERROR, e) { "Error updating cover for ${manga.title}" }
            null
        }
    }

    private enum class Order { TITLE, DATE }

    companion object {
        const val ID = 0L

        private val LATEST_THRESHOLD = 7.days.inWholeMilliseconds
    }
}

fun Manga.isLocal(): Boolean = source == LocalSource.ID

fun ContentSource.isLocal(): Boolean = id == LocalSource.ID

fun DomainSource.isLocal(): Boolean = id == LocalSource.ID
