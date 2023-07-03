package io.agora.beauty.demo

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.view.isInvisible
import io.agora.beauty.demo.databinding.MainActivityBinding

class MainActivity : ComponentActivity() {

    private val mBinding by lazy {
        MainActivityBinding.inflate(LayoutInflater.from(this))
    }
    private val mPermissionHelp = PermissionHelp(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        mBinding.etChannelName.setText((java.util.Random().nextInt(100) + 1000).toString())
        mBinding.spResolution.setSelection(2)
        mBinding.spRoleType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val isAudience =
                    mBinding.spRoleType.selectedItem.toString() == getString(R.string.audience)
                (mBinding.spResolution.parent as ViewGroup).isInvisible = isAudience
                (mBinding.spBeautyType.parent as ViewGroup).isInvisible = isAudience
                (mBinding.spFrameRate.parent as ViewGroup).isInvisible = isAudience
                (mBinding.spBeautyCaptureMode.parent as ViewGroup).isInvisible = isAudience
                (mBinding.spBeautyProcessMode.parent as ViewGroup).isInvisible = isAudience
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        mBinding.btnJoin.setOnClickListener {
            if (TextUtils.isEmpty(mBinding.etChannelName.text)) {
                Toast.makeText(this, "频道名不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            mPermissionHelp.checkCameraAndMicPerms(
                granted = {
                    gotoBeautyActivity()
                },
                unGranted = {
                    Toast.makeText(this, "没有直播权限", Toast.LENGTH_SHORT).show()
                },
                true
            )
        }
    }

    private fun gotoBeautyActivity() {
        if (mBinding.spRoleType.selectedItem.toString() == getString(R.string.broadcast)) {
            when (mBinding.spBeautyType.selectedItem as String) {
                getString(R.string.beauty_sensetime) -> SenseTimeActivity.launch(
                    this,
                    mBinding.etChannelName.text.toString(),
                    mBinding.spResolution.selectedItem.toString(),
                    mBinding.spFrameRate.selectedItem.toString(),
                    mBinding.spBeautyCaptureMode.selectedItem.toString(),
                    mBinding.spBeautyProcessMode.selectedItem.toString(),
                )

                getString(R.string.beauty_faceunity) -> FaceUnityActivity.launch(
                    this,
                    mBinding.etChannelName.text.toString(),
                    mBinding.spResolution.selectedItem.toString(),
                    mBinding.spFrameRate.selectedItem.toString(),
                    mBinding.spBeautyCaptureMode.selectedItem.toString(),
                    mBinding.spBeautyProcessMode.selectedItem.toString()
                )

                getString(R.string.beauty_bytedance) -> ByteDanceActivity.launch(
                    this,
                    mBinding.etChannelName.text.toString(),
                    mBinding.spResolution.selectedItem.toString(),
                    mBinding.spFrameRate.selectedItem.toString(),
                    mBinding.spBeautyCaptureMode.selectedItem.toString(),
                    mBinding.spBeautyProcessMode.selectedItem.toString()
                )
            }
        } else {
            AudienceActivity.launch(this, mBinding.etChannelName.text.toString())
        }

    }


}
