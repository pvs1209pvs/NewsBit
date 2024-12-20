package com.param.newsbit.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_SLIDE
import com.google.android.material.snackbar.Snackbar
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

    //    private val args by navArgs<NewsArticleFragmentArgs>()
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

        if(arguments?.getString("url")==null){

        }

        val argsUrl = arguments?.getString("url") ?: return
        val argsTitle = arguments?.getString("title") ?: return
        val argsImageUrl = arguments?.getString("imgUrl")



        binding.toolbar.apply {

            setBackgroundColor(Color.valueOf(0f, 0f, 0f, 0.5f).toArgb())

            setNavigationIcon(R.drawable.baseline_arrow_back_24)

            setNavigationOnClickListener {
                findNavController().popBackStack()
            }


        }

        binding.swipeRefreshSummary.apply {
            setOnRefreshListener {
                viewModel.refreshSummary(argsUrl)
                isRefreshing = true
            }
        }


        binding.tv.text = argsTitle
        binding.body.movementMethod = ScrollingMovementMethod()

        binding.swapFab.setOnClickListener {

            val updated =
                if (viewModel.summaryStatus.value?.first == NewsViewMode.SUMMARY) NewsViewMode.FULL else NewsViewMode.SUMMARY

            viewModel.summaryStatus.value = viewModel.summaryStatus.value?.copy(first = updated)

        }

        binding.newsBookmarkFab.setOnClickListener {
            viewModel.toggleBookmark(argsUrl)
        }

        viewModel.getBookmarkLD(argsUrl).observe(viewLifecycleOwner) {
            val draw = if (it == 1) R.drawable.ic_round_bookmark_24
            else R.drawable.ic_round_bookmark_border_24
            binding.newsBookmarkFab.setImageDrawable(resources.getDrawable(draw))
        }

        viewModel.downloadSummary(argsUrl)

        viewModel.selectSummary(argsUrl).observe(viewLifecycleOwner) {
            binding.summary.text = it
        }

        viewModel.getNewsBody(argsUrl).observe(viewLifecycleOwner) {
            binding.body.text = it
        }

        viewModel.summaryStatus.observe(viewLifecycleOwner) {

            val viewMode = it.first
            val summary = it.second
            val refresh = it.third

            Log.i(TAG, "onViewCreated: ViewMode-Status $viewMode $summary")

            when (viewMode) {

                NewsViewMode.SUMMARY -> {

                    binding.body.visibility = View.GONE
                    binding.swipeRefreshSummary.visibility = View.VISIBLE
                    binding.swipeRefreshSummary.isEnabled = true
                    binding.swapFab.setImageDrawable(resources.getDrawable(R.drawable.baseline_unfold_more_24))

                    when (refresh) {

                        NetworkStatus.NOT_STARTED -> {}

                        NetworkStatus.IN_PROGRESS -> {
                            binding.swipeRefreshSummary.isRefreshing = true

                        }

                        NetworkStatus.SUCCESS -> {
                            binding.swipeRefreshSummary.isRefreshing = false
                        }

                        NetworkStatus.ERROR -> {
                            binding.swipeRefreshSummary.isRefreshing = false

                            Snackbar
                                .make(
                                    binding.root,
                                    "Failed to refresh summary",
                                    Snackbar.LENGTH_LONG
                                )
                                .setAnchorView(requireActivity().findViewById(R.id.bottomNavigationView))
                                .setAnimationMode(ANIMATION_MODE_SLIDE)
                                .show()

                        }

                    }

                    when (summary) {

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
                    binding.swipeRefreshSummary.visibility = View.GONE
                    binding.body.visibility = View.VISIBLE
                    binding.summaryProgressBar.visibility = View.GONE
                    binding.errorMsg.visibility = View.GONE
                    binding.swipeRefreshSummary.isRefreshing = false
                    binding.swipeRefreshSummary.isEnabled = false
                    binding.swapFab.setImageDrawable(resources.getDrawable(R.drawable.baseline_unfold_less_24))

                }

            }

        }

        lifecycleScope.launch(Dispatchers.IO) {

            Log.i(TAG, "News image url: $argsImageUrl")

            binding.newsArticleImage.load(argsImageUrl) {
                transformations(RoundedCornersTransformation(8f))
                crossfade(500)
                scale(Scale.FIT)
                error(R.drawable._04_error)
            }

        }

    }


    private fun setIconMenuBookmark(menuItem: MenuItem, bookmark: Boolean) {

        val bookmarkToggleIcon =
            if (bookmark) R.drawable.ic_round_bookmark_24
            else R.drawable.ic_round_bookmark_border_24

        menuItem.setIcon(bookmarkToggleIcon)

    }

}
