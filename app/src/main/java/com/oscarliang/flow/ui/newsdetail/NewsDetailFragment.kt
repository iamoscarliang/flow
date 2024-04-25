package com.oscarliang.flow.ui.newsdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.oscarliang.flow.databinding.FragmentNewsDetailBinding
import com.oscarliang.flow.databinding.LayoutAdMediumBinding
import com.oscarliang.flow.model.News
import com.oscarliang.flow.ui.common.ClickListener
import com.oscarliang.flow.ui.common.ItemClickListener
import com.oscarliang.flow.util.autoCleared
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewsDetailFragment : Fragment() {

    var binding by autoCleared<FragmentNewsDetailBinding>()

    private val viewModel by viewModel<NewsDetailViewModel>()
    private val params by navArgs<NewsDetailFragmentArgs>()
    private var currentNativeAd: NativeAd? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dataBinding = FragmentNewsDetailBinding.inflate(
            inflater,
            container,
            false,
        )
        binding = dataBinding
        return dataBinding.root
    }

    override fun onDestroyView() {
        currentNativeAd?.destroy()
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.setNewsId(params.newsId)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.news = viewModel.news
        binding.backListener = object : ClickListener {
            override fun onClick() {
                NavHostFragment.findNavController(this@NewsDetailFragment).navigateUp()
            }
        }
        binding.bookmarkListener = object : ItemClickListener<News> {
            override fun onClick(item: News) {
                viewModel.toggleBookmark(item)
            }
        }
        initAds()
    }

    private fun initAds() {
        val adLoader = AdLoader.Builder(requireContext(), "ca-app-pub-3940256099942544/2247696110")
            .forNativeAd { nativeAd ->
                // If this callback occurs after the activity is destroyed, you must call
                // destroy and return or you may get a memory leak
                if (isDetached
                    || requireActivity().isDestroyed
                    || requireActivity().isFinishing
                    || requireActivity().isChangingConfigurations
                ) {
                    nativeAd.destroy()
                    return@forNativeAd
                }

                // Must call destroy on old ads, otherwise you will have a memory leak
                currentNativeAd?.destroy()
                currentNativeAd = nativeAd

                // Inflate the ads layout and display the ads
                val adBinding = LayoutAdMediumBinding.inflate(layoutInflater, null, false)
                displayNativeAd(nativeAd, adBinding)

                // Remove the previous ads and add new one
                binding.frameLayout.removeAllViews()
                binding.frameLayout.addView(adBinding.root)
            }.build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun displayNativeAd(ad: NativeAd, adBinding: LayoutAdMediumBinding) {
        adBinding.ad = ad
        adBinding.nativeAdView.headlineView = adBinding.textAdHeadline
        adBinding.nativeAdView.bodyView = adBinding.textAdBody
        adBinding.nativeAdView.starRatingView = adBinding.ratingBar
        adBinding.nativeAdView.iconView = adBinding.imageAdIcon
        adBinding.nativeAdView.callToActionView = adBinding.btnAdCta
        adBinding.nativeAdView.mediaView = adBinding.mediaView
        adBinding.nativeAdView.setNativeAd(ad)
    }

}