package com.telegram.bot.utils

import kotlin.math.min

/**
 * Object that contains methods for paging.
 */
object Paging {

    const val ITEMS_PER_PAGE = 10

    /**
     * Returns page of the list.
     */
    fun <T> List<T>.getPage(page: Int, pageSize: Int = ITEMS_PER_PAGE): List<T> {
        if (pageSize <= 0 || page <= 0) {
            throw RuntimeException("Invalid page parameter")
        }

        val offset = (page - 1) * pageSize
        if (this.size <= offset) {
            throw RuntimeException("Page out of bounds")
        }
        return this.subList(offset, min(offset + pageSize, this.size))
    }
}