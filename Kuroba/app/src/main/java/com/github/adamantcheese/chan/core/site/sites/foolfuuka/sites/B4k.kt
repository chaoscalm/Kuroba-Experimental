package com.github.adamantcheese.chan.core.site.sites.foolfuuka.sites

import com.github.adamantcheese.chan.core.site.Site
import com.github.adamantcheese.chan.core.site.SiteIcon
import com.github.adamantcheese.chan.core.site.common.FoolFuukaCommentParser
import com.github.adamantcheese.chan.core.site.sites.foolfuuka.FoolFuukaActions
import com.github.adamantcheese.chan.core.site.sites.foolfuuka.FoolFuukaApi
import com.github.adamantcheese.chan.core.site.sites.foolfuuka.FoolFuukaEndpoints
import com.github.adamantcheese.common.DoNotStrip
import com.github.adamantcheese.model.data.archive.ArchiveType
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

@DoNotStrip
class B4k : BaseFoolFuukaSite() {

  override fun rootUrl(): HttpUrl = ROOT_URL

  override fun setup() {
    setEnabled(true)
    setName(SITE_NAME)
    setIcon(SiteIcon.fromFavicon(imageLoaderV2, FAVICON_URL))
    setBoardsType(Site.BoardsType.INFINITE)
    setResolvable(URL_HANDLER)
    setConfig(object : CommonConfig() {})
    setEndpoints(FoolFuukaEndpoints(this, rootUrl()))
    setActions(FoolFuukaActions(this))
    setApi(FoolFuukaApi(this))
    setParser(FoolFuukaCommentParser(mockReplyManager, archivesManager))
  }

  companion object : FoolFuukaSiteStatic {
    init {
      foolFuukaSiteStatic = this
    }

    override val FAVICON_URL: HttpUrl
      get() = "https://b4k.co/assets/favicons/luna-alt.png".toHttpUrl()
    override val ROOT: String
      get() = "https://arch.b4k.co/"
    override val ROOT_URL: HttpUrl
      get() =  ROOT.toHttpUrl()
    override val SITE_NAME: String
      get() = ArchiveType.B4k.domain
    override val MEDIA_HOSTS: Array<String>
      get() = arrayOf(ROOT_URL.toString())
    override val NAMES: Array<String>
      get() = arrayOf("b4k")
    override val CLASS: Class<out Site>
      get() = B4k::class.java
  }

}