package com.oscarliang.flow.ui.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.oscarliang.flow.databinding.FragmentNewsBinding
import com.oscarliang.flow.ui.common.CategoryListAdapter
import com.oscarliang.flow.ui.common.ClickListener
import com.oscarliang.flow.ui.common.LatestNewsListAdapter
import com.oscarliang.flow.ui.common.NewsListAdapter
import com.oscarliang.flow.util.TimeConverter.getTimePassBy
import com.oscarliang.flow.util.autoCleared
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.abs
import kotlin.math.max

class NewsFragment : Fragment() {

    var binding by autoCleared<FragmentNewsBinding>()
    private val viewModel by viewModel<NewsViewModel>()
    private var latestNewsAdapter by autoCleared<LatestNewsListAdapter>()
    private var newsAdapter by autoCleared<NewsListAdapter>()
    private var categoryAdapter by autoCleared<CategoryListAdapter>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dataBinding = FragmentNewsBinding.inflate(
            inflater,
            container,
            false
        )
        binding = dataBinding
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.setQuery(getTimePassBy(24), 10)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.latestNews = viewModel.latestNews
        binding.news = viewModel.news

        this.latestNewsAdapter = LatestNewsListAdapter(
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
            }
        )
        this.newsAdapter = NewsListAdapter(
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
            }
        )
        this.categoryAdapter = CategoryListAdapter(
            itemClickListener = {
                viewModel.selectCategory(it)
            }
        )
        binding.latestNewsList.adapter = latestNewsAdapter
        binding.categoryList.adapter = categoryAdapter
        binding.newsList.apply {
            adapter = newsAdapter
            itemAnimator?.changeDuration = 0
        }
        binding.listener = object : ClickListener {
            override fun onClick() {
                viewModel.refresh()
            }
        }
        binding.dotsIndicator.attachTo(binding.latestNewsList)
        binding.appbar.addOnOffsetChangedListener { _, verticalOffset ->
            binding.swipeRefreshLayout.isEnabled = verticalOffset == 0
        }
        initRecyclerView()
        initViewPagerAnimation()
    }

    private fun initRecyclerView() {
        viewModel.latestNews.observe(viewLifecycleOwner) { result ->
            latestNewsAdapter.submitList(result?.data)
        }
        viewModel.news.observe(viewLifecycleOwner) { result ->
            newsAdapter.submitList(result?.data)
        }
        viewModel.categories.observe(viewLifecycleOwner) { result ->
            categoryAdapter.submitList(result?.data)
            result?.data?.let {
                val selected = it.find { data -> data.isSelected }
                binding.categoryList.scrollToPosition(it.indexOf(selected))
            }
        }

        binding.nestedScrollView.setOnScrollChangeListener { view: NestedScrollView, _: Int, scrollY: Int, _: Int, _: Int ->
            // Check is scroll to bottom
            if (scrollY == view.getChildAt(0).measuredHeight - view.measuredHeight) {
                viewModel.loadNextPage()
            }
        }
        viewModel.loadMoreState.observe(viewLifecycleOwner) { state ->
            if (state == null) {
                binding.isRunning = false
                binding.hasMore = false
            } else {
                binding.isRunning = state.isRunning
                binding.hasMore = state.hasMore
                val error = state.errorMessageIfNotHandled
                if (error != null) {
                    Snackbar.make(binding.coordinatorLayout, error, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun initViewPagerAnimation() {
        binding.latestNewsList.setPageTransformer { view, position ->
            view.apply {
                val minScale = 0.85f
                val minAlpha = 0.5f
                val pageWidth = width
                val pageHeight = height
                when {
                    // [-Infinity,-1)
                    position < -1 -> {
                        // This page is way off-screen to the left
                        alpha = 0f
                    }

                    // [-1,1]
                    position <= 1 -> {
                        // Modify the default slide transition to shrink the page as well
                        val scaleFactor = max(minScale, 1 - abs(position))
                        val verticalMargin = pageHeight * (1 - scaleFactor) / 2
                        val horizontalMargin = pageWidth * (1 - scaleFactor) / 2
                        translationX = if (position < 0) {
                            horizontalMargin - verticalMargin / 2
                        } else {
                            horizontalMargin + verticalMargin / 2
                        }

                        // Scale the page down (between MIN_SCALE and 1)
                        scaleX = scaleFactor
                        scaleY = scaleFactor

                        // Fade the page relative to its size.
                        alpha = (minAlpha +
                                (((scaleFactor - minScale) / (1 - minScale)) * (1 - minAlpha)))
                    }

                    // (1,+Infinity]
                    else -> {
                        // This page is way off-screen to the right
                        alpha = 0f
                    }
                }
            }
        }
        // Disable the blink effect when item updated
        for (i in 0 until binding.latestNewsList.childCount) {
            val view = binding.latestNewsList.getChildAt(i)
            if (view is RecyclerView) {
                view.itemAnimator?.changeDuration = 0
                return
            }
        }
    }

}