package com.oscarliang.flow.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.oscarliang.flow.databinding.LayoutCategoryItemBinding
import com.oscarliang.flow.model.Category

class CategoryListAdapter(
    private val itemClickListener: ((Category) -> Unit)?,
) : DataBoundListAdapter<Category, LayoutCategoryItemBinding>(
    diffCallback = object : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem
        }
    }
) {

    override fun createBinding(parent: ViewGroup): LayoutCategoryItemBinding {
        val binding = LayoutCategoryItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        binding.root.setOnClickListener {
            binding.category?.let {
                itemClickListener?.invoke(it)
            }
        }
        return binding
    }

    override fun bind(binding: LayoutCategoryItemBinding, item: Category) {
        binding.category = item
    }

}