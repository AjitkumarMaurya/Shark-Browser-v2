package ajit.browser.rocket.bhaskar

import ajit.browser.lite.partner.NewsItem

data class BhaskarItem(
    override val id: String,
    override val imageUrl: String?,
    override val title: String,
    override val newsUrl: String,
    override val time: Long,
    val summary: String?,
    val language: String?,
    override val category: String?,
    override val subcategory: String?,
    val keywords: String?,
    val description: String?,
    val tags: List<String>?,
    val articleFrom: String?,
    val province: String?,
    val city: String?
) : NewsItem {
    override val source: String = "DainikBhaskar.com"
    override val partner: String = "DainikBhaskar.com"
}