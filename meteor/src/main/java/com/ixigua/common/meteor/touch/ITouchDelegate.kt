package com.ixigua.common.meteor.touch

import android.view.MotionEvent

/**
 * Created by dss886 on 2019-05-08.
 */
interface ITouchDelegate {

    fun findTouchTarget(event: MotionEvent): ITouchTarget?

}