package kr.co.rentalk.mylibrary.ui

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.co.rentalk.mylibrary.ui.value.CameraColor
import kr.co.rentalk.mylibrary.R
import kr.co.rentalk.mylibrary.data.PhotoData
import java.util.concurrent.TimeUnit

@Composable
fun CameraScreen(context: Context, viewModel: CameraViewModel, photoData: PhotoData, photoUri: Uri?, finishActivity: () -> Unit, completeAction: (bitmap: Bitmap) -> Unit) {
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { mutableStateOf(ImageCapture.Builder().build()) }
    val cameraInfo = remember { mutableStateOf<CameraInfo?>(null) }
    val cameraControl = remember { mutableStateOf<CameraControl?>(null) }
    val bitmap by viewModel.bitmap.collectAsStateWithLifecycle()
    val shutterState by viewModel.shutterState.collectAsStateWithLifecycle()

    LaunchedEffect(bitmap) {
        if (bitmap == null) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener(
                {
                    cameraProvider = cameraProviderFuture.get()
                    val resolutionSelector = ResolutionSelector.Builder().setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY).build()
                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()
                    val previewBuilder = Preview.Builder()
                    //viewModel.setPreviewExtender(previewBuilder)
                    val preview = previewBuilder
                        .setResolutionSelector(resolutionSelector)
                        .build().apply {
                            setSurfaceProvider(previewView.surfaceProvider)
                        }
                    imageCapture.value = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .setResolutionSelector(resolutionSelector)
                        .build()

                    cameraProvider?.unbindAll()
                    val camera = cameraProvider?.bindToLifecycle(
                        (context as CameraActivity), cameraSelector, preview, imageCapture.value
                    )
                    camera?.let {
                        cameraInfo.value = it.cameraInfo
                        cameraControl.value = it.cameraControl
                    }
                },
                ContextCompat.getMainExecutor(context)
            )
        }
    }
    CameraScreenContents(
        previewView,
        cameraInfo = cameraInfo.value,
        cameraControl = cameraControl.value,
        bitmap = bitmap,
        photoPositionData = photoData,
        shutterState = shutterState,
        resetBitmap = { viewModel.resetBitmap() },
        takePicture = { viewModel.takePicture(context, imageCapture = imageCapture.value, photoUri) },
        finishActivity = finishActivity,
        completeAction = { takePicture -> completeAction(takePicture) }
    )
}

@Composable
fun CameraScreenContents(
    previewView: PreviewView,
    cameraInfo: CameraInfo?,
    cameraControl: CameraControl?,
    bitmap: Bitmap? = null,
    photoPositionData: PhotoData,
    shutterState: Boolean,
    resetBitmap: () -> Unit,
    takePicture: () -> Unit,
    finishActivity: () -> Unit,
    completeAction: (bitmap: Bitmap) -> Unit
) {
    bitmap?.let {
        CheckPicture(it, resetBitmap) { takePicture -> completeAction(takePicture) }
    } ?: TakePicture(previewView, cameraInfo, cameraControl, photoPositionData, shutterState, finishActivity, takePicture)
}

@Composable
private fun CheckPicture(bitmap: Bitmap, resetBitmap: () -> Unit, completeAction: (bitmap: Bitmap) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Image(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "",
            contentScale = ContentScale.FillHeight
        )
        CheckButtonContainer(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            resetBitmap = resetBitmap,
            completeAction = { completeAction(it) },
            bitmap = bitmap
        )
    }
}

@Composable
private fun TakePicture(
    previewView: PreviewView, cameraInfo: CameraInfo?,
    cameraControl: CameraControl?, photoPositionData: PhotoData, shutterState: Boolean, finishActivity: () -> Unit, takePicture: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        IconButton(
            modifier = Modifier
                .size(56.dp)
                .padding(10.dp),
            onClick = { finishActivity() }
        ) {
            Icon(
                Icons.Filled.ArrowBackIosNew,
                contentDescription = null,
                tint = Color.White
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        CameraPreview(
            Modifier.weight(7f), previewView, photoPositionData, shutterState, cameraInfo, cameraControl
        )
        CameraPreviewButtonContainer(
            Modifier
                .fillMaxHeight()
                .weight(2f), shutterState, takePicture
        )
    }
}

@Composable
private fun CheckButtonContainer(modifier: Modifier, resetBitmap: () -> Unit, completeAction: (bitmap: Bitmap) -> Unit, bitmap: Bitmap) {
    Row(
        modifier = modifier
            .background(CameraColor.DarkGray40)
            .padding(horizontal = 30.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            modifier = Modifier.wrapContentWidth(),
            onClick = { resetBitmap() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
        ) {
            Text(
                fontSize = 18.sp,
                text = "다시시도"
            )
        }
        Button(
            modifier = Modifier.wrapContentWidth(),
            onClick = {
                completeAction(bitmap)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
        ) {
            Text(
                fontSize = 18.sp,
                text = "완료"
            )
        }
    }
}

@Composable
private fun CameraPreview(
    modifier: Modifier, previewView: PreviewView, photoPositionData: PhotoData, shutterState: Boolean,
    cameraInfo: CameraInfo?,
    cameraControl: CameraControl?,
) {
    var currentZoomRatio by remember { mutableFloatStateOf(1f) }
    var focusPosition by remember { mutableStateOf<Offset?>(null) }
    var isFocusActive by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var focusJob: Job? = null

    Box(modifier = modifier
        .pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, _ ->
                currentZoomRatio = (currentZoomRatio * zoom).coerceIn(1f, cameraInfo?.zoomState?.value?.maxZoomRatio ?: 1f)
                cameraControl?.setZoomRatio(currentZoomRatio)
            }
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = { offset ->
                    focusPosition = offset
                    isFocusActive = true
                    focusJob?.cancel()

                    val factory = previewView.meteringPointFactory
                    val point = factory.createPoint(offset.x, offset.y)
                    val action = FocusMeteringAction
                        .Builder(point, FocusMeteringAction.FLAG_AF)
                        .setAutoCancelDuration(3, TimeUnit.SECONDS)
                        .build()
                    cameraControl?.startFocusAndMetering(action)

                    focusJob = coroutineScope.launch {
                        delay(1000)
                        isFocusActive = false
                    }
                }
            )
        }
    ) {
        if (cameraControl != null && cameraInfo != null) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )
        }
        photoPositionData.guideImageRes?.let { imageRes ->
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .align(Alignment.Center),
                painter = painterResource(id = imageRes),
                contentDescription = "",
                contentScale = ContentScale.FillHeight
            )
        }
        /*if (!shutterState) {
            Text(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.White),
                text = "카메라 조정 중"
            )
        }*/

        if (isFocusActive && focusPosition != null) {
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = focusPosition!!.x.toInt() - 96,
                            y = focusPosition!!.y.toInt() - 96
                        )
                    }
                    .size(64.dp)
                    .border(2.dp, Color.White, CircleShape)
                    .background(Color.Transparent, CircleShape)
                    .align(Alignment.TopStart)
            )
        }
    }
}

@Composable
private fun CameraPreviewButtonContainer(modifier: Modifier, shutterState: Boolean, takePicture: () -> Unit) {
    Box(
        modifier = modifier
    ) {
        //val painter = if (shutterState) painterResource(id = R.drawable.ic_shutter_normal) else painterResource(id = R.drawable.ic_shutter_not_enable)
        val painter = painterResource(id = R.drawable.ic_shutter_normal)
        Image(
            modifier = Modifier
                .align(Alignment.Center)
                .size(100.dp)
                .padding(16.dp)
                .clickable {
                    takePicture()
                },
            painter = painter,
            contentDescription = null
        )
    }
}