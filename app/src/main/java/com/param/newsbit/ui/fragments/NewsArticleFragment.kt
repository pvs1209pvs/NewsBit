package com.param.newsbit.ui.fragments

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import com.param.newsbit.R
import com.param.newsbit.databinding.FragmentNewsArticleBinding
import com.param.newsbit.entity.NetworkStatus
import com.param.newsbit.entity.NewsViewMode
import com.param.newsbit.viewmodel.ViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.log


@AndroidEntryPoint
class NewsArticleFragment : Fragment() {

    private val TAG = javaClass.simpleName

    private lateinit var binding: FragmentNewsArticleBinding
    private val args by navArgs<NewsArticleFragmentArgs>()
    private val viewModel by viewModels<ViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewsArticleBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            tv.text = args.newsTitle
            body.movementMethod = ScrollingMovementMethod()
        }

        binding.swapFab.setOnClickListener {

            val updated =
                if (viewModel.summaryStatus.value?.first == NewsViewMode.SUMMARY) NewsViewMode.FULL else NewsViewMode.SUMMARY

            viewModel.summaryStatus.value = viewModel.summaryStatus.value?.copy(first = updated)

        }

        viewModel.downloadSummary(args.newsUrl)

        viewModel.selectSummary(args.newsUrl).observe(viewLifecycleOwner) {
            binding.summary.text = it
        }

        viewModel.getNewsBody(args.newsUrl).observe(viewLifecycleOwner) {
            binding.body.text = it
        }


        viewModel.summaryStatus.observe(viewLifecycleOwner) {


            val viewMode = it.first
            val status = it.second

            Log.i(TAG, "onViewCreated: ViewMode-Status $viewMode $status")

            when (viewMode) {

                NewsViewMode.SUMMARY -> {

                    binding.summary.visibility = View.VISIBLE
                    binding.body.visibility = View.GONE

                    when (status) {

                        NetworkStatus.NOT_STARTED -> {
                            binding.summaryProgressBar.visibility = View.VISIBLE
                            binding.errorMsg.visibility = View.GONE
                        }

                        NetworkStatus.IN_PROGRESS -> {
                            binding.summaryProgressBar.visibility = View.VISIBLE
                            binding.errorMsg.visibility = View.GONE

                        }

                        NetworkStatus.SUCCESS -> {
                            binding.summaryProgressBar.visibility = View.GONE
                            binding.errorMsg.visibility = View.GONE

                        }

                        NetworkStatus.ERROR -> {
                            binding.summaryProgressBar.visibility = View.GONE
                            binding.errorMsg.visibility = View.VISIBLE
                        }

                    }

                }

                NewsViewMode.FULL -> {
                    binding.summary.visibility = View.GONE
                    binding.body.visibility = View.VISIBLE
                    binding.summaryProgressBar.visibility = View.GONE
                    binding.errorMsg.visibility = View.GONE
                }

            }

        }

        /*        viewModel.viewMode.observe(viewLifecycleOwner) {

                    Log.i(TAG, "onViewCreated: ViewMode: $it")

                    when (it) {

                        NewsViewMode.SUMMARY -> {
                            binding.summary.visibility = View.VISIBLE
                            binding.body.visibility = View.GONE
                            binding.summaryProgressBar.visibility = View.VISIBLE
                            binding.errorMsg.visibility = View.VISIBLE
                        }

                        NewsViewMode.FULL -> {
                            binding.summary.visibility = View.GONE
                            binding.body.visibility = View.VISIBLE
                            binding.summaryProgressBar.visibility = View.GONE
                            binding.errorMsg.visibility = View.GONE
                        }

                        else -> {}

                    }

                }

                viewModel.downloadSummaryStatus.observe(viewLifecycleOwner) {

                    when (it) {

                        NetworkStatus.NOT_STARTED -> {
                            binding.summaryProgressBar.visibility = View.GONE
                            binding.errorMsg.visibility = View.GONE
                        }

                        NetworkStatus.IN_PROGRESS -> {
                            binding.summaryProgressBar.visibility = View.VISIBLE
                            binding.errorMsg.visibility = View.GONE

                        }

                        NetworkStatus.SUCCESS -> {
                            binding.summaryProgressBar.visibility = View.GONE
                            binding.errorMsg.visibility = View.GONE

                        }

                        NetworkStatus.ERROR -> {
                            binding.summaryProgressBar.visibility = View.GONE
                            binding.errorMsg.visibility = View.VISIBLE
                            binding.body.visibility = View.GONE

                        }

                        else -> {}

                    }

                }

            */

        lifecycleScope.launch(Dispatchers.IO) {

            Log.i(TAG, "News image url: ${args.newsImgUrl.toString()}")

            binding.newsArticleImage.load(args.newsImgUrl) {
                transformations(RoundedCornersTransformation(8f))
                crossfade(500)
                scale(Scale.FIT)
                error(R.drawable._04_error)
            }

        }

        requireActivity().addMenuProvider(
            object : MenuProvider {

                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_news_article, menu)
                    setIconMenuBookmark(menu[0], args.newsIsBookmarked)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

                    return when (menuItem.itemId) {

                        R.id.bookmarkToggle -> {

                            setIconMenuBookmark(menuItem, !args.newsIsBookmarked)

                            viewModel.toggleBookmark(
                                args.newsUrl,
                                !args.newsIsBookmarked
                            )

                            true
                        }

                        else -> false
                    }

                }

            }, viewLifecycleOwner, Lifecycle.State.RESUMED
        )

    }


    private fun setIconMenuBookmark(menuItem: MenuItem, bookmark: Boolean) {

        val bookmarkToggleIcon =
            if (bookmark) R.drawable.ic_round_bookmark_24
            else R.drawable.ic_round_bookmark_border_24

        menuItem.setIcon(bookmarkToggleIcon)

    }

}
