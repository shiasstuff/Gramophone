/*
 *     Copyright (C) 2024 Akane Foundation
 *
 *     Gramophone is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Gramophone is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.akanework.gramophone.logic.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.content.res.AppCompatResources
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import me.zhanghai.android.fastscroll.FastScroller
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import me.zhanghai.android.fastscroll.FixOnItemTouchListenerRecyclerView
import me.zhanghai.android.fastscroll.PopupTextProvider
import org.akanework.gramophone.R


class MyRecyclerView(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int)
	: FixOnItemTouchListenerRecyclerView(context, attributeSet, defStyleAttr) {
	constructor(context: Context, attributeSet: AttributeSet?)
			: this(context, attributeSet, 0)
	constructor(context: Context) : this(context, null)

	private var appBarLayout: AppBarLayout? = null
	private var scrollInProgress = false
	private var scrollIsNatural = false

	fun setAppBar(appBarLayout: AppBarLayout) {
		this.appBarLayout = appBarLayout
	}

	private fun setAppBarExpanded(expanded: Boolean) {
		val behavior = (appBarLayout?.layoutParams as CoordinatorLayout.LayoutParams?)?.behavior
		val isExpanded = behavior is AppBarLayout.Behavior && behavior.topAndBottomOffset == 0
		if (isExpanded != expanded) {
			appBarLayout?.setExpanded(expanded, true)
		}
	}

	// TODO expand/unexpand with fastscroll too

	override fun fling(velocityX: Int, velocityY: Int): Boolean {
		scrollInProgress = true
		val isZero = (layoutManager as? LinearLayoutManager)
			?.findFirstVisibleItemPosition() == 0
		if (isZero && velocityY > 0)
			setAppBarExpanded(false)
		return super.fling(velocityX, velocityY)
	}

	override fun smoothScrollBy(dx: Int, dy: Int) {
		scrollInProgress = true
		val isZero = (layoutManager as? LinearLayoutManager)
			?.findFirstVisibleItemPosition() == 0
		if (isZero && dy > 0)
			setAppBarExpanded(false)
		super.smoothScrollBy(dx, dy)
	}

	fun startSmoothScrollCompat(scroller: SmoothScroller) {
		scrollInProgress = true
		val isZero = (layoutManager as? LinearLayoutManager)
			?.findFirstVisibleItemPosition() == 0
		if (isZero && scroller.targetPosition > 0)
			setAppBarExpanded(false)
		layoutManager?.startSmoothScroll(scroller)
	}

	override fun onScrollStateChanged(state: Int) {
		super.onScrollStateChanged(state)
		if (state == SCROLL_STATE_DRAGGING) {
			scrollInProgress = true
			scrollIsNatural = true
		} else if (!scrollIsNatural && state == SCROLL_STATE_SETTLING) {
			scrollInProgress = true
		} else if (state == SCROLL_STATE_IDLE) {
			if (scrollInProgress && !scrollIsNatural) {
				val pos =
					(layoutManager as? LinearLayoutManager)?.findFirstCompletelyVisibleItemPosition()
				setAppBarExpanded(pos == 0)
			}
			scrollInProgress = false
			scrollIsNatural = false
		}
	}

	fun awakenScrollBarsPublic() {
		// TODO this is a hack
		scrollBy(0, 1)
		scrollBy(0, -1)
	}

	fun fastScroll(popupTextProvider: PopupTextProvider?): FastScroller {
		return FastScrollerBuilder(this)
			// TODO some popuptextprovider produce junk that breaks fastscroller
			.setViewHelper(RecyclerViewHelper(this, popupTextProvider))
			.useMd2Style()
			.setTrackDrawable(
				AppCompatResources.getDrawable(
					context,
					R.drawable.ic_transparent
				)!!
			)
			.build()
	}

	abstract class Adapter<VH : ViewHolder> : RecyclerView.Adapter<VH>() {
		final override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
			super.onAttachedToRecyclerView(recyclerView)
			onAttachedToRecyclerView(recyclerView as MyRecyclerView)
		}

		final override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
			super.onDetachedFromRecyclerView(recyclerView)
			onDetachedFromRecyclerView(recyclerView as MyRecyclerView)
		}

		open fun onAttachedToRecyclerView(recyclerView: MyRecyclerView) {}
		open fun onDetachedFromRecyclerView(recyclerView: MyRecyclerView) {}
	}
}