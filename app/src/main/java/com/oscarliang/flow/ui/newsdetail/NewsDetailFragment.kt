package com.oscarliang.flow.ui.newsdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.oscarliang.flow.databinding.FragmentNewsDetailBinding
import com.oscarliang.flow.databinding.LayoutAdMediumBinding
import com.oscarliang.flow.model.News
import com.oscarliang.flow.ui.common.ClickListener
import com.oscarliang.flow.ui.common.ItemClickListener
import com.oscarliang.flow.ui.common.NewsMediumAdListAdapter
import com.oscarliang.flow.ui.news.NewsFragmentDirections
import com.oscarliang.flow.util.autoCleared
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewsDetailFragment : Fragment() {

    var binding by autoCleared<FragmentNewsDetailBinding>()
    private val viewModel by viewModel<NewsDetailViewModel>()
    private var newsAdapter by autoCleared<NewsMediumAdListAdapter>()
    private val params by navArgs<NewsDetailFragmentArgs>()

    private val adBuilder by inject<AdLoader.Builder>()
    private val adRequest by inject<AdRequest>()
    private val ads = ArrayList<NativeAd>()

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
        // Must call destroy on old ads, otherwise we will have a memory leak
        ads.forEach {
            it.destroy()
        }
        ads.clear()
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.setNewsId(params.newsId)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.news = viewModel.news
        binding.moreNews = viewModel.moreNews
        this.newsAdapter = NewsMediumAdListAdapter(
            itemClickListener = {
                findNavController()
                    .navigate(
                        NewsFragmentDirections.actionToNewsDetailFragment(
                            it.id
                        )
                    )
            },
            bookmarkClickListener = {
                viewModel.toggleBookmark(it)
            },
            adLoadListener = { nativeAd ->
                // If this callback occurs after the activity is destroyed, we must call
                // destroy and return or we may get a memory leak
                if (requireActivity().isDestroyed
                    || requireActivity().isFinishing
                    || requireActivity().isChangingConfigurations
                ) {
                    nativeAd.destroy()
                    return@NewsMediumAdListAdapter null
                }
                ads.add(nativeAd)
                LayoutAdMediumBinding.inflate(layoutInflater)
            },
            adBuilder = adBuilder,
            adRequest = adRequest
        )
        binding.moreNewsList.apply {
            adapter = newsAdapter
            itemAnimator?.changeDuration = 0
        }
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
        binding.retryListener = object : ClickListener {
            override fun onClick() {
                viewModel.retry()
            }
        }
        initRecyclerView()
    }

    private fun initRecyclerView() {
        viewModel.moreNews.observe(viewLifecycleOwner) { result ->
            // Ignore the loading state, since more news won't be updated
            result?.data?.let {
                newsAdapter.submitList(it)
            }
        }
    }

}