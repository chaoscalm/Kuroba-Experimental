package com.github.k1rakishou.chan.ui.compose.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.k1rakishou.chan.ui.compose.KurobaTextUnit
import com.github.k1rakishou.chan.ui.compose.animatedHorizontalLine
import com.github.k1rakishou.chan.ui.compose.collectTextFontSize
import com.github.k1rakishou.chan.ui.compose.providers.LocalChanTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun KurobaComposeTextFieldV2(
  state: TextFieldState,
  modifier: Modifier = Modifier,
  fontSize: KurobaTextUnit = KurobaTextUnit(16.sp),
  enabled: Boolean = true,
  readOnly: Boolean = false,
  isError: Boolean = false,
  inputTransformation: InputTransformation? = null,
  textStyle: TextStyle = TextStyle.Default,
  shape: Shape = TextFieldDefaults.TextFieldShape,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  onKeyboardAction: KeyboardActionHandler? = null,
  lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
  onTextLayout: (Density.(getResult: () -> TextLayoutResult?) -> Unit)? = null,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  outputTransformation: OutputTransformation? = null,
  label: @Composable ((InteractionSource) -> Unit)? = null,
  scrollState: ScrollState = rememberScrollState(),
) {
  val chanTheme = LocalChanTheme.current
  val view = LocalView.current

  DisposableEffect(
    key1 = view,
    effect = {
      if (view.isAttachedToWindow) {
        view.requestApplyInsets()
      }

      onDispose {
        if (view.isAttachedToWindow) {
          view.requestApplyInsets()
        }
      }
    }
  )

  val textSelectionColors = remember(key1 = chanTheme.accentColor) {
    TextSelectionColors(
      handleColor = if (lineLimits is TextFieldLineLimits.SingleLine) {
        Color.Transparent
      } else {
        chanTheme.accentColorCompose
      },
      backgroundColor = chanTheme.accentColorCompose.copy(alpha = 0.4f)
    )
  }

  CompositionLocalProvider(LocalTextSelectionColors provides textSelectionColors) {
    val textFontSize = collectTextFontSize(defaultFontSize = fontSize)
    val colors = chanTheme.textFieldColors()
    val textColor = textStyle.color.takeOrElse { colors.textColor(enabled).value }

    val mergedTextStyle = remember(textStyle, textColor, textFontSize) {
      textStyle.merge(TextStyle(color = textColor, fontSize = textFontSize))
    }

    val isFocused by interactionSource.collectIsFocusedAsState()
    val backgroundColor by colors.backgroundColor(enabled)
    val cursorColor by colors.cursorColor(isError)

    BasicTextField(
      state = state,
      modifier = modifier
        .background(backgroundColor, shape)
        .animatedHorizontalLine(
          enabled = enabled,
          isError = false,
          isFocused = isFocused,
          lineWidth = 2.dp
        )
        .defaultMinSize(
          minWidth = KurobaComposeDefaults.TextField.MinWidth,
          minHeight = KurobaComposeDefaults.TextField.MinHeight
        ),
      enabled = enabled,
      readOnly = readOnly,
      inputTransformation = inputTransformation,
      textStyle = mergedTextStyle,
      keyboardOptions = keyboardOptions,
      onKeyboardAction = onKeyboardAction,
      lineLimits = lineLimits,
      onTextLayout = onTextLayout,
      interactionSource = interactionSource,
      cursorBrush = remember(cursorColor) { SolidColor(cursorColor) },
      outputTransformation = outputTransformation,
      decorator = @Composable { innerTextField ->
        val labelFunc: (@Composable (() -> Unit))? = if (label == null) {
          null
        } else {
          { label(interactionSource) }
        }

        TextFieldDefaults.TextFieldDecorationBox(
          value = remember(state.text) { state.text.toString() },
          visualTransformation = VisualTransformation.None,
          innerTextField = innerTextField,
          placeholder = null,
          label = labelFunc,
          leadingIcon = null,
          trailingIcon = null,
          singleLine = lineLimits is TextFieldLineLimits.SingleLine,
          enabled = enabled,
          isError = false,
          interactionSource = interactionSource,
          colors = colors,
          contentPadding = remember { PaddingValues(4.dp) }
        )
      },
      scrollState = scrollState
    )
  }
}