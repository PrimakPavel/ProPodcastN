package com.propodcast.app.core.download

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.propodcast.app.core.database.ProPodcastDatabase
import com.propodcast.app.core.database.entity.toDownloadEntity
import com.propodcast.app.core.model.DownloadStatus
import com.propodcast.app.core.testing.testEpisode
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Robolectric (SDK 34 sandbox on Java 17), real Room in memory, MockWebServer as the audio host. */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class EpisodeDownloadWorkerTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val server = MockWebServer()
    private lateinit var db: ProPodcastDatabase
    private lateinit var files: EpisodeFileStore
    private val audio = "ID3-fake-mp3-".repeat(10_000)

    @Before
    fun setUp() {
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder().setExecutor(SynchronousExecutor()).build(),
        )
        db = Room.inMemoryDatabaseBuilder(context, ProPodcastDatabase::class.java).allowMainThreadQueries().build()
        files = EpisodeFileStore(context)
        server.start()
    }

    @After
    fun tearDown() {
        db.close()
        server.close()
        files.delete("e1")
    }

    private val dao get() = db.downloadedEpisodeDao()

    private suspend fun queue(url: String = server.url("/e1.mp3").toString()) {
        dao.upsert(testEpisode("e1").copy(audioUrl = url).toDownloadEntity(addedAt = 0)!!)
    }

    private fun worker(runAttemptCount: Int = 0) = TestListenableWorkerBuilder<EpisodeDownloadWorker>(context)
        .setInputData(EpisodeDownloadWorker.request("e1").workSpec.input)
        .setRunAttemptCount(runAttemptCount)
        .setWorkerFactory(
            object : WorkerFactory() {
                override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters) =
                    EpisodeDownloadWorker(appContext, workerParameters, dao, files, DownloadNotifications(appContext), OkHttpClient())
            },
        )
        .build()

    @Test
    fun success_writesTheFile_andMarksItDownloaded() = runTest {
        server.enqueue(MockResponse.Builder().body(audio).build())
        queue()

        val result = worker().doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertEquals(audio, files.file("e1").readText())
        assertFalse("no partial file is left", files.partFile("e1").exists())
        val row = dao.get("e1")!!
        assertEquals(DownloadStatus.DOWNLOADED, row.status)
        assertEquals(audio.length.toLong(), row.sizeBytes)
    }

    @Test
    fun serverError_isRetried_thenFails() = runTest {
        server.enqueue(MockResponse.Builder().code(500).build())
        server.enqueue(MockResponse.Builder().code(500).build())
        queue()

        assertEquals(ListenableWorker.Result.retry(), worker(runAttemptCount = 0).doWork())
        assertEquals(DownloadStatus.QUEUED, dao.get("e1")!!.status)

        assertEquals(ListenableWorker.Result.failure(), worker(runAttemptCount = 2).doWork())
        assertEquals(DownloadStatus.FAILED, dao.get("e1")!!.status)
        assertFalse(files.file("e1").exists())
        assertFalse(files.partFile("e1").exists())
    }

    @Test
    fun cancelledBeforeStart_doesNothing() = runTest {
        // No row: the user cancelled while it was still queued.
        assertEquals(ListenableWorker.Result.failure(), worker().doWork())
        assertEquals(0, server.requestCount)
    }
}
