/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.bodymap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import java.util.Stack

class BodyMapView(context: Context, attrs: AttributeSet?) : AppCompatImageView(context, attrs) {

    private val pathPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        //color = Color.GREEN
        color = Color.parseColor("#279989")
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    private var markerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        //color = Color.GREEN // Default Marker Color
        // color is #B7EAE3
        color = Color.parseColor("#279989")
        style = Paint.Style.FILL
    }

    private val markers = mutableListOf<PointF>() // Stores marker positions
    private val paths = mutableListOf<Path>() // Stores drawn paths

    private val undoStack = Stack<Any>() // Stores markers and paths for undo
    private val redoStack = Stack<Any>() // Stores undone actions for redo

    private var selectedMarker: PointF? = null // For editing markers
    private var currentPath: Path? = null
    private var isDrawing = false

    init {
        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val touchedPoint = PointF(event.x, event.y)

                    if (selectMarker(touchedPoint)) {
                        invalidate()
                    } else {
                        isDrawing = true
                        currentPath = Path().apply { moveTo(event.x, event.y) }
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    if (isDrawing) {
                        currentPath?.lineTo(event.x, event.y)
                        invalidate()
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if (isDrawing) {
                        currentPath?.let {
                            paths.add(it)
                            undoStack.push(it) // Add to undo stack
                            redoStack.clear() // Clear redo stack on new action
                        }
                        isDrawing = false
                        currentPath = null
                    } else {
                        markers.add(PointF(event.x, event.y))
                        undoStack.push(markers.last()) // Add marker to undo stack
                        redoStack.clear() // Clear redo stack on new action
                    }
                    invalidate()
                }
            }
            true
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw existing paths
        for (path in paths) {
            canvas.drawPath(path, pathPaint)
        }

        // Draw markers
        for (marker in markers) {
            canvas.drawCircle(marker.x, marker.y, 20f, markerPaint)
        }

        // Draw current path while drawing
        currentPath?.let {
            canvas.drawPath(it, pathPaint)
        }
    }

    private fun selectMarker(touchPoint: PointF): Boolean {
        for (marker in markers) {
            if (Math.hypot(
                    (marker.x - touchPoint.x).toDouble(),
                    (marker.y - touchPoint.y).toDouble()
                ) < 30
            ) {
                selectedMarker = marker
                showEditDeleteDialog()
                return true
            }
        }
        return false
    }

    private fun showEditDeleteDialog() {
        AlertDialog.Builder(context)
            .setTitle("Edit Marker")
            .setMessage("Do you want to remove this marker?")
            .setPositiveButton("Delete") { _, _ ->
                selectedMarker?.let {
                    markers.remove(it)
                    undoStack.remove(it) // Remove from undo stack
                    redoStack.clear() // Clear redo stack on delete
                }
                invalidate()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val lastItem = undoStack.pop()
            redoStack.push(lastItem) // Save undone action in redo stack
            when (lastItem) {
                is PointF -> markers.remove(lastItem) // Remove last marker
                is Path -> paths.remove(lastItem) // Remove last path
            }
            invalidate()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val lastItem = redoStack.pop()
            undoStack.push(lastItem) // Push back to undo stack
            when (lastItem) {
                is PointF -> markers.add(lastItem) // Restore marker
                is Path -> paths.add(lastItem) // Restore path
            }
            invalidate()
        }
    }

    fun changeMarkerColor(newColor: Int) {
        markerPaint.color = newColor
        invalidate()
    }

    //    fun getBitmap(): Bitmap {
//        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(bitmap)
//        draw(canvas)
//        return bitmap
//    }
    fun getBitmap(): Bitmap {
        // Create the bitmap with the specified width and height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // Create a Canvas object with the bitmap
        val canvas = Canvas(bitmap)

        // Set the background color
        canvas.drawColor(Color.parseColor("#F0FCFA"))  // Set background color (#F0FCFA in this case)

        // Draw on the canvas
        draw(canvas)

        return bitmap
    }

    fun hasMarkers(): Boolean {
        return markers.isNotEmpty()
    }

}




