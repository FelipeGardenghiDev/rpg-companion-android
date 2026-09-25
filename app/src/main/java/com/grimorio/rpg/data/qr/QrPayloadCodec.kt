package com.grimorio.rpg.data.qr

import com.grimorio.rpg.domain.model.ShareablePayload
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * Utilitário de codificação e compressão de dados para transporte ultracompacto via QR Code.
 */
object QrPayloadCodec {

    private const val PREFIX_COMPRESSED = "GRIMORIO_GZ:"
    private const val PREFIX_PLAIN = "GRIMORIO:"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(payload: ShareablePayload, compress: Boolean = true): String {
        val rawJson = json.encodeToString(payload)
        return if (compress) {
            val compressedBytes = compressGzip(rawJson.toByteArray(Charsets.UTF_8))
            val base64 = Base64.getEncoder().encodeToString(compressedBytes)
            "$PREFIX_COMPRESSED$base64"
        } else {
            "$PREFIX_PLAIN$rawJson"
        }
    }

    fun decode(rawString: String): Result<ShareablePayload> {
        return runCatching {
            val trimmed = rawString.trim()
            val jsonString = when {
                trimmed.startsWith(PREFIX_COMPRESSED) -> {
                    val base64Part = trimmed.removePrefix(PREFIX_COMPRESSED)
                    val compressedBytes = Base64.getDecoder().decode(base64Part)
                    val decompressedBytes = decompressGzip(compressedBytes)
                    String(decompressedBytes, Charsets.UTF_8)
                }
                trimmed.startsWith(PREFIX_PLAIN) -> {
                    trimmed.removePrefix(PREFIX_PLAIN)
                }
                trimmed.startsWith("{") -> {
                    trimmed
                }
                else -> throw IllegalArgumentException("Formato de payload não reconhecido pelo Grimório RPG")
            }

            json.decodeFromString<ShareablePayload>(jsonString)
        }
    }

    private fun compressGzip(data: ByteArray): ByteArray {
        val byteStream = ByteArrayOutputStream()
        GZIPOutputStream(byteStream).use { gzip ->
            gzip.write(data)
        }
        return byteStream.toByteArray()
    }

    private fun decompressGzip(compressed: ByteArray): ByteArray {
        val byteStream = ByteArrayInputStream(compressed)
        GZIPInputStream(byteStream).use { gzip ->
            return gzip.readBytes()
        }
    }
}
