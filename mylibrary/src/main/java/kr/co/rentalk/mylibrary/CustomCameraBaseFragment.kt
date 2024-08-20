package kr.co.rentalk.mylibrary

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import kr.co.rentalk.mylibrary.data.DefaultPhotoData
import kr.co.rentalk.mylibrary.data.PhotoData
import kr.co.rentalk.mylibrary.ui.CameraScreen

open class CustomCameraBaseFragment : Fragment() {
    private var photoData: PhotoData? = null
    private var photoUriString: String? = null
    private var parentOrientation: Int? = null
    private var cameraOrientation: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        photoData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("photoData", PhotoData::class.java)
        } else {
            arguments?.getParcelable("photoData")
        }
        photoUriString = arguments?.getString("photoUri")
        parentOrientation = arguments?.getInt("parentOrientation")
        cameraOrientation = arguments?.getInt("cameraOrientation")
        requireActivity().requestedOrientation =
            cameraOrientation ?: ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CameraScreen(
                        context = requireActivity(),
                        photoData = photoData ?: DefaultPhotoData.leftHeadLight,
                        photoUri = photoUriString?.let { Uri.parse(photoUriString) },
                        completeAction = { bitmap ->
                            completeAction(bitmap)
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().requestedOrientation =
            parentOrientation ?: ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    open fun completeAction(bitmap: Bitmap) {}

    fun getBundleForFragment(
        photoData: PhotoData,
        photoUri: Uri?,
        parentOrientation: Int,
        cameraOrientation: Int
    ): Bundle {
        return Bundle().apply {
            putParcelable("photoData", photoData)
            putString("photoUri", photoUri.toString())
            putInt("parentOrientation", parentOrientation)
            putInt("cameraOrientation", cameraOrientation)
        }
    }
}