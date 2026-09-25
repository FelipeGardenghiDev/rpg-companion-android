package com.grimorio.rpg.data.qr

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer

/**
 * Analisador de frames em tempo real para detecção de QR Code via CameraX e ZXing.
 */
class QrCodeAnalyzer(
    private val onQrCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader()
    private var isScanned = false

    override fun analyze(image: ImageProxy) {
        if (isScanned) {
            image.close()
            return
        }

        val buffer = image.planes[0].buffer
        val data = ByteArray(buffer.remaining())
        buffer.get(data)

        val width = image.width
        val height = image.height

        val source = PlanarYUVLuminanceSource(
            data,
            width,
            height,
            0,
            0,
            width,
            height,
            false
        )

        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

        try {
            val result = reader.decodeWithState(binaryBitmap)
            isScanned = true
            onQrCodeScanned(result.text)
        } catch (_: Exception) {
            // Nenhum QR code decodificado neste quadro
        } finally {
            image.close()
        }
    }
}
