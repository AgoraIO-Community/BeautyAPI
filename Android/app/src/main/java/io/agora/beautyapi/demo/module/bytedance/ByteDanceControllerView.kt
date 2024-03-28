package io.agora.beautyapi.demo.module.bytedance

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.view.isVisible
import io.agora.beautyapi.demo.R
import io.agora.beautyapi.demo.widget.BeautyControllerView

class ByteDanceControllerView : BeautyControllerView {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onPageListCreate(): List<PageInfo> {
        val beautyConfig = ByteDanceBeautySDK.beautyConfig
        return listOf(
            PageInfo(
                R.string.beauty_group_beauty,
                listOf(
                    ItemInfo(
                        R.string.beauty_item_none,
                        R.mipmap.ic_beauty_none,
                        onValueChanged = { _ ->
                            beautyConfig.smooth = 0.0f
                            beautyConfig.whiten = 0.0f
                            beautyConfig.thinFace = 0.0f
                            beautyConfig.enlargeEye = 0.0f
                            beautyConfig.redden = 0.0f
                            beautyConfig.shrinkCheekbone = 0.0f
                            beautyConfig.shrinkJawbone = 0.0f
                            beautyConfig.whiteTeeth = 0.0f
                            beautyConfig.hairlineHeight = 0.0f
                            beautyConfig.narrowNose = 0.0f
                            beautyConfig.mouthSize = 0.0f
                            beautyConfig.chinLength = 0.0f
                            beautyConfig.brightEye = 0.0f
                            beautyConfig.darkCircles = 0.0f
                            beautyConfig.nasolabialFolds = 0.0f
                        }
                    ),
                    ItemInfo(
                        R.string.beauty_item_beauty_smooth,
                        R.mipmap.ic_beauty_face_mopi,
                        beautyConfig.smooth,
                        isSelected = true,
                        onValueChanged = { value ->
                            beautyConfig.smooth = value
                        }
                    ),
                    ItemInfo(
                        R.string.beauty_item_beauty_whiten,
                        R.mipmap.ic_beauty_face_meibai,
                        beautyConfig.whiten,
                        onValueChanged = { value ->
                            beautyConfig.whiten = value
                        }
                    ), ItemInfo(
                        R.string.beauty_item_beauty_overall,
                        R.mipmap.ic_beauty_face_shoulian,
                        beautyConfig.thinFace,
                        onValueChanged = { value ->
                            beautyConfig.thinFace = value
                        }
                    ), ItemInfo(
                        R.string.beauty_item_beauty_cheekbone,
                        R.mipmap.ic_beauty_face_shouquangu,
                        beautyConfig.shrinkCheekbone,
                        onValueChanged = { value ->
                            beautyConfig.shrinkCheekbone = value
                        }
                    ), ItemInfo(
                        R.string.beauty_item_beauty_eye,
                        R.mipmap.ic_beauty_face_eye,
                        beautyConfig.enlargeEye,
                        onValueChanged = { value ->
                            beautyConfig.enlargeEye = value
                        }
                    ), ItemInfo(
                        R.string.beauty_item_beauty_nose,
                        R.mipmap.ic_beauty_face_shoubi,
                        beautyConfig.narrowNose,
                        onValueChanged = { value ->
                            beautyConfig.narrowNose = value
                        }
                    ), ItemInfo(
                        R.string.beauty_item_beauty_chin,
                        R.mipmap.ic_beauty_face_xiaba,
                        beautyConfig.chinLength,
                        onValueChanged = { value ->
                            beautyConfig.chinLength = value
                        }
                    ), ItemInfo(
                        R.string.beauty_item_beauty_jawbone,
                        R.mipmap.ic_beauty_face_xiahegu,
                        beautyConfig.shrinkJawbone,
                        onValueChanged = { value ->
                            beautyConfig.shrinkJawbone = value
                        }
                    ), ItemInfo(
                        R.string.beauty_item_beauty_forehead,
                        R.mipmap.ic_beauty_face_etou,
                        beautyConfig.hairlineHeight,
                        onValueChanged = { value ->
                            beautyConfig.hairlineHeight = value
                        }
                    ), ItemInfo(
                        R.string.beauty_item_beauty_mouth,
                        R.mipmap.ic_beauty_face_zuixing,
                        beautyConfig.mouthSize,
                        onValueChanged = { value ->
                            beautyConfig.mouthSize = value
                        }
                    ), ItemInfo(
                        R.string.beauty_item_beauty_teeth,
                        R.mipmap.ic_beauty_face_meiya,
                        beautyConfig.whiteTeeth,
                        onValueChanged = { value ->
                            beautyConfig.whiteTeeth = value
                        }
                    ), ItemInfo(
                        R.string.beauty_item_beauty_bright_eye,
                        R.mipmap.ic_beauty_face_bright_eye,
                        beautyConfig.brightEye,
                        onValueChanged = { value ->
                            beautyConfig.brightEye = value
                        }
                    ), ItemInfo(
                        R.string.beauty_item_beauty_remove_nasolabial_folds,
                        R.mipmap.ic_beauty_face_remove_nasolabial_folds,
                        beautyConfig.nasolabialFolds,
                        onValueChanged = { value ->
                            beautyConfig.nasolabialFolds = value
                        }
                    ), ItemInfo(
                        R.string.beauty_item_beauty_remove_dark_circles,
                        R.mipmap.ic_beauty_face_remove_dark_circles,
                        beautyConfig.darkCircles,
                        onValueChanged = { value ->
                            beautyConfig.darkCircles = value
                        }
                    )
                )
            ),
            PageInfo(
                R.string.beauty_group_effect,
                listOf(
                    ItemInfo(
                        R.string.beauty_item_none,
                        R.mipmap.ic_beauty_none,
                        isSelected = beautyConfig.makeUp == null,
                        onValueChanged = { _ ->
                            beautyConfig.makeUp = null
                        }
                    ),
                    ItemInfo(
                        R.string.beauty_item_effect_cwei,
                        R.mipmap.ic_beauty_effect_cwei,
                        withPadding = false,
                        isSelected = beautyConfig.makeUp?.style == "cwei",
                        value = if (beautyConfig.makeUp?.style == "cwei") beautyConfig.makeUp?.identity
                            ?: 0.8f else 0.8f,
                        onValueChanged = { value ->
                            beautyConfig.makeUp = ByteDanceBeautySDK.MakeUpItem(
                                context,
                                "cwei",
                                value
                            )
                        }
                    ),
                    ItemInfo(
                        R.string.beauty_item_effect_yuanqi,
                        R.mipmap.ic_beauty_effect_yuanqi,
                        withPadding = false,
                        isSelected = beautyConfig.makeUp?.style == "yuanqi",
                        value = if (beautyConfig.makeUp?.style == "yuanqi") beautyConfig.makeUp?.identity
                            ?: 0.8f else 0.8f,
                        onValueChanged = { value ->
                            beautyConfig.makeUp = ByteDanceBeautySDK.MakeUpItem(
                                context,
                                "yuanqi",
                                value
                            )
                        }
                    )
                )
            ),
            PageInfo(
                R.string.beauty_group_adjust,
                listOf(
                    ItemInfo(
                        R.string.beauty_item_none,
                        R.mipmap.ic_beauty_none,
                        0.0f,
                        isSelected = true,
                        onValueChanged = { _ ->
                            beautyConfig.sharpen = 0.0f
                            beautyConfig.clear = 0.0f
                        },
                    ),
                    ItemInfo(
                        R.string.beauty_item_adjust_sharpen,
                        R.mipmap.ic_beauty_adjust_sharp,
                        beautyConfig.sharpen,
                        onValueChanged = { value ->
                            beautyConfig.sharpen = value
                        }
                    ),
                    ItemInfo(
                        R.string.beauty_item_adjust_clarity,
                        R.mipmap.ic_beauty_adjust_clear,
                        beautyConfig.clear,
                        onValueChanged = { value ->
                            beautyConfig.clear = value
                        }
                    ),
                )
            ),
            PageInfo(
                R.string.beauty_group_sticker,
                listOf(
                    ItemInfo(
                        R.string.beauty_item_none,
                        R.mipmap.ic_beauty_none,
                        isSelected = beautyConfig.sticker == null,
                        onValueChanged = { _ ->
                            beautyConfig.sticker = null
                        }
                    ),
                    ItemInfo(
                        R.string.beauty_item_sticker_huahua,
                        R.mipmap.ic_beauty_sticker_huahua,
                        isSelected = beautyConfig.sticker == "zhaocaimao",
                        onValueChanged = { _ ->
                            beautyConfig.sticker = "zhaocaimao"
                        }
                    )
                )
            )
        )
    }

    override fun onSelectedChanged(pageIndex: Int, itemIndex: Int) {
        super.onSelectedChanged(pageIndex, itemIndex)
        val pageInfo = pageList[pageIndex]
        val itemInfo = pageInfo.itemList[itemIndex]
        if (itemInfo.name == R.string.beauty_item_none
            || pageInfo.name == R.string.beauty_group_sticker
        ) {
            viewBinding.slider.visibility = View.INVISIBLE
            viewBinding.ivCompare.isVisible = false
        } else if (pageInfo.name == R.string.beauty_group_beauty
            || pageInfo.name == R.string.beauty_group_effect
            || pageInfo.name == R.string.beauty_group_adjust
        ) {
            viewBinding.slider.visibility = View.VISIBLE
            viewBinding.ivCompare.isVisible = true
        }
    }
}