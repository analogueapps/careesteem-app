/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.aits.careesteem.R
import kotlin.properties.Delegates

@SuppressLint("UseCompatLoadingForDrawables")
class PinView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    var setOnCompletedListener: (pinCode: String) -> Unit = {}
    var setOnPinKeyClickListener: (keyPressed: String) -> Unit = {}
    var onFingerprintClickListener: (() -> Unit)? = null

    private lateinit var attributes: TypedArray

    lateinit var pinOneProgress: View
    lateinit var pinTwoProgress: View
    lateinit var pinThreeProgress: View
    lateinit var pinFourProgress: View

    //lateinit var pinFiveProgress: View
    //lateinit var pinSixProgress: View
    lateinit var numbersGridView: RecyclerView
    lateinit var forgotPasscode: TextView

    private var currentPinCode by Delegates.observable("") { _, _, newValue ->
        val drawable = DrawableCompat.wrap(resources.getDrawable(R.drawable.oval, null))
        DrawableCompat.setTint(
            drawable,
            attributes.getColor(R.styleable.PinView_dotProgressColor, Color.parseColor("#304FFE"))
        )

        if (newValue.length >= 0) initPinCodeProgress()
        if (newValue.length >= 1) pinOneProgress.background = drawable
        if (newValue.length >= 2) pinTwoProgress.background = drawable
        if (newValue.length >= 3) pinThreeProgress.background = drawable
        if (newValue.length >= 4) pinFourProgress.background = drawable
//        if (newValue.length >= 5) pinFiveProgress.background = drawable
//        if (newValue.length >= 6) pinSixProgress.background = drawable
    }

    private var isFingerVisible = false

    init {
        inflate(context, R.layout.pin_view, this)
        attributes = context.obtainStyledAttributes(attrs, R.styleable.PinView)

        isFingerVisible = attributes.getBoolean(R.styleable.PinView_fingerVisible, false)

        initializeViews()

        initPinCodeProgress()

        setDotProgressLayoutParams(pinOneProgress)
        setDotProgressLayoutParams(pinTwoProgress)
        setDotProgressLayoutParams(pinThreeProgress)
        setDotProgressLayoutParams(pinFourProgress)
//        setDotProgressLayoutParams(pinFiveProgress)
//        setDotProgressLayoutParams(pinSixProgress)

        numbersGridView.adapter =
            NumbersAdapter(attributes, isFingerVisible, onFingerprintClickListener)
    }

    // create method for setting fingerVisible
    var fingerVisible: Boolean
        get() = isFingerVisible
        set(value) {
            isFingerVisible = value
            numbersGridView.adapter =
                NumbersAdapter(attributes, isFingerVisible, onFingerprintClickListener)
        }


    private fun initializeViews() {
        pinOneProgress = findViewById<View>(R.id.pinOneProgress)
        pinTwoProgress = findViewById<View>(R.id.pinTwoProgress)
        pinThreeProgress = findViewById<View>(R.id.pinThreeProgress)
        pinFourProgress = findViewById<View>(R.id.pinFourProgress)
//        pinFiveProgress = findViewById<View>(R.id.pinFiveProgress)
//        pinSixProgress = findViewById<View>(R.id.pinSixProgress)
        numbersGridView = findViewById<RecyclerView>(R.id.numbersGridView)
        forgotPasscode = findViewById<TextView>(R.id.forgotPasscode)

        // showForgotPasscode
        showForgotPin(attributes.getBoolean(R.styleable.PinView_showForgotPasscode, false))

        forgotPasscode.setOnClickListener {
            forgotPasscodeClick()
        }
    }

    private fun setDotProgressLayoutParams(view: View) {
        view.layoutParams.width =
            attributes.getDimensionPixelSize(R.styleable.PinView_dotRadius, 30)
        view.layoutParams.height =
            attributes.getDimensionPixelSize(R.styleable.PinView_dotRadius, 30)
    }

    private fun initPinCodeProgress() {
        val drawable = DrawableCompat.wrap(resources.getDrawable(R.drawable.oval, null))
        DrawableCompat.setTint(
            drawable,
            attributes.getColor(R.styleable.PinView_dotUnProgressColor, Color.LTGRAY)
        )

        pinOneProgress.background = drawable
        pinTwoProgress.background = drawable
        pinThreeProgress.background = drawable
        pinFourProgress.background = drawable
//        pinFiveProgress.background = drawable
//        pinSixProgress.background = drawable
    }

    fun deleteLastPin() {
        if (currentPinCode.isNotEmpty())
            currentPinCode = currentPinCode.dropLast(1)
    }

    fun clearPin() {
        currentPinCode = ""
    }

    private fun showForgotPin(isEnabled: Boolean) {
        forgotPasscode.visibility = if (isEnabled) View.VISIBLE else View.GONE
    }

    fun forgotPasscodeClick() {

    }

    private fun appendNumber(number: Int) {
        if (currentPinCode.length < 3) {
            currentPinCode = currentPinCode.plus(number)
            setOnPinKeyClickListener(number.toString())
        } else if (currentPinCode.length == 3) {
            currentPinCode = currentPinCode.plus(number)
            setOnCompletedListener(currentPinCode)
        }
    }

    private inner class NumbersAdapter(
        private val attributes: TypedArray,
        private val isFingerVisible: Boolean,
        private val onFingerprintClick: (() -> Unit)?
    ) :
        RecyclerView.Adapter<NumbersAdapter.ViewHolder>() {

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_number, parent, false)
            return ViewHolder(view, attributes)
        }

        override fun getItemCount(): Int {
            return 12
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(holder, position)
        }

        inner class ViewHolder(itemView: View, attributes: TypedArray) :
            RecyclerView.ViewHolder(itemView) {
            val numberTextView: TextView = itemView.findViewById(R.id.numberTextView)
            val deleteImageView: ImageView = itemView.findViewById(R.id.deleteImageView)
            val fingerImageView: ImageView = itemView.findViewById(R.id.fingerImageView)

            init {
                initializeViewHolderViews()
            }

            private fun initializeViewHolderViews() {
                val fontSize =
                    attributes.getDimensionPixelSize(R.styleable.PinView_numbersTextSize, 64)

                val fontSizePass =
                    attributes.getDimensionPixelSize(R.styleable.PinView_numbersTextSize, 70)

//                deleteImageView.layoutParams.width = fontSize
//                deleteImageView.layoutParams.height = fontSize

//                fingerImageView.layoutParams.width = fontSizePass
//                fingerImageView.layoutParams.height = fontSizePass

//                deleteImageView.setColorFilter(
//                    attributes.getColor(
//                        R.styleable.PinView_deleteButtonColor,
//                        Color.BLACK
//                    )
//                )
            }

            fun bind(viewHolder: ViewHolder, position: Int) {
                val number = position + 1
                when {
                    position <= 8 -> {
                        viewHolder.apply {
                            numberTextView.text = number.toString()
                            itemView.setOnClickListener {
                                appendNumber(number)
                            }
                        }
                    }

//                    position == 9 -> {
//                        viewHolder.apply {
//                            numberTextView.visibility = View.GONE
//                            fingerImageView.visibility = View.VISIBLE
//                            itemView.setOnClickListener {
//                                deleteLastPin()
//                                setOnPinKeyClickListener("delete")
//                            }
//                        }
//                    }

                    position == 9 -> {
                        viewHolder.apply {
                            numberTextView.visibility = View.GONE
                            fingerImageView.visibility =
                                if (isFingerVisible) View.VISIBLE else View.GONE
                            itemView.setOnClickListener {
                                if (isFingerVisible) {
//                                    deleteLastPin()
//                                    setOnPinKeyClickListener("fingerprint")
                                    onFingerprintClickListener?.invoke()
                                }
                            }
                        }
                    }

                    position == 10 -> viewHolder.apply {
                        numberTextView.text = "0"
                        itemView.setOnClickListener {
                            appendNumber(0)
                            setOnPinKeyClickListener("0")
                        }
                    }

//                    position == 11 -> viewHolder.apply {
//                        numberTextView.text = "C"
//                        itemView.setOnClickListener {
//                            clearPin()
//                            setOnPinKeyClickListener("clear")
//                        }
//                    }

                    position == 11 -> {
                        viewHolder.apply {
                            numberTextView.visibility = View.GONE
                            deleteImageView.visibility = View.VISIBLE
                            itemView.setOnClickListener {
                                deleteLastPin()
                                setOnPinKeyClickListener("delete")
                            }
                        }
                    }
                }
            }
        }
    }
}