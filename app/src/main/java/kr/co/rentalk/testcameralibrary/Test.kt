package kr.co.rentalk.testcameralibrary

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import kr.co.rentalk.mylibrary.data.DefaultPhotoData
import kr.co.rentalk.mylibrary.ui.CameraScreen

class Test : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val photoData = DefaultPhotoData.leftHeadLight
        val uri = null
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                CameraScreen(
                    context = this,
                    photoData = photoData,
                    photoUri = uri,
                    completeAction = {
                        setResult(RESULT_OK, Intent())
                        finish()
                    }
                )
            }
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
}