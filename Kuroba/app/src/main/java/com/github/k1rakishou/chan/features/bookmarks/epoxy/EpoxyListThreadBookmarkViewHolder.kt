package com.github.k1rakishou.chan.features.bookmarks.epoxy

import android.content.Context
import androidx.appcompat.widget.AppCompatImageView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.github.k1rakishou.chan.Chan
import com.github.k1rakishou.chan.R
import com.github.k1rakishou.chan.features.bookmarks.data.ThreadBookmarkSelection
import com.github.k1rakishou.chan.features.bookmarks.data.ThreadBookmarkStats
import com.github.k1rakishou.chan.ui.theme.ThemeEngine
import com.github.k1rakishou.model.data.descriptor.ChanDescriptor
import javax.inject.Inject

@EpoxyModelClass(layout = R.layout.epoxy_list_thread_bookmark_view)
abstract class EpoxyListThreadBookmarkViewHolder
  : EpoxyModelWithHolder<BaseThreadBookmarkViewHolder>(), ThemeEngine.ThemeChangesListener, UnifiedBookmarkInfoAccessor {

  @Inject
  lateinit var themeEngine: ThemeEngine

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var imageLoaderRequestData: BaseThreadBookmarkViewHolder.ImageLoaderRequestData? = null
  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var threadDescriptor: ChanDescriptor.ThreadDescriptor? = null
  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var bookmarkClickListener: ((ChanDescriptor.ThreadDescriptor) -> Unit)? = null
  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var bookmarkLongClickListener: ((ChanDescriptor.ThreadDescriptor) -> Unit)? = null
  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var bookmarkStatsClickListener: ((ChanDescriptor.ThreadDescriptor) -> Unit)? = null
  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var context: Context? = null

  @EpoxyAttribute
  var threadBookmarkStats: ThreadBookmarkStats? = null
  @EpoxyAttribute
  var threadBookmarkSelection: ThreadBookmarkSelection? = null
  @EpoxyAttribute
  var titleString: String? = null
  @EpoxyAttribute
  var highlightBookmark: Boolean = false
  @EpoxyAttribute
  open var isTablet: Boolean = false
  @EpoxyAttribute
  var groupId: String? = null
  @EpoxyAttribute
  var moveBookmarksWithUnreadRepliesToTop: Boolean = false
  @EpoxyAttribute
  var moveNotActiveBookmarksToBottom: Boolean = false
  @EpoxyAttribute
  var viewThreadBookmarksGridMode: Boolean = false

  private var holder: BaseThreadBookmarkViewHolder? = null
  var dragIndicator: AppCompatImageView? = null

  override fun getBookmarkGroupId(): String? = groupId
  override fun getBookmarkStats(): ThreadBookmarkStats? = threadBookmarkStats
  override fun getBookmarkDescriptor(): ChanDescriptor.ThreadDescriptor? = threadDescriptor

  override fun bind(holder: BaseThreadBookmarkViewHolder) {
    super.bind(holder)
    Chan.inject(this)

    this.holder = holder
    this.dragIndicator = holder.dragIndicator

    holder.setImageLoaderRequestData(imageLoaderRequestData)
    holder.setDescriptor(threadDescriptor)
    holder.bookmarkSelection(threadBookmarkSelection)
    holder.bookmarkClickListener(bookmarkClickListener)
    holder.bookmarkLongClickListener(bookmarkLongClickListener)
    holder.bookmarkStatsClickListener(false, bookmarkStatsClickListener)
    holder.setThreadBookmarkStats(false, threadBookmarkStats)
    holder.setTitle(titleString, threadBookmarkStats?.watching ?: false)
    holder.highlightBookmark(highlightBookmark || threadBookmarkSelection?.isSelected == true)
    holder.updateListViewSizes(isTablet)
    holder.updateDragIndicatorColors(false)
    holder.updateDragIndicatorState(threadBookmarkStats)

    val watching = threadBookmarkStats?.watching ?: true
    context?.let { holder.bindImage(false, watching, it) }

    themeEngine.addListener(this)
  }

  override fun unbind(holder: BaseThreadBookmarkViewHolder) {
    super.unbind(holder)

    themeEngine.removeListener(this)
    holder.unbind()

    this.holder = null
  }

  override fun onThemeChanged() {
    holder?.apply {
      setThreadBookmarkStats(true, threadBookmarkStats)
      setTitle(titleString, threadBookmarkStats?.watching ?: false)
      highlightBookmark(highlightBookmark)
      updateDragIndicatorColors(false)
    }
  }

  override fun createNewHolder(): BaseThreadBookmarkViewHolder {
    return BaseThreadBookmarkViewHolder(
      context!!.resources.getDimension(R.dimen.thread_list_bookmark_view_image_size).toInt()
    )
  }

}