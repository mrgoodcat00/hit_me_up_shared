package com.mrgoodcat.hitmeup.presentation.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrgoodcat.hitmeup.presentation.ui.theme.BlackTitle
import com.mrgoodcat.hitmeup.presentation.ui.theme.ErrorBorderColor
import com.mrgoodcat.hitmeup.presentation.ui.theme.GrayTitle
import com.mrgoodcat.hitmeup.presentation.ui.theme.LightBlueBorder
import com.mrgoodcat.hitmeup.presentation.ui.theme.LightGreyBorder
import com.mrgoodcat.hitmeup.presentation.ui.theme.LightGreyText
import com.mrgoodcat.hitmeup.presentation.ui.theme.Roboto

@Stable
@Composable
fun InputFieldComponent(
    fieldTitle: String,
    fieldText: String,
    titleColor: Color = GrayTitle,
    enabled: Boolean = true,
    fieldErrorText: String,
    isFieldError: Boolean,
    placeHolderText: String = fieldTitle,
    isRequired: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions(),
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onValueChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Top,
        modifier = Modifier.padding(0.dp)
    ) {
        Text(
            text = fieldTitle + if (isRequired) "*" else "",
            fontSize = 13.sp,
            lineHeight = 16.sp,
            textAlign = TextAlign.Start,
            color = titleColor,
            fontFamily = Roboto,
            letterSpacing = 0.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        //TODO customize filed height
        OutlinedTextField(
            enabled = enabled,
            value = fieldText,
            onValueChange = { onValueChange(it) },
            placeholder = {
                Text(
                    placeHolderText,
                    color = LightGreyText,
                    fontFamily = Roboto,
                    fontSize = 15.sp,
                    lineHeight = 24.sp
                )
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = LightGreyBorder,
                focusedBorderColor = LightBlueBorder,
                focusedTextColor = BlackTitle,
                cursorColor = LightBlueBorder,
                selectionColors = TextSelectionColors(
                    handleColor = LightBlueBorder,
                    backgroundColor = Color.LightGray
                ),
                errorBorderColor = ErrorBorderColor,
            ),
            keyboardOptions = keyboardOptions,
            modifier = Modifier.fillMaxWidth(),
            supportingText = {
                if (isFieldError) {
                    TextFieldErrorMessage(fieldErrorText)
                }
            },
            visualTransformation = visualTransformation,
            isError = isFieldError,
        )
    }
}