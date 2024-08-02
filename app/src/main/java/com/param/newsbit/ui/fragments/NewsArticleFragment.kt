package com.param.newsbit.ui.fragments

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.*
import androidx.core.view.MenuProvider
import androidx.core.view.children
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import com.google.android.material.chip.Chip
import com.param.newsbit.R
import com.param.newsbit.databinding.FragmentNewsArticleBinding
import com.param.newsbit.model.parser.NetworkStatus
import com.param.newsbit.viewmodel.ViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


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
            newsArticleTitle.text = args.newsHeader.title
            newsSummary.movementMethod = ScrollingMovementMethod()
            newsFull.movementMethod = ScrollingMovementMethod()
            newsArticleDate.text = args.newsHeader.pubDate.run { "$dayOfMonth, $month $year" }
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshSummary(args.newsHeader.url)
            binding.swipeRefresh.isRefreshing = true
        }

        viewModel.downloadSummary(args.newsHeader.url)

        viewModel.selectSummary(args.newsHeader.url).observe(viewLifecycleOwner) { summary ->
            if (!summary.isNullOrBlank()) {
                binding.newsSummary.text = summary
            }
        }

        viewModel.selectNewsBody(args.newsHeader.url).observe(viewLifecycleOwner) { body ->
            binding.newsFull.text = body
            Log.i(TAG, "News body len ${body.length}")
        }

        lifecycleScope.launch(Dispatchers.IO) {

            Log.i(TAG, "Coil image url = ${args.newsHeader.imageUrl.toString()}")

            binding.newsArticleImage.load(args.newsHeader.imageUrl) {
                transformations(RoundedCornersTransformation(8f))
                crossfade(500)
                error(R.drawable._04_error)
                scale(Scale.FIT)
            }

        }

        viewModel.viewMode.observe(viewLifecycleOwner) { selectedViewMode ->

            when (selectedViewMode) {

                "Show Summary" -> {
                    binding.newsFull.visibility = View.GONE
                    binding.swipeRefresh.visibility = View.VISIBLE

                    viewModel.downloadSummaryError.observe(viewLifecycleOwner) { networkStatus ->

                        Log.i(TAG, "Downloading summary status = $networkStatus")

                        when (networkStatus) {

                            NetworkStatus.IN_PROGRESS -> {
                                binding.progressBar.visibility = View.VISIBLE
                                binding.newsSummary.visibility = View.GONE
                                binding.errorSummary.visibility = View.GONE
                            }

                            NetworkStatus.SUCCESS -> {
                                binding.progressBar.visibility = View.GONE
                                binding.newsSummary.visibility = View.VISIBLE
                                binding.errorSummary.visibility = View.GONE
                            }

                            NetworkStatus.ERROR -> {
                                binding.progressBar.visibility = View.GONE
                                binding.newsSummary.visibility = View.GONE
                                binding.errorSummary.visibility = View.VISIBLE
                            }

                            else -> {}

                        }

                    }

                    viewModel.refreshSummaryError.observe(viewLifecycleOwner) { networkStat ->
                        Log.i(TAG, "Refresh summary status = $networkStat")
                        binding.swipeRefresh.isRefreshing = networkStat == NetworkStatus.IN_PROGRESS
                    }

                }

                "Show Full" -> {
                    binding.newsFull.visibility = View.VISIBLE

                    // Prevents summary related UI from overlapping full news.
                    binding.progressBar.visibility = View.GONE
                    binding.newsSummary.visibility = View.GONE
                    binding.errorSummary.visibility = View.GONE
                    binding.swipeRefresh.visibility = View.GONE
                }

            }

        }

        binding.contentToggleChipGroup.setOnCheckedStateChangeListener { _, _ ->
            val selectedViewMode = binding.contentToggleChipGroup.children
                .toList()
                .map { it as Chip }
                .filter { it.isChecked }
                .map { it.text }
                .first()
                .toString()
            Log.i(TAG, "Summary or Full ChipGroup selection = $selectedViewMode")
            viewModel.viewMode.value = selectedViewMode
        }

        requireActivity().addMenuProvider(
            object : MenuProvider {

                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_news_article, menu)
                    setIconMenuBookmark(menu[0], args.newsHeader.isBookmarked)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

                    return when (menuItem.itemId) {

                        R.id.bookmarkToggle -> {

                            args.newsHeader.isBookmarked = !args.newsHeader.isBookmarked

                            setIconMenuBookmark(menuItem, args.newsHeader.isBookmarked)

                            viewModel.toggleBookmark(
                                args.newsHeader.url,
                                args.newsHeader.isBookmarked
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
