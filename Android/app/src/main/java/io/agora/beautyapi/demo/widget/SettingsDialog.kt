package io.agora.beautyapi.demo.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.agora.beautyapi.demo.databinding.SettingDialogBinding

class SettingsDialog(private val context: Context) {

    private val mBinding by lazy {
        SettingDialogBinding.inflate(LayoutInflater.from(context))
    }

    private val bottomSheetDialog by lazy {
        BottomSheetDialog(context).apply {
            setContentView(mBinding.root)
        }
    }

    fun setBeautyEnable(enable: Boolean) {
        mBinding.swBeauty.isChecked = enable
    }

    fun setOnBeautyChangeListener(onChanged: (enable: Boolean) -> Unit) {
        mBinding.swBeauty.setOnCheckedChangeListener { _, isChecked ->
            onChanged.invoke(isChecked)
        }
    }

    fun setOnColorEnhanceChangeListener(onChanged: (enable: Boolean) -> Unit) {
        mBinding.swColorEnhance.setOnCheckedChangeListener { _, isChecked ->
            onChanged.invoke(isChecked)
        }
    }

    fun setOnI420ChangeListener(onChanged: (enable: Boolean) -> Unit) {
        mBinding.swI420.setOnCheckedChangeListener { _, isChecked ->
            onChanged.invoke(isChecked)
        }
    }

    fun setResolutionSelect(resolution: String) {
        for (i in 0 until mBinding.spResolution.count) {
            if (mBinding.spResolution.getItemAtPosition(i).equals(resolution)) {
                mBinding.spResolution.setSelection(i)
                break
            }
        }
    }

    fun setOnResolutionChangeListener(onChanged: (resolution: String) -> Unit) {
        mBinding.spResolution.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                parent?.getItemAtPosition(position)?.toString()?.let(onChanged)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    fun setFrameRateSelect(frameRate: String) {
        for (i in 0 until mBinding.spFrameRate.count) {
            if (mBinding.spFrameRate.getItemAtPosition(i).equals(frameRate)) {
                mBinding.spFrameRate.setSelection(i)
                break
            }
        }
    }

    fun setOnFrameRateChangeListener(onChanged: (frameRate: String) -> Unit) {
        mBinding.spFrameRate.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                parent?.getItemAtPosition(position)?.toString()?.let(onChanged)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    fun show() {
        bottomSheetDialog.show()
    }
}