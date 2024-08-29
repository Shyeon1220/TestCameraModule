package kr.co.rentalk.mylibrary.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kr.co.rentalk.mylibrary.R

@Parcelize
data class PhotoData(
    val text: String,
    val index: Int,
    val guideImageRes: Int? = null
) : Parcelable

object DefaultPhotoData {
    val leftHeadLight = PhotoData("좌측\n헤드라이트", 0, R.drawable.img_left_head_light)
    val front = PhotoData("앞면", 1, R.drawable.img_front)
    val rightHeadLight = PhotoData("우측\n헤드라이트", 2, R.drawable.img_right_head_light)
    val leftFrontTire = PhotoData("좌측\n앞타이어(휠)", 3, R.drawable.img_left_front_tire)
    val frontWindShield = PhotoData("전면 유리", 4, R.drawable.img_wind_shield)
    val rightFrontTire = PhotoData("우측\n앞타이어(휠)", 5, R.drawable.img_right_front_tire)
    val leftFrontDoor = PhotoData("좌측\n앞휀더~앞문", 6, R.drawable.img_left_front_door)
    val dashBoard = PhotoData("계기판\n(유류량+주행거리)", 7, R.drawable.img_instrumnet_panel)
    val rightFrontDoor = PhotoData("우측\n앞휀더~앞문", 8, R.drawable.img_right_front_door)
    val leftBackDoor = PhotoData("좌측\n뒷문~뒷휀더", 9, R.drawable.img_left_back_door)
    val rightBackDoor = PhotoData("우측\n뒷문~뒷휀더", 10, R.drawable.img_right_back_door)
    val leftBackTire = PhotoData("좌측\n뒷타이어(휠)", 11, R.drawable.img_left_back_tire)
    val rightBackTire = PhotoData("우측\n뒷타이어(휠)", 12, R.drawable.img_right_back_tire)
    val leftRearLight = PhotoData("좌측\n리어라이트", 13, R.drawable.img_left_rear_light)
    val back = PhotoData("뒷면", 14, R.drawable.img_back)
    val rightRearLight = PhotoData("우측\n리어라이트", 15, R.drawable.img_right_rear_light)
    val additional1 = PhotoData("기타 1", 16)
    val additional2 = PhotoData("기타 2", 17)
    val additional3 = PhotoData("기타 3", 18)
    val additional4 = PhotoData("기타 4", 19)
    val additional5 = PhotoData("기타 5", 20)
}