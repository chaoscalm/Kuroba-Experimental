package com.github.k1rakishou.chan.features.toolbar.state.thread

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.github.k1rakishou.chan.features.toolbar.ToolbarMenu
import com.github.k1rakishou.chan.features.toolbar.ToolbarMenuItem
import com.github.k1rakishou.chan.features.toolbar.ToolbarText
import com.github.k1rakishou.chan.features.toolbar.state.IKurobaToolbarParams
import com.github.k1rakishou.chan.features.toolbar.state.KurobaToolbarSubState
import com.github.k1rakishou.chan.features.toolbar.state.ToolbarContentState
import com.github.k1rakishou.chan.features.toolbar.state.ToolbarStateKind

@Immutable
data class KurobaThreadToolbarParams(
  val leftItem: ToolbarMenuItem? = null,
  val title: ToolbarText? = null,
  val subtitle: ToolbarText? = null,
  val scrollableTitle: Boolean = false,
  val toolbarMenu: ToolbarMenu? = null,
  val iconClickInterceptor: ((ToolbarMenuItem) -> Boolean)? = null
) : IKurobaToolbarParams {
  override val kind: ToolbarStateKind = ToolbarStateKind.Thread
}

class KurobaThreadToolbarSubState(
  params: KurobaThreadToolbarParams = KurobaThreadToolbarParams()
) : KurobaToolbarSubState() {
  private val _leftItem = mutableStateOf<ToolbarMenuItem?>(params.leftItem)
  val leftItem: State<ToolbarMenuItem?>
    get() = _leftItem

  private val _title = mutableStateOf<ToolbarText?>(params.title)
  val title: State<ToolbarText?>
    get() = _title

  private val _subtitle = mutableStateOf<ToolbarText?>(params.subtitle)
  val subtitle: State<ToolbarText?>
    get() = _subtitle

  private val _scrollableTitle = mutableStateOf<Boolean>(params.scrollableTitle)
  val scrollableTitle: State<Boolean>
    get() = _scrollableTitle

  private val _toolbarMenu = mutableStateOf<ToolbarMenu?>(params.toolbarMenu)
  val toolbarMenu: State<ToolbarMenu?>
    get() = _toolbarMenu

  private var _iconClickInterceptor: ((ToolbarMenuItem) -> Boolean)? = params.iconClickInterceptor
  val iconClickInterceptor: ((ToolbarMenuItem) -> Boolean)?
    get() = _iconClickInterceptor

  private val _toolbarContentState = mutableStateOf<ToolbarContentState>(ToolbarContentState.Empty)
  val toolbarContentState: State<ToolbarContentState>
    get() = _toolbarContentState

  override val kind: ToolbarStateKind = params.kind

  override val leftMenuItem: ToolbarMenuItem?
    get() = _leftItem.value

  override val rightToolbarMenu: ToolbarMenu?
    get() = _toolbarMenu.value

  override fun update(params: IKurobaToolbarParams) {
    params as KurobaThreadToolbarParams

    _leftItem.value = params.leftItem
    _title.value = params.title
    _subtitle.value = params.subtitle
    _scrollableTitle.value = params.scrollableTitle
    _toolbarMenu.value = params.toolbarMenu
    _iconClickInterceptor = params.iconClickInterceptor
  }

  fun updateTitle(
    newTitle: ToolbarText? = _title.value,
    newSubTitle: ToolbarText? = _subtitle.value
  ) {
    _title.value = newTitle
    _subtitle.value = newSubTitle
  }

  fun updateToolbarContentState(state: ToolbarContentState) {
    _toolbarContentState.value = state
  }

  override fun toString(): String {
    return "KurobaThreadToolbarSubState(title: '${_title.value}', subtitle: '${_subtitle.value}')"
  }

}