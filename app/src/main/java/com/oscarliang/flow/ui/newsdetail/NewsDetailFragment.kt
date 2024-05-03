package com.oscarliang.flow.ui.newsdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.oscarliang.flow.R
import com.oscarliang.flow.databinding.FragmentNewsDetailBinding
import com.oscarliang.flow.model.News
import com.oscarliang.flow.ui.common.ClickListener
import com.oscarliang.flow.ui.common.ItemClickListener
import com.oscarliang.flow.ui.common.MenuItemClickListener
import com.oscarliang.flow.ui.common.NewsListAdapter
import com.oscarliang.flow.util.autoCleared
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewsDetailFragment : Fragment() {

    var binding by autoCleared<FragmentNewsDetailBinding>()
    private val viewModel by viewModel<NewsDetailViewModel>()
    private var newsAdapter by autoCleared<NewsListAdapter>()
    private val params by navArgs<NewsDetailFragmentArgs>()

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.setNewsId(params.newsId)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.news = viewModel.news
        binding.moreNews = viewModel.moreNews
        this.newsAdapter = NewsListAdapter(
            itemClickListener = {
                findNavController()
                    .navigate(
                        NewsDetailFragmentDirections.actionToNewsDetailFragment(
                            it.id
                        )
                    )
            },
            bookmarkClickListener = {
                viewModel.toggleBookmark(it)
            }
        )
        binding.moreNewsList.apply {
            adapter = newsAdapter
            itemAnimator?.changeDuration = 0
        }
        binding.backListener = object : ClickListener {
            override fun onClick() {
                findNavController().navigateUp()
            }
        }
        binding.toolbar.inflateMenu(R.menu.menu_browser)
        binding.browserListener = object : MenuItemClickListener<News> {
            override fun onClick(item: News): Boolean {
                findNavController().navigate(
                    NewsDetailFragmentDirections.actionNewsDetailFragmentToBrowserFragment(
                        item.url
                    )
                )
                return true
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