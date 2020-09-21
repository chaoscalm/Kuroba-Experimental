package com.github.k1rakishou.model.source.remote

import com.github.k1rakishou.common.AppConstants
import com.github.k1rakishou.common.suspendCall
import com.github.k1rakishou.model.common.Logger
import com.github.k1rakishou.model.data.archive.ArchiveThread
import com.github.k1rakishou.model.parser.ArchivesJsonParser
import com.github.k1rakishou.model.util.ensureBackgroundThread
import com.google.gson.stream.JsonReader
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class ArchivesRemoteSource(
  okHttpClient: OkHttpClient,
  loggerTag: String,
  logger: Logger,
  private val archivesJsonParser: ArchivesJsonParser
) : AbstractRemoteSource(okHttpClient, logger) {
  private val TAG = "$loggerTag ArchivesRemoteSource"

  open suspend fun fetchThreadFromNetwork(
    threadArchiveRequestLink: String,
    threadNo: Long
  ): ArchiveThread {
    logger.log(TAG, "fetchThreadFromNetwork($threadArchiveRequestLink, $threadNo)")
    ensureBackgroundThread()

    val httpRequest = Request.Builder()
      .url(threadArchiveRequestLink)
      .get()
      // We need to have a user agent for archived.moe
      // If we won't send a valid user agent then all the "remote_media_link"s will be
      // redirect links instead of real links.
      .header("User-Agent", AppConstants.USER_AGENT)
      .build()

    val response = withTimeout(MAX_ARCHIVE_FETCH_WAIT_TIME_MS) { okHttpClient.suspendCall(httpRequest) }
    if (!response.isSuccessful) {
      throw IOException("Bad response status: ${response.code}")
    }

    val body = response.body
      ?: throw IOException("Response has no body")

    return body.byteStream().use { inputStream ->
      return@use JsonReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))
        .use { jsonReader ->
          val parsedArchivePosts = archivesJsonParser.parsePosts(jsonReader, threadNo)
          return@use ArchiveThread(parsedArchivePosts)
        }
    }
  }

  class ArchivesApiException(message: String) : Exception(message)

  companion object {
    private const val MAX_ARCHIVE_FETCH_WAIT_TIME_MS = 20_000L
  }
}