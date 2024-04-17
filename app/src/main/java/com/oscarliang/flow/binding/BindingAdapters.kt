package com.oscarliang.flow.binding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.oscarliang.flow.R

object BindingAdapters {

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
