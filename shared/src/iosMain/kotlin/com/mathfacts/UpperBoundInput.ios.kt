package com.mathfacts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSSelectorFromString
import platform.UIKit.NSTextAlignmentCenter
import platform.UIKit.UIBarButtonItem
import platform.UIKit.UIBarButtonSystemItem
import platform.UIKit.UIColor
import platform.UIKit.UIControlEventEditingChanged
import platform.UIKit.UIControlEventEditingDidBegin
import platform.UIKit.UIControlEventEditingDidEnd
import platform.UIKit.UIFont
import platform.UIKit.UIKeyboardTypeASCIICapableNumberPad
import platform.UIKit.UILabel
import platform.UIKit.UITapGestureRecognizer
import platform.UIKit.UITextBorderStyle
import platform.UIKit.UITextField
import platform.UIKit.UIToolbar
import platform.UIKit.UIView
import kotlin.math.max

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class UpperBoundEditor : UIView(frame = CGRectMake(0.0, 0.0, 130.0, 96.0)) {
    private val blue = UIColor.colorWithRed(59.0 / 255.0, 130.0 / 255.0, 246.0 / 255.0, 1.0)
    private val yellow = UIColor.colorWithRed(1.0, 200.0 / 255.0, 87.0 / 255.0, 1.0)
    private var hasError = false

    private val label = UILabel().apply {
        text = "Up to"
        textColor = UIColor.whiteColor
        font = UIFont.boldSystemFontOfSize(18.0)
        textAlignment = NSTextAlignmentCenter
        backgroundColor = UIColor.clearColor
    }

    val textField = UITextField().apply {
        borderStyle = UITextBorderStyle.UITextBorderStyleNone
        keyboardType = UIKeyboardTypeASCIICapableNumberPad
        textAlignment = NSTextAlignmentCenter
        font = UIFont.boldSystemFontOfSize(48.0)
        textColor = UIColor.whiteColor
        tintColor = yellow
        backgroundColor = UIColor.clearColor
        inputAccessoryView = UIToolbar().apply {
            items = listOf(
                UIBarButtonItem(
                    barButtonSystemItem = UIBarButtonSystemItem.UIBarButtonSystemItemFlexibleSpace,
                    target = null,
                    action = null,
                ),
                UIBarButtonItem(
                    barButtonSystemItem = UIBarButtonSystemItem.UIBarButtonSystemItemDone,
                    target = this@UpperBoundEditor,
                    action = NSSelectorFromString("dismissKeyboard"),
                ),
            )
            sizeToFit()
        }
        addTarget(
            target = this@UpperBoundEditor,
            action = NSSelectorFromString("editingChanged"),
            forControlEvents = UIControlEventEditingChanged,
        )
        addTarget(
            target = this@UpperBoundEditor,
            action = NSSelectorFromString("editingDidBegin"),
            forControlEvents = UIControlEventEditingDidBegin,
        )
        addTarget(
            target = this@UpperBoundEditor,
            action = NSSelectorFromString("editingDidEnd"),
            forControlEvents = UIControlEventEditingDidEnd,
        )
    }

    var onValueChange: (TextFieldValue) -> Unit = {}
    private var acceptedText = ""

    init {
        backgroundColor = UIColor.clearColor
        layer.cornerRadius = 16.0
        layer.borderWidth = 2.0
        addSubview(label)
        addSubview(textField)
        addGestureRecognizer(
            UITapGestureRecognizer(
                target = this,
                action = NSSelectorFromString("focusAndSelectAll"),
            ).apply {
                cancelsTouchesInView = true
            },
        )
        updateBorder()
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        val (viewWidth, viewHeight) = bounds.useContents { size.width to size.height }
        label.setFrame(CGRectMake(0.0, 5.0, viewWidth, 20.0))
        textField.setFrame(CGRectMake(0.0, 24.0, viewWidth, max(0.0, viewHeight - 24.0)))
    }

    fun setValue(value: TextFieldValue) {
        acceptedText = value.text
        if (textField.text != value.text) textField.text = value.text
    }

    fun setError(isError: Boolean) {
        hasError = isError
        updateBorder()
    }

    private fun updateBorder() {
        layer.borderColor = when {
            hasError -> UIColor.redColor.CGColor
            textField.isFirstResponder() -> yellow.CGColor
            else -> blue.CGColor
        }
    }

    @Suppress("UNUSED")
    @kotlinx.cinterop.ObjCAction
    fun editingChanged() {
        val proposed = textField.text.orEmpty()
        if (proposed.length <= 3 && proposed.all(Char::isDigit)) {
            acceptedText = proposed
            onValueChange(TextFieldValue(proposed, TextRange(proposed.length)))
        } else {
            textField.text = acceptedText
        }
    }

    @Suppress("UNUSED")
    @kotlinx.cinterop.ObjCAction
    fun editingDidBegin() {
        updateBorder()
        textField.selectAll(null)
    }

    @Suppress("UNUSED")
    @kotlinx.cinterop.ObjCAction
    fun focusAndSelectAll() {
        textField.becomeFirstResponder()
        textField.selectAll(null)
        updateBorder()
    }

    @Suppress("UNUSED")
    @kotlinx.cinterop.ObjCAction
    fun editingDidEnd() {
        updateBorder()
    }

    @Suppress("UNUSED")
    @kotlinx.cinterop.ObjCAction
    fun dismissKeyboard() {
        textField.resignFirstResponder()
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformUpperBoundInput(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    isError: Boolean,
    modifier: Modifier,
) {
    UIKitView(
        factory = { UpperBoundEditor() },
        modifier = modifier.width(130.dp).height(96.dp),
        background = androidx.compose.ui.graphics.Color.Transparent,
        update = { editor ->
            editor.onValueChange = onValueChange
            editor.setValue(value)
            editor.setError(isError)
        },
    )
}
