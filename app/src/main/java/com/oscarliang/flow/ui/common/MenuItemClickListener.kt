package com.oscarliang.flow.ui.common

interface MenuItemClickListener<T> {

    fun onClick(item: T): Boolean

}