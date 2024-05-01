package com.oscarliang.flow.ui.bookmarks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.oscarliang.flow.databinding.FragmentBookmarksBinding
import com.oscarliang.flow.ui.common.NewsListAdapter
import com.oscarliang.flow.util.autoCleared
import org.koin.androidx.viewmodel.ext.android.viewModel

class BookmarksFragment : Fragment() {

    var binding by autoCleared<FragmentBookmarksBinding>()
    private val viewModel by viewModel<BookmarksViewModel>()
    private var newsAdapter by autoCleared<NewsListAdapter>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dataBinding = FragmentBookmarksBinding.inflate(
            inflater,
            container,
            false
        )
        binding = dataBinding
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.bookmarks = viewModel.bookmarks
        this.newsAdapter = NewsListAdapter(
            itemClickListener = {
                findNavController()
                    .navigate(
                        BookmarksFragmentDirections.actionToNewsDetailFragment(
                            it.id
                        )
                    )
            },
            bookmarkClickListener = {
                viewModel.toggleBookmark(it)
            }
        )
        binding.newsList.apply {
            adapter = newsAdapter
            itemAnimator?.changeDuration = 0
        }
        initRecyclerView()
    }

    private fun initRecyclerView() {
        viewModel.bookmarks.observe(viewLifecycleOwner) { result ->
            newsAdapter.submitList(result)
        }
    }

}