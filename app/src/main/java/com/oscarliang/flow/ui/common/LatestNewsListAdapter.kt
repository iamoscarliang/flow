package com.oscarliang.flow.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.oscarliang.flow.databinding.LayoutLatestNewsItemBinding
import com.oscarliang.flow.model.News

class LatestNewsListAdapter(
    private val itemClickListener: ((News) -> Unit)?,
    private val bookmarkClickListener: ((News) -> Unit)?
) : DataBoundListAdapter<News, LayoutLatestNewsItemBinding>(
    diffCallback = object : DiffUtil.ItemCallback<News>() {
        override fun areItemsTheSame(oldItem: News, newItem: News): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: News, newItem: News): Boolean {
            return oldItem == newItem
        }
    }
) {

    override fun createBinding(parent: ViewGroup): LayoutLatestNewsItemBinding {
        val binding = LayoutLatestNewsItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        binding.root.setOnClickListener {
            binding.news?.let {
                itemClickListener?.invoke(it)
            }
        }
        binding.btnBookmark.setOnClickListener {
            binding.news?.let {
                bookmarkClickListener?.invoke(it)
            }
        }
        return binding
    }

    override fun bind(binding: LayoutLatestNewsItemBinding, item: News) {
        binding.news = item
    }

}