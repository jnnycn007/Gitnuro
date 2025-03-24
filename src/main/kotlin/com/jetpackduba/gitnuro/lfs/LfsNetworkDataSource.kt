package com.jetpackduba.gitnuro.lfs

import com.jetpackduba.gitnuro.NetworkConstants
import com.jetpackduba.gitnuro.Result
import com.jetpackduba.gitnuro.models.lfs.LfsObjectBatch
import com.jetpackduba.gitnuro.models.lfs.LfsObjects
import com.jetpackduba.gitnuro.models.lfs.LfsPrepareUploadObjectBatch
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.readByteArray
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Path
import javax.inject.Inject

interface ILfsNetworkDataSource {
    suspend fun postBatchObjects(
        remoteUrl: String,
        lfsPrepareUploadObjectBatch: LfsPrepareUploadObjectBatch,
        objHeaders: Map<String, String>,
        username: String?,
        password: String?,
    ): Result<LfsObjects, LfsError>

    suspend fun uploadObject(
        uploadUrl: String,
        oid: String,
        file: Path,
        size: Long,
        objHeaders: Map<String, String>,
        username: String?,
        password: String?,
    ): Result<Unit, LfsError>

    suspend fun verify(
        url: String,
        oid: String,
        size: Long,
        headers: Map<String, String>,
        username: String?,
        password: String?,
    ): Result<Unit, LfsError>

    suspend fun downloadObject(
        downloadUrl: String,
        outPath: Path,
        headers: Map<String, String>,
        username: String?,
        password: String?,
    ): Result<Unit, LfsError>
}

class LfsNetworkDataSource @Inject constructor(
    private val client: HttpClient,
) : ILfsNetworkDataSource {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun postBatchObjects(
        remoteUrl: String,
        lfsPrepareUploadObjectBatch: LfsPrepareUploadObjectBatch,
        objHeaders: Map<String, String>,
        username: String?,
        password: String?,
    ): Result<LfsObjects, LfsError> {
        val response = client.post("${remoteUrl.removeSuffix("/")}/objects/batch") {
            for (header in objHeaders) {
                header(header.key, header.value)
            }

            this.headers {
                if (username != null && password != null && !headers.contains(NetworkConstants.AUTH_HEADER)) {
                    basicAuth(username, password)
                }

                if (!this.contains(NetworkConstants.ACCEPT_HEADER)) {
                    this[NetworkConstants.ACCEPT_HEADER] = "application/vnd.git-lfs"
                }
            }

            this.headers {
                contentType(ContentType("application", "vnd.git-lfs+json"))
            }

            setBody(json.encodeToString(lfsPrepareUploadObjectBatch))
        }

        if (response.status != HttpStatusCode.OK) {
            return Result.Err(LfsError.HttpError(response.status))
        }

        return Result.Ok(json.decodeFromString(response.bodyAsText()))
    }

    override suspend fun uploadObject(
        uploadUrl: String,
        oid: String,
        file: Path,
        size: Long,
        objHeaders: Map<String, String>,
        username: String?,
        password: String?,
    ): Result<Unit, LfsError> {
        val response = client.put(uploadUrl) {
            for (header in objHeaders) {
                header(header.key, header.value)
            }

            this.headers {
                if (username != null && password != null && !headers.contains(NetworkConstants.AUTH_HEADER)) {
                    basicAuth(username, password)
                }

                if (!this.contains(NetworkConstants.ACCEPT_HEADER)) {
                    this["Accept"] = "application/vnd.git-lfs"
                }

                this["Content-Length"] = size.toString()
            }

            setBody(file.readChannel())
        }

        if (response.status != HttpStatusCode.OK) {
            return Result.Err(LfsError.HttpError(response.status))
        }

        return Result.Ok(Unit)
    }

    override suspend fun verify(
        url: String,
        oid: String,
        size: Long,
        headers: Map<String, String>,
        username: String?,
        password: String?,
    ): Result<Unit, LfsError> {

        val response = client.post(url) {
            for (header in headers) {
                header(header.key, header.value)
            }

            this.headers {
                if (
                    !headers.contains(NetworkConstants.AUTH_HEADER) &&
                    (username != null && password != null)
                ) {
                    basicAuth(username, password)
                }

                if (!this.contains(NetworkConstants.ACCEPT_HEADER)) {
                    this[NetworkConstants.ACCEPT_HEADER] = "application/vnd.git-lfs"
                }
            }

            val body = LfsObjectBatch(oid, size)
            setBody(json.encodeToString(body))
        }

        if (response.status != HttpStatusCode.OK) {
            return Result.Err(LfsError.HttpError(response.status))
        }

        return Result.Ok(Unit)
    }

    override suspend fun downloadObject(
        downloadUrl: String,
        outPath: Path,
        headers: Map<String, String>,
        username: String?,
        password: String?,
    ): Result<Unit, LfsError> {
        val response = client.get(downloadUrl) {
            for (header in headers) {
                header(header.key, header.value)
            }

            this.headers {
                if (username != null && password != null && !headers.contains(NetworkConstants.AUTH_HEADER)) {
                    basicAuth(username, password)
                }

                if (!this.contains(NetworkConstants.ACCEPT_HEADER)) {
                    this[NetworkConstants.ACCEPT_HEADER] = "application/vnd.git-lfs"
                }
            }
        }

        if (response.status != HttpStatusCode.OK) {
            return Result.Err(LfsError.HttpError(response.status))
        }

        val channel: ByteReadChannel = response.body()
        val file = outPath.toFile()

        withContext(Dispatchers.IO) {
            file.parentFile?.mkdirs()
            file.createNewFile()
        }

        while (!channel.isClosedForRead) {
            val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
            while (!packet.exhausted()) {
                val bytes = packet.readByteArray()
                file.appendBytes(bytes)
            }
        }

        return Result.Ok(Unit)
    }
}