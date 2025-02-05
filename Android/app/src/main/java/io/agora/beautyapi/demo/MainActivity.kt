package io.agora.beautyapi.demo

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.view.isInvisible
import io.agora.beautyapi.demo.databinding.MainActivityBinding
import io.agora.beautyapi.demo.module.bytedance.ByteDanceActivity
import io.agora.beautyapi.demo.module.cosmos.CosmosActivity
import io.agora.beautyapi.demo.module.faceunity.FaceUnityActivity
import io.agora.beautyapi.demo.module.sensetime.SenseTimeActivity
import io.agora.beautyapi.demo.widget.PermissionHelp

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
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val isAudience = mBinding.spRoleType.selectedItem.toString() == getString(R.string.audience)
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
                Toast.makeText(this, R.string.beauty_channel_name_cannot_be_empty, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            mPermissionHelp.checkCameraAndMicPerms(
                granted = {
                    gotoBeautyActivity()
                },
                unGranted = {
                    Toast.makeText(this, R.string.beauty_no_permission_for_live_streaming, Toast.LENGTH_SHORT).show()
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
                    mBinding.swFence.isChecked
                )

                getString(R.string.beauty_faceunity) -> FaceUnityActivity.launch(
                    this,
                    mBinding.etChannelName.text.toString(),
                    mBinding.spResolution.selectedItem.toString(),
                    mBinding.spFrameRate.selectedItem.toString(),
                    mBinding.spBeautyCaptureMode.selectedItem.toString(),
                    mBinding.spBeautyProcessMode.selectedItem.toString(),
                    mBinding.swFence.isChecked
                )

                getString(R.string.beauty_bytedance) -> ByteDanceActivity.launch(
                    this,
                    mBinding.etChannelName.text.toString(),
                    mBinding.spResolution.selectedItem.toString(),
                    mBinding.spFrameRate.selectedItem.toString(),
                    mBinding.spBeautyCaptureMode.selectedItem.toString(),
                    mBinding.spBeautyProcessMode.selectedItem.toString(),
                    mBinding.swFence.isChecked
                )

                getString(R.string.beauty_cosmos) -> CosmosActivity.launch(
                    this,
                    mBinding.etChannelName.text.toString(),
                    mBinding.spResolution.selectedItem.toString(),
                    mBinding.spFrameRate.selectedItem.toString(),
                    mBinding.spBeautyCaptureMode.selectedItem.toString(),
                    mBinding.spBeautyProcessMode.selectedItem.toString(),
                    mBinding.swFence.isChecked
                )
            }
        } else {
            AudienceActivity.launch(this, mBinding.etChannelName.text.toString())
        }
    }
}