package com.oscarliang.flow.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.oscarliang.flow.databinding.LayoutAdSmallBinding
import com.oscarliang.flow.databinding.LayoutNewsItemBinding
import com.oscarliang.flow.model.News

private const val NEWS_VIEW_TYPE = 0
private const val AD_VIEW_TYPE = 1
private const val ITEMS_PER_AD = 10

class NewsListAdapter(
    private val itemClickListener: ((News) -> Unit)?,
    private val bookmarkClickListener: ((News) -> Unit)?,
    private val adLoadListener: ((NativeAd) -> LayoutAdSmallBinding?)? = null,
    private val adBuilder: AdLoader.Builder? = null,
    private val adRequest: AdRequest? = null
) : DataBoundListAdapter<News, LayoutNewsItemBinding>(
    diffCallback = object : DiffUtil.ItemCallback<News>() {
        override fun areItemsTheSame(oldItem: News, newItem: News): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: News, newItem: News): Boolean {
            return oldItem == newItem
        }
    }
) {

    override fun getItemViewType(position: Int): Int {
        return if (adLoadListener != null && position % ITEMS_PER_AD == 4) {
            AD_VIEW_TYPE
        } else {
            NEWS_VIEW_TYPE
        }
    }

    override fun createBinding(parent: ViewGroup, viewType: Int): LayoutNewsItemBinding {
        val binding = LayoutNewsItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        binding.cardView.setOnClickListener {
            binding.news?.let {
                itemClickListener?.invoke(it)
            }
        }
        binding.btnBookmark.setOnClickListener {
            binding.news?.let {
                bookmarkClickListener?.invoke(it)
            }
        }
        if (viewType == AD_VIEW_TYPE) {
            initNativeAd(binding)
        }
        return binding
    }

    override fun bind(binding: LayoutNewsItemBinding, item: News) {
        binding.news = item
    }

    private fun initNativeAd(binding: LayoutNewsItemBinding) {
        val adLoader = adBuilder?.forNativeAd { nativeAd ->
            val adBinding = adLoadListener?.invoke(nativeAd)
            adBinding?.let {
                bindNativeAd(nativeAd, it)
                // Remove the previous ads and add new one
                binding.frameLayout.isVisible = true
                binding.frameLayout.removeAllViews()
                binding.frameLayout.addView(it.root)
            }
        }?.build()
        adLoader?.loadAd(adRequest!!)
    }

    private fun bindNativeAd(ad: NativeAd, adBinding: LayoutAdSmallBinding) {
        adBinding.ad = ad
        adBinding.nativeAdView.headlineView = adBinding.textAdHeadline
        adBinding.nativeAdView.starRatingView = adBinding.ratingBar
        adBinding.nativeAdView.iconView = adBinding.imageAdIcon
        adBinding.nativeAdView.callToActionView = adBinding.btnAdCta
        adBinding.nativeAdView.setNativeAd(ad)
    }

}