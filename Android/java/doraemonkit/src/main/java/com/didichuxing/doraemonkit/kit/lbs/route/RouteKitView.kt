package com.didichuxing.doraemonkit.kit.lbs.route

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.children
import com.amap.api.maps.AMap
import com.amap.api.navi.AMapNavi
import com.amap.api.navi.enums.PathPlanningStrategy
import com.amap.api.navi.model.NaviLatLng
import com.blankj.utilcode.util.ConvertUtils
import com.didichuxing.doraemonkit.DoraemonKit
import com.didichuxing.doraemonkit.R
import com.didichuxing.doraemonkit.kit.core.AbsDokitView
import com.didichuxing.doraemonkit.kit.core.DokitViewLayoutParams
import com.didichuxing.doraemonkit.kit.core.DokitViewManager
import com.didichuxing.doraemonkit.kit.gpsmock.GpsMockManager
import com.didichuxing.doraemonkit.util.DokitUtil
import com.didichuxing.doraemonkit.util.LogHelper
import com.didichuxing.doraemonkit.util.UIUtils
import kotlin.math.ceil

/**
 * ================================================
 * 作    者：jint（金台）
 * 版    本：1.0
 * 创建日期：2/25/21-14:44
 * 描    述：
 * 修订历史：
 * ================================================
 */
class RouteKitView : AbsDokitView() {
    companion object {
        const val TAG = "RouteKitView"
    }

    private var aMap: AMap? = null

    private var mAMapNavi: AMapNavi? = null

    override fun onCreate(context: Context?) {
        mAMapNavi = AMapNavi.getInstance(activity?.application)
    }

    override fun onCreateView(context: Context?, rootView: FrameLayout?): View {
        return LayoutInflater.from(context).inflate(R.layout.dk_float_lbs_route, rootView, false)
    }

    var index = 0

    lateinit var mSeekbar: SeekBar
    lateinit var mTvTip: TextView
    override fun onViewCreated(rootView: FrameLayout?) {
        rootView?.let {
            val close = it.findViewById<ImageView>(R.id.iv_close)
            mSeekbar = it.findViewById<SeekBar>(R.id.seekbar)
            mTvTip = it.findViewById<TextView>(R.id.tv_tip)
            mSeekbar.progress = 0
            val tvProgress = it.findViewById<TextView>(R.id.tv_progress)
            tvProgress.text = "当前导航进度: 0%"
            close.setOnClickListener {
                DokitViewManager.getInstance().detach(this)
            }


            mSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    tvProgress.text = "当前导航进度: $progress%"
                    mAMapNavi?.let { navi ->
                        if (navi.naviPath.coordList.isEmpty()) {
                            return
                        }
                        var index: Int =
                            ceil(navi.naviPath.coordList.size * progress / 100.0).toInt()
                        if (index > navi.naviPath.coordList.size - 1) {
                            index = navi.naviPath.coordList.size - 1
                        }
                        val naviLatLng = navi.naviPath.coordList[index]
                        GpsMockManager.getInstance()
                            .mockLocationWithNotify(naviLatLng.latitude, naviLatLng.longitude)

                    }


                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }

            })


        }


    }

    override fun initDokitViewLayoutParams(params: DokitViewLayoutParams?) {
        params?.let {
            it.width = ConvertUtils.dp2px(300.0f)
            it.height = DokitViewLayoutParams.WRAP_CONTENT
            it.gravity = Gravity.TOP or Gravity.LEFT
            it.x = 200
            it.y = 200
        }
    }


    override fun onResume() {
        super.onResume()
        if (findAMapView() == null) {
            mSeekbar.visibility = View.GONE
            mTvTip.visibility = View.VISIBLE
            return
        }

        mAMapNavi?.let {
            if (it.naviPath.coordList.isEmpty()) {
                mSeekbar.visibility = View.GONE
                mTvTip.visibility = View.VISIBLE
            } else {
                mSeekbar.visibility = View.VISIBLE
                mTvTip.visibility = View.GONE
            }
        }

    }

    private fun findAMapView(): com.amap.api.maps.MapView? {
        val decorView = activity.window.decorView as ViewGroup
        decorView.children.forEach {
            LogHelper.i(
                TAG, "viewId====>${
                    UIUtils.getRealIdText(it)
                }"
            )
            when (it) {
                is com.amap.api.maps.MapView -> return it
                is ViewGroup -> return traversAMapView(it)
            }

        }
        return null
    }

    private fun traversAMapView(viewGroup: ViewGroup): com.amap.api.maps.MapView? {
        viewGroup.children.forEach {
            LogHelper.i(
                TAG, "viewId====>${
                    UIUtils.getRealIdText(it)
                }"
            )
            when (it) {
                is com.amap.api.maps.MapView -> return it
                is ViewGroup -> return traversAMapView(it)
            }

        }
        return null
    }


}