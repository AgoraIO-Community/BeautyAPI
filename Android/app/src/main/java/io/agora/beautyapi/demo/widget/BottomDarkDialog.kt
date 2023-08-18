package io.agora.beautyapi.demo.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.agora.beautyapi.demo.R
import io.agora.beautyapi.demo.databinding.BottomDarkDialogBinding
import io.agora.beautyapi.demo.utils.StatusBarUtil


open class BottomDarkDialog : BottomSheetDialog {
    private val mBinding by lazy { BottomDarkDialogBinding.inflate(LayoutInflater.from(context)) }

    constructor(context: Context) : this(context, R.style.bottom_dark_dialog)
    constructor(context: Context, theme: Int) : super(context, theme){
        super.setContentView(mBinding.root)
    }

    override fun onStart() {
        super.onStart()
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        StatusBarUtil.hideStatusBar(window, false)
    }


    override fun setContentView(view: View) {
        throw RuntimeException("setContentView is not allow. Please use setTopView or setBottomView")
    }

    protected fun setTopView(view: View){
        mBinding.topLayout.addView(view)
    }

    protected fun setBottomView(view: View){
        mBinding.bottomLayout.addView(view)
    }



}