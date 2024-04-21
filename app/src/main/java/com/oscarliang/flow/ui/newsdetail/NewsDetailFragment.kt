package com.oscarliang.flow.ui.newsdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import com.oscarliang.flow.databinding.FragmentNewsDetailBinding
import com.oscarliang.flow.model.News
import com.oscarliang.flow.ui.common.ClickListener
import com.oscarliang.flow.ui.common.ItemClickListener
import com.oscarliang.flow.util.autoCleared
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewsDetailFragment : Fragment() {

    var binding by autoCleared<FragmentNewsDetailBinding>()

    private val viewModel by viewModel<NewsDetailViewModel>()
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
    }

}