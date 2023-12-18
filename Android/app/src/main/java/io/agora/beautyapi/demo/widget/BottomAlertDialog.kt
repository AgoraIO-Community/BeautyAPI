package io.agora.beautyapi.demo.widget

import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.agora.beautyapi.demo.R
import io.agora.beautyapi.demo.utils.StatusBarUtil


open class BottomAlertDialog : BottomSheetDialog {

    constructor(context: Context) : this(context, R.style.bottom_dark_dialog)
    constructor(context: Context, theme: Int) : super(context, theme)

    override fun onStart() {
        super.onStart()
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        StatusBarUtil.hideStatusBar(window, false)
    }

}