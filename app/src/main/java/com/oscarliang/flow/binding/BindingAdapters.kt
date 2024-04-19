package com.oscarliang.flow.binding

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.oscarliang.flow.R

object BindingAdapters {

    @JvmStatic
    @BindingAdapter("showHide")
    fun showHide(view: View, show: Boolean) {
        view.isVisible = show
    }

    @JvmStatic
    @BindingAdapter("refreshHide")
    fun refreshHide(view: SwipeRefreshLayout, isRefreshing: Boolean) {
        if (view.isRefreshing) {
            view.isRefreshing = isRefreshing
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["imageUrl"])
    fun loadImage(imageView: ImageView, url: String?) {
        Glide.with(imageView.context)
            .load(url)
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_error)
            .into(imageView)
    }

}
