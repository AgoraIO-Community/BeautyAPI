/*
 * MIT License
 *
 * Copyright (c) 2023 Agora Community
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.agora.beautyapi.demo.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import io.agora.beautyapi.demo.databinding.BeautyDialogBottomBinding
import io.agora.beautyapi.demo.databinding.BeautyDialogItemBinding
import io.agora.beautyapi.demo.databinding.BeautyDialogPageBinding
import io.agora.beautyapi.demo.databinding.BeautyDialogTopBinding

class BeautyDialog constructor(context: Context) : BottomDarkDialog(context) {

    data class ItemInfo(
        @StringRes val name: Int,
        @DrawableRes val icon: Int,
        var value: Float,
        val onValueChanged: (BeautyDialog, Float) -> Unit
    )

    data class GroupInfo(
        @StringRes val name: Int,
        var selectedIndex: Int = 0,
        val itemList: List<ItemInfo>,
    )

    var groupList: List<GroupInfo> = mutableListOf()
        set(value) {
            field = value
            mBottomBinding.tabLayout.removeAllTabs()
            value.forEach {groupInfo ->
                mBottomBinding.tabLayout.apply {
                    addTab(newTab().setText(groupInfo.name))
                }
            }
            mGroupAdapter.resetAll(value)
        }

    var isEnable : Boolean = true

    var onEnableChanged: ((Boolean) -> Unit)? = null

    var isTopLayoutVisible = false
        set(value) {
            field = value
            mTopBinding.root.isVisible = value
            mTopBinding.slider.isVisible = value
            mTopBinding.tvStrength.isVisible = value
        }

    var isTopSliderVisible = false
        set(value) {
            field = value
            mTopBinding.slider.visibility = if(field) View.VISIBLE else View.INVISIBLE
            mTopBinding.tvStrength.visibility = if(field) View.VISIBLE else View.INVISIBLE
        }

    private val mTopBinding by lazy {
        BeautyDialogTopBinding.inflate(
            LayoutInflater.from(
                context
            )
        )
    }

    private val mBottomBinding by lazy {
        BeautyDialogBottomBinding.inflate(
            LayoutInflater.from(
                context
            )
        )
    }


    private val mGroupAdapter =
        object : BindingSingleAdapter<GroupInfo, BeautyDialogPageBinding>() {

            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): BindingViewHolder<BeautyDialogPageBinding> {
                val viewHolder = super.onCreateViewHolder(parent, viewType)
                viewHolder.itemView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                return viewHolder
            }

            override fun onBindViewHolder(
                holder: BindingViewHolder<BeautyDialogPageBinding>,
                tabPosition: Int
            ) {
                val groupItem = getItem(tabPosition) ?: return
                val groupPosition = tabPosition

                val itemAdapter = object :
                    BindingSingleAdapter<ItemInfo, BeautyDialogItemBinding>() {

                    override fun onBindViewHolder(
                        holder: BindingViewHolder<BeautyDialogItemBinding>,
                        position: Int
                    ) {
                        val itemInfo = getItem(position) ?: return

                        holder.binding.ivIcon.isActivated = position == groupItem.selectedIndex
                        holder.binding.ivIcon.setImageResource(itemInfo.icon)
                        if (groupItem.selectedIndex == position && mBottomBinding.tabLayout.selectedTabPosition == groupPosition) {
                            bindSlider(itemInfo)
                        }
                        holder.binding.ivIcon.setOnClickListener {
                            if (position == groupItem.selectedIndex) {
                                return@setOnClickListener
                            }
                            val activate = !it.isActivated
                            it.isActivated = activate

                            val oSelectedIndex = groupItem.selectedIndex
                            groupItem.selectedIndex = position
                            notifyItemChanged(oSelectedIndex)
                            notifyItemChanged(groupItem.selectedIndex)
                            bindSlider(itemInfo)
                        }
                        holder.binding.tvName.setText(itemInfo.name)
                    }
                }
                itemAdapter.resetAll(groupItem.itemList)
                holder.binding.recycleView.adapter = itemAdapter
            }
        }


    private val mOnTabSelectedListener = object : OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            groupList[mBottomBinding.tabLayout.selectedTabPosition].apply {
                bindSlider(itemList[selectedIndex])
            }
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {

        }

        override fun onTabReselected(tab: TabLayout.Tab?) {

        }
    }

    init {
        setTopView(mTopBinding.root)
        setBottomView(mBottomBinding.root)

        mBottomBinding.viewPager.isUserInputEnabled = false
        mBottomBinding.viewPager.offscreenPageLimit = 1
        mBottomBinding.viewPager.adapter = mGroupAdapter

        mBottomBinding.tabLayout.addOnTabSelectedListener(mOnTabSelectedListener)
        TabLayoutMediator(
            mBottomBinding.tabLayout,
            mBottomBinding.viewPager
        ) { tab, position ->
            tab.text = context.getString(groupList[position].name)
        }.attach()

        mTopBinding.root.isVisible = false
        mTopBinding.ivCompare.setOnClickListener {
            isEnable = !isEnable
            onEnableChanged?.invoke(isEnable)
        }
    }

    private fun bindSlider(itemInfo: ItemInfo) {
        mTopBinding.slider.clearOnChangeListeners()
        mTopBinding.slider.clearOnSliderTouchListeners()

        mTopBinding.slider.value = itemInfo.value
        itemInfo.onValueChanged.invoke(this@BeautyDialog, itemInfo.value)
        mTopBinding.slider.addOnChangeListener { slider, sValure, fromUser ->
            itemInfo.value = sValure
            itemInfo.onValueChanged.invoke(this@BeautyDialog, sValure)
        }
    }
}