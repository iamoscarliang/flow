package com.oscarliang.flow.ui.search

import android.content.Context
import android.os.Bundle
import android.os.IBinder
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.material.snackbar.Snackbar
import com.oscarliang.flow.R
import com.oscarliang.flow.databinding.FragmentSearchBinding
import com.oscarliang.flow.databinding.LayoutAdSmallBinding
import com.oscarliang.flow.ui.common.ClickListener
import com.oscarliang.flow.ui.common.NewsSmallAdListAdapter
import com.oscarliang.flow.util.NEWS_PER_PAGE_COUNT
import com.oscarliang.flow.util.autoCleared
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    var binding by autoCleared<FragmentSearchBinding>()
    private val viewModel by viewModel<SearchViewModel>()
    private var newsAdapter by autoCleared<NewsSmallAdListAdapter>()

    private val adBuilder by inject<AdLoader.Builder>()
    private val adRequest by inject<AdRequest>()
    private val ads = ArrayList<NativeAd>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dataBinding = FragmentSearchBinding.inflate(
            inflater,
            container,
            false
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
        binding.lifecycleOwner = viewLifecycleOwner
        binding.searchResults = viewModel.searchResults

        this.newsAdapter = NewsSmallAdListAdapter(
            itemClickListener = {
                findNavController()
                    .navigate(
                        SearchFragmentDirections.actionToNewsDetailFragment(
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
                if (isDetached
                    || activity == null
                    || requireActivity().isDestroyed
                    || requireActivity().isFinishing
                    || requireActivity().isChangingConfigurations
                ) {
                    nativeAd.destroy()
                    return@NewsSmallAdListAdapter null
                }
                ads.add(nativeAd)
                LayoutAdSmallBinding.inflate(layoutInflater)
            },
            adBuilder = adBuilder,
            adRequest = adRequest
        )
        binding.newsList.apply {
            adapter = newsAdapter
            itemAnimator?.changeDuration = 0
        }
        binding.listener = object : ClickListener {
            override fun onClick() {
                viewModel.retry()
            }
        }
        initRecyclerView()
        initSearchInputListener()
    }

    private fun initRecyclerView() {
        viewModel.searchResults.observe(viewLifecycleOwner) { result ->
            newsAdapter.submitList(result?.data)
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

    private fun initSearchInputListener() {
        binding.editSearch.setOnEditorActionListener { view: View, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch(view)
                true
            } else {
                false
            }
        }
        binding.editSearch.setOnKeyListener { view: View, keyCode: Int, event: KeyEvent ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                doSearch(view)
                true
            } else {
                false
            }
        }
    }

    private fun doSearch(v: View) {
        dismissKeyboard(v.windowToken)
        val query = binding.editSearch.text.toString()
        if (query.isBlank()) {
            val msg = getString(R.string.empty_search)
            Snackbar.make(binding.coordinatorLayout, msg, Snackbar.LENGTH_LONG).show()
        } else {
            viewModel.setQuery(query, NEWS_PER_PAGE_COUNT)
        }
    }

    private fun dismissKeyboard(windowToken: IBinder) {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(windowToken, 0)
    }

}