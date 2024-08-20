package kr.co.rentalk.mylibrary.ui

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.media.Image
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.Window
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraViewModel() : ViewModel() {
    var isTake: Boolean = false
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private val _bitmap = MutableStateFlow<Bitmap?>(null)
    val bitmap: StateFlow<Bitmap?> = _bitmap

    private val _shutterState = MutableStateFlow(true)
    val shutterState: StateFlow<Boolean> = _shutterState

    @SuppressLint("UnsafeOptInUsageError")
    fun setPreviewExtender(previewBuilder: Preview.Builder) {
        val previewExtender = Camera2Interop.Extender(previewBuilder).apply {
            setCaptureRequestOption(
                CaptureRequest.CONTROL_AWB_MODE,
                CaptureRequest.CONTROL_AWB_MODE_AUTO
            )
        }

        previewExtender.setSessionCaptureCallback(object : CameraCaptureSession.CaptureCallback() {
            @SuppressLint("SetTextI18n")
            override fun onCaptureCompleted(
                session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult
            ) {
                super.onCaptureCompleted(session, request, result)

                val afState = result.get(CaptureResult.CONTROL_AF_STATE)

                val value =
                    (afState == CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED || afState == CaptureRequest.CONTROL_AF_STATE_FOCUSED_LOCKED) // 초점이 맞춰진 상태

                //if (!isTake) setShutterState(value)
            }
        })
    }

    fun takePicture(context: Context, imageCapture: ImageCapture?, photoUri: Uri?) {
        if (imageCapture == null) return

        imageCapture.takePicture(
            cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                @SuppressLint("UnsafeOptInUsageError")
                override fun onCaptureSuccess(image: ImageProxy) {
                    isTake = true
                    val imgProxy = image.image ?: return
                    val imgBitmap = imgProxy.toBitmap()
                    image.close()

                    /*val rotateMatrix = Matrix()

                    val bitmap = Bitmap.createBitmap(
                        imgBitmap, 0, 0, imgBitmap.width, imgBitmap.height, rotateMatrix, true
                     )*/
                    photoUri?.let {
                        saveBitmapToGallery(it, context, imgBitmap)
                    }
                    _bitmap.value = imgBitmap
                }

                override fun onError(exc: ImageCaptureException) {
                    setShutterState(false)
                    isTake = false
                }
            })
    }

    fun setShutterState(state: Boolean) {
        _shutterState.value = state
    }

    fun resetBitmap() {
        _bitmap.value = null
    }

    fun Image.toBitmap(): Bitmap {
        val buffer = planes[0].buffer
        buffer.rewind()
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun saveBitmapToUri(context: Context, bitmap: Bitmap, photoUri: Uri): Boolean {
        var success = false
        try {
            val contentResolver: ContentResolver = context.contentResolver
            contentResolver.openOutputStream(photoUri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                success = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return success
    }

    fun saveBitmapToGallery(photoUri: Uri, context: Context, bitmap: Bitmap) {
        val success = saveBitmapToUri(context, bitmap, photoUri)
        if (success) {
            Log.d("shyTest", "Bitmap saved successfully!")
        } else {
            Log.d("shyTest", "Failed to save bitmap.")
        }
    }

    fun hideBottomSystemUI(window: Window, view: View) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, view).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}