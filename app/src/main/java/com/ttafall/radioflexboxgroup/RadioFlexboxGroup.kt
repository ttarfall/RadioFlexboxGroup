package com.ttafall.radioflexboxgroup

/**
 * Created by ttarfall on 2017/5/17.
 */

import android.content.Context
import android.content.res.TypedArray
import android.support.annotation.IdRes
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioButton

import com.google.android.flexbox.FlexboxLayout

import java.util.concurrent.atomic.AtomicInteger

/**
 * @author ttarfall
 * *
 * @date 2017-05-17 11:11
 */
class RadioFlexboxGroup : FlexboxLayout {
    /**
     *
     * Returns the identifier of the selected radio button in this group.
     * Upon empty selection, the returned value is -1.

     * @return the unique id of the selected radio button in this group
     * *
     * @attr ref android.R.styleable#RadioGroup_checkedButton
     * *
     * @see .check
     * @see .clearCheck
     */
    var mCheckedId = -1
    var checkedRadioButtonId = -1
        @IdRes
        get() = mCheckedId
        private set
    // tracks children radio buttons checked state
    private var mChildOnCheckedChangeListener: CompoundButton.OnCheckedChangeListener? = null
    // when true, mOnCheckedChangeListener discards events
    private var mProtectFromCheckedChange = false
    private var mOnCheckedChangeListener: OnCheckedChangeListener? = null
    private var mPassThroughListener: PassThroughHierarchyChangeListener? = null

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        init()
    }

    private fun init() {
        mChildOnCheckedChangeListener = CheckedStateTracker()
        mPassThroughListener = PassThroughHierarchyChangeListener()
        super.setOnHierarchyChangeListener(mPassThroughListener)
    }

    /**
     * {@inheritDoc}
     */
    override fun setOnHierarchyChangeListener(listener: ViewGroup.OnHierarchyChangeListener?) {
        // the user listener is delegated to our pass-through listener
        mPassThroughListener?.mOnHierarchyChangeListener = listener
    }

    /**
     * {@inheritDoc}
     */
    override fun onFinishInflate() {
        super.onFinishInflate()

        // checks the appropriate radio button as requested in the XML file
        if (mCheckedId != -1) {
            mProtectFromCheckedChange = true
            setCheckedStateForView(mCheckedId, true)
            mProtectFromCheckedChange = false
            setCheckedId(mCheckedId)
        }
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        if (child is RadioButton) {
            val button = child
            if (button.isChecked) {
                mProtectFromCheckedChange = true
                if (mCheckedId != -1) {
                    setCheckedStateForView(mCheckedId, false)
                }
                mProtectFromCheckedChange = false
                setCheckedId(button.id)
            }
        }

        super.addView(child, index, params)
    }

    /**
     *
     * Sets the selection to the radio button whose identifier is passed in
     * parameter. Using -1 as the selection identifier clears the selection;
     * such an operation is equivalent to invoking [.clearCheck].

     * @param id the unique id of the radio button to select in this group
     * *
     * @see .getMCheckedId
     * @see .clearCheck
     */
    fun check(@IdRes id: Int) {
        // don't even bother
        if (id != -1 && id == mCheckedId) {
            return
        }

        if (mCheckedId != -1) {
            setCheckedStateForView(mCheckedId, false)
        }

        if (id != -1) {
            setCheckedStateForView(id, true)
        }

        setCheckedId(id)
    }

    private fun setCheckedId(@IdRes id: Int) {
        mCheckedId = id
        if (mOnCheckedChangeListener != null) {
            mOnCheckedChangeListener?.onCheckedChanged(this, mCheckedId)
        }
    }

    private fun setCheckedStateForView(viewId: Int, checked: Boolean) {
        val checkedView = findViewById<RadioButton>(viewId)
        if (checkedView != null) {
            checkedView.isChecked = checked
        }
    }

    /**
     *
     * Clears the selection. When the selection is cleared, no radio button
     * in this group is selected and [.getMCheckedId] returns
     * null.

     * @see .check
     * @see .getMCheckedId
     */
    fun clearCheck() {
        check(-1)
    }

    /**
     *
     * Register a callback to be invoked when the checked radio button
     * changes in this group.

     * @param listener the callback to call on checked state change
     */
    fun setOnCheckedChangeListener(listener: OnCheckedChangeListener) {
        mOnCheckedChangeListener = listener
    }

    /**
     * {@inheritDoc}
     */
    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return RadioFlexboxGroup.LayoutParams(context, attrs)
    }

    /**
     * {@inheritDoc}
     */
    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean {
        return p is LayoutParams
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun getAccessibilityClassName(): CharSequence {
        return RadioFlexboxGroup::class.java.name
    }

    /**
     *
     * This set of layout parameters defaults the width and the height of
     * the children to [.WRAP_CONTENT] when they are not specified in the
     * XML file. Otherwise, this class ussed the value read from the XML file.
     *
     *
     *
     * See
     * for a list of all child view attributes that this class supports.
     */
    class LayoutParams : FlexboxLayout.LayoutParams {
        /**
         * {@inheritDoc}
         */
        constructor(c: Context?, attrs: AttributeSet?) : super(c, attrs)

        /**
         * {@inheritDoc}
         */
        constructor(w: Int, h: Int) : super(w, h)

        /**
         * {@inheritDoc}
         */
        constructor(p: ViewGroup.LayoutParams?) : super(p)

        /**
         * {@inheritDoc}
         */
        constructor(source: ViewGroup.MarginLayoutParams?) : super(source)

        /**
         *
         * Fixes the child's width to
         * [ViewGroup.LayoutParams.WRAP_CONTENT] and the child's
         * height to  [ViewGroup.LayoutParams.WRAP_CONTENT]
         * when not specified in the XML file.

         * @param a          the styled attributes set
         * *
         * @param widthAttr  the width attribute to fetch
         * *
         * @param heightAttr the height attribute to fetch
         */
        override fun setBaseAttributes(a: TypedArray,
                                       widthAttr: Int, heightAttr: Int) {

            if (a.hasValue(widthAttr)) {
                width = a.getLayoutDimension(widthAttr, "layout_width")
            } else {
                width = ViewGroup.LayoutParams.WRAP_CONTENT
            }

            if (a.hasValue(heightAttr)) {
                height = a.getLayoutDimension(heightAttr, "layout_height")
            } else {
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }
    }

    /**
     *
     * Interface definition for a callback to be invoked when the checked
     * radio button changed in this group.
     */
    interface OnCheckedChangeListener {
        /**
         *
         * Called when the checked radio button has changed. When the
         * selection is cleared, checkedId is -1.

         * @param group     the group in which the checked radio button has changed
         * *
         * @param checkedId the unique identifier of the newly checked radio button
         */
        fun onCheckedChanged(group: RadioFlexboxGroup, @IdRes checkedId: Int)
    }

    private inner class CheckedStateTracker : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            // prevents from infinite recursion
            if (mProtectFromCheckedChange) {
                return
            }

            mProtectFromCheckedChange = true
            if (mCheckedId != -1) {
                setCheckedStateForView(mCheckedId, false)
            }
            mProtectFromCheckedChange = false

            val id = buttonView.id
            setCheckedId(id)
        }
    }

    /**
     *
     * A pass-through listener acts upon the events and dispatches them
     * to another listener. This allows the table layout to set its own internal
     * hierarchy change listener without preventing the user to setup his.
     */
    private inner class PassThroughHierarchyChangeListener : ViewGroup.OnHierarchyChangeListener {
        var mOnHierarchyChangeListener: ViewGroup.OnHierarchyChangeListener? = null

        /**
         * {@inheritDoc}
         */
        override fun onChildViewAdded(parent: View, child: View) {
            if (parent === this@RadioFlexboxGroup && child is RadioButton) {
                var id = child.getId()
                // generates an id if it's missing
                if (id == View.NO_ID) {
                    id = generateViewId()
                    child.setId(id)
                }
                child.setOnCheckedChangeListener(
                        mChildOnCheckedChangeListener)
            }

            mOnHierarchyChangeListener?.onChildViewAdded(parent, child)
        }

        /**
         * {@inheritDoc}
         */
        override fun onChildViewRemoved(parent: View, child: View) {
            if (parent === this@RadioFlexboxGroup && child is RadioButton) {
                child.setOnCheckedChangeListener(null)
            }

            mOnHierarchyChangeListener?.onChildViewRemoved(parent, child)
        }
    }

    companion object {

        private val sNextGeneratedId = AtomicInteger(1)

        fun generateViewId(): Int {
            while (true) {
                val result = sNextGeneratedId.get()
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                var newValue = result + 1
                if (newValue > 0x00FFFFFF) newValue = 1 // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result
                }
            }
        }
    }
}
