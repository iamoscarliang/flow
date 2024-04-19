package com.oscarliang.flow.ui.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.oscarliang.flow.databinding.FragmentNewsBinding
import com.oscarliang.flow.ui.common.CategoryListAdapter
import com.oscarliang.flow.ui.common.LatestNewsListAdapter
import com.oscarliang.flow.ui.common.NewsListAdapter
import com.oscarliang.flow.ui.common.RetryListener
import com.oscarliang.flow.util.TimeConverter.getTimePassBy
import com.oscarliang.flow.util.autoCleared
import org.koin.androidx.viewmodel.ext.android.viewModel

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

            },
            bookmarkClickListener = {
                viewModel.toggleBookmark(it)
            }
        )
        this.newsAdapter = NewsListAdapter(
            itemClickListener = {

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
        binding.latestNewsList.apply {
            adapter = latestNewsAdapter
            itemAnimator?.changeDuration = 0
        }
        binding.newsList.apply {
            adapter = newsAdapter
            itemAnimator?.changeDuration = 0
        }
        binding.categoryList.adapter = categoryAdapter
        binding.listener = object : RetryListener {
            override fun retry() {
                viewModel.refresh()
            }
        }
        binding.appbar.addOnOffsetChangedListener { _, verticalOffset ->
            binding.swipeRefreshLayout.isEnabled = verticalOffset == 0
        }
        initRecyclerView()
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
                    Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

}