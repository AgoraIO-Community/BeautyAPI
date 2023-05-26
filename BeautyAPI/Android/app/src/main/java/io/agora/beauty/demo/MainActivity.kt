package io.agora.beauty.demo

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.ComponentActivity
import io.agora.beauty.demo.databinding.MainActivityBinding

class MainActivity : ComponentActivity() {

    private val mBinding by lazy {
        MainActivityBinding.inflate(LayoutInflater.from(this))
    }
    private val mPermissionHelp = PermissionHelp(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        mBinding.btnJoin.setOnClickListener {
            if(TextUtils.isEmpty(mBinding.etChannelName.text)){
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
        if (mBinding.spBeautyType.selectedItem as String == getString(R.string.beauty_sensetime)) {
            SenseTimeActivity.launch(this, mBinding.etChannelName.text.toString())
        }
    }


}
