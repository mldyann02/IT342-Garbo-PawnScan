package com.cit.pawnscan.features.auth

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class PhilippinePhoneTextWatcher(private val input: EditText) : TextWatcher {
    private var isUpdating = false

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

    override fun afterTextChanged(s: Editable?) {
        if (isUpdating || s == null) return

        var value = s.toString().replace(Regex("[^\\d+]"), "")
        if (value.startsWith("09")) {
            value = "+63" + value.drop(1)
        } else if (value.startsWith("639")) {
            value = "+$value"
        } else if (!value.startsWith("+639")) {
            value = "+639"
        }

        val digits = value.drop(4).replace(Regex("\\D"), "").take(9)
        val formatted = "+639$digits"
        if (formatted != s.toString()) {
            isUpdating = true
            input.setText(formatted)
            input.setSelection(formatted.length)
            isUpdating = false
        }
    }
}
