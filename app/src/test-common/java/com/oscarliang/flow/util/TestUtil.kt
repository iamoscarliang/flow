package com.oscarliang.flow.util

import com.oscarliang.flow.model.Category
import com.oscarliang.flow.model.News

private const val UNKNOWN_ID = "-1"

object TestUtil {

    fun createNews(
        count: Int,
        title: String,
        body: String,
        source: String
    ): List<News> {
        return (0 until count).map {
            createNews(
                id = it.toString(),
                title = title + it,
                body = body + it,
                source = source + it
            )
        }
    }

    fun createNews(
        title: String,
        body: String,
        source: String
    ) = createNews(
        id = UNKNOWN_ID,
        title = title,
        body = body,
        source = source
    )

    fun createNews(
        id: String,
        title: String,
        body: String,
        source: String
    ) = News(
        id = id,
        title = title,
        body = body,
        date = "",
        image = null,
        source = News.Source(source, source)
    )

    fun createCategories(
        count: Int,
        name: String
    ): List<Category> {
        return (0 until count).map {
            createCategory(
                id = it.toString(),
                name = name
            )
        }
    }

    fun createCategory(
        id: String,
        name: String
    ) = Category(
        id = id,
        name = name
    )

}