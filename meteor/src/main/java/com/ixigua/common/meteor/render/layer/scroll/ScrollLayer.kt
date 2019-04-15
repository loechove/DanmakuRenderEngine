package com.ixigua.common.meteor.render.layer.scroll

import android.view.MotionEvent
import com.ixigua.common.meteor.control.DanmakuController
import com.ixigua.common.meteor.data.IDanmakuData
import com.ixigua.common.meteor.render.IRenderLayer
import com.ixigua.common.meteor.render.cache.IDrawCachePool
import com.ixigua.common.meteor.render.draw.IDrawItem
import com.ixigua.common.meteor.touch.ITouchDelegate
import com.ixigua.common.meteor.touch.ITouchTarget
import com.ixigua.common.meteor.utils.LAYER_TYPE_SCROLL

/**
 * Created by dss886 on 2018/11/8.
 */
class ScrollLayer(private val mController: DanmakuController,
                  private val mCachePool: IDrawCachePool) : IRenderLayer, ITouchDelegate {

    private val mScrollLines = mutableListOf<ScrollLine>()
    private val mPreDrawItems = mutableListOf<IDrawItem<IDanmakuData>>()
    private var mWidth = 0
    private var mHeight = 0

    override fun getLayerType(): Int {
        return LAYER_TYPE_SCROLL
    }

    override fun onLayoutSizeChanged(width: Int, height: Int) {
        mWidth = width
        mHeight = height
        val config = mController.config
        val lineHeight = config.scroll.lineHeight
        val lineMargin = config.scroll.lineMargin
        val displayHeight = height * config.scroll.displayPercent
        val maxLineCount = (displayHeight / (lineHeight + lineMargin)).toInt()
        configScrollLine(maxLineCount, lineHeight, lineMargin)
    }

    override fun addItem(item: IDrawItem<IDanmakuData>) {
        mScrollLines.forEach { line ->
            if (line.addItem(item)) {
                return
            }
        }
        // No Line has enough space to place this item, so discard it
        mCachePool.release(item)
    }

    override fun typesetting(isPlaying: Boolean, configChanged: Boolean) {
        mScrollLines.forEach { line ->
            line.typesetting(isPlaying, configChanged)
        }
    }

    override fun getPreDrawItems(): List<IDrawItem<IDanmakuData>> {
        mPreDrawItems.clear()
        mScrollLines.forEach { line ->
            mPreDrawItems.addAll(line.getPreDrawItems())
        }
        return mPreDrawItems
    }

    override fun findTouchTarget(event: MotionEvent): ITouchTarget? {
        mScrollLines.forEach { line ->
            if (event.y > line.y + line.height) {
                return@forEach
            }
            if (event.y >= line.y) {
                return line
            }
            return null
        }
        return null
    }

    override fun clear() {
        mScrollLines.forEach { line ->
            line.clearRender()
        }
    }

    private fun configScrollLine(maxLineCount: Int, lineHeight: Float, lineMargin: Float) {
        if (maxLineCount > mScrollLines.size) {
            for (i in 1..(maxLineCount - mScrollLines.size)) {
                mScrollLines.add(ScrollLine(mController, mCachePool).apply {
                    mController.registerCmdMonitor(this)
                })
            }
        } else if (maxLineCount < mScrollLines.size) {
            val subLines = mScrollLines.take(maxLineCount)
            mScrollLines.takeLast(mScrollLines.size - maxLineCount).forEach {
                mController.unRegisterCmdMonitor(it)
            }
            mScrollLines.clear()
            mScrollLines.addAll(subLines)
        }
        mScrollLines.forEachIndexed { index, line ->
            line.onLayoutChanged(mWidth.toFloat(), lineHeight, (index + 1) * lineMargin + index * lineHeight)
        }
    }
}
