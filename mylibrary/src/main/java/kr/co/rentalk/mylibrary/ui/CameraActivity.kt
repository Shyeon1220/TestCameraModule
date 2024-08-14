package kr.co.rentalk.mylibrary.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import kr.co.rentalk.mylibrary.data.DefaultPhotoData
import kr.co.rentalk.mylibrary.data.PhotoData

class CameraActivity : ComponentActivity() {
    val viewModel: CameraViewModel = CameraViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.hideBottomSystemUI(window, window.decorView)

        val photoData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("photoData", PhotoData::class.java)
        } else {
            intent.getParcelableExtra("photoData") as? PhotoData
        }
        val photoUriString = intent.getStringExtra("photoUri")
        val uri = photoUriString?.let { Uri.parse(photoUriString) }

        setContent {
            CameraScreen(
                context = this,
                viewModel = viewModel,
                photoData = photoData ?: DefaultPhotoData.leftHeadLight,
                photoUri = uri,
                finishActivity = { finish() },
                completeAction = {
                    setResult(RESULT_OK, Intent())
                    finish()
                }
            )
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
}