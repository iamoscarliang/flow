package com.oscarliang.flow.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.oscarliang.flow.databinding.LayoutAdSmallBinding
import com.oscarliang.flow.databinding.LayoutNewsAdSmallItemBinding
import com.oscarliang.flow.model.News

class NewsSmallAdListAdapter(
    private val itemClickListener: ((News) -> Unit)?,
    private val bookmarkClickListener: ((News) -> Unit)?,
    private val adLoadListener: (NativeAd) -> LayoutAdSmallBinding?,
    private val adBuilder: AdLoader.Builder,
    private val adRequest: AdRequest
) : DataBoundListAdapter<News, LayoutNewsAdSmallItemBinding>(
    object : DiffUtil.ItemCallback<News>() {
        override fun areItemsTheSame(oldItem: News, newItem: News): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: News, newItem: News): Boolean {
            return oldItem == newItem
        }
    }
) {

    override fun getItemViewType(position: Int): Int {
        return if (position % ITEMS_PER_AD == 4) {
            AD_VIEW_TYPE
        } else {
            NEWS_VIEW_TYPE
        }
    }

    override fun createBinding(parent: ViewGroup, viewType: Int): LayoutNewsAdSmallItemBinding {
        val binding = LayoutNewsAdSmallItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        binding.layoutNewsItem.root.setOnClickListener {
            binding.news?.let {
                itemClickListener?.invoke(it)
            }
        }
        binding.layoutNewsItem.btnBookmark.setOnClickListener {
            binding.news?.let {
                bookmarkClickListener?.invoke(it)
            }
        }
        if (viewType == AD_VIEW_TYPE) {
            initNativeAd(binding)
        }
        return binding
    }

    override fun bind(binding: LayoutNewsAdSmallItemBinding, item: News) {
        binding.news = item
    }

    private fun initNativeAd(binding: LayoutNewsAdSmallItemBinding) {
        val adLoader = adBuilder.forNativeAd { nativeAd ->
            val adBinding = adLoadListener(nativeAd)
            adBinding?.let {
                bindNativeAd(nativeAd, it)
                // Remove the previous ads and add new one
                binding.frameLayout.isVisible = true
                binding.frameLayout.removeAllViews()
                binding.frameLayout.addView(it.root)
            }
        }.build()
        adLoader.loadAd(adRequest)
    }

    private fun bindNativeAd(ad: NativeAd, adBinding: LayoutAdSmallBinding) {
        adBinding.ad = ad
        adBinding.nativeAdView.headlineView = adBinding.textAdHeadline
        adBinding.nativeAdView.starRatingView = adBinding.ratingBar
        adBinding.nativeAdView.iconView = adBinding.imageAdIcon
        adBinding.nativeAdView.callToActionView = adBinding.btnAdCta
        adBinding.nativeAdView.setNativeAd(ad)
    }

    companion object {
        private const val NEWS_VIEW_TYPE = 0
        private const val AD_VIEW_TYPE = 1
        private const val ITEMS_PER_AD = 10
    }

}