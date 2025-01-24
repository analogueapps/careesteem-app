/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.utils

object AppConstant {
    // def Values
    const val TRUE: Boolean = true
    const val FALSE: Boolean = false
    const val YES: String = "Yes"
    const val NO: String = "No"
    const val ACTIVE: String = "Active"

    fun maskPhoneNumber(phoneNumber: String): String {
        return if (phoneNumber.length > 2) {
            // Mask all characters except the last two
            val maskedPart = "*".repeat(phoneNumber.length - 2)
            val lastTwoDigits = phoneNumber.takeLast(2)
            "$maskedPart$lastTwoDigits"
        } else {
            // If the number is too short, return it as is
            phoneNumber
        }
    }
}