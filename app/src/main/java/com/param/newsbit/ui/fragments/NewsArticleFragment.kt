package com.param.newsbit.ui.fragments

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.*
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
import com.param.newsbit.viewmodel.ViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_news_article.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException


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
            newsArticleDate.text = args.newsHeader.pubDate.run { "$dayOfMonth, $month $year" }
        }


        // Swipe down to refresh summary
        binding.swipeRefresh.setOnRefreshListener {

            viewModel.refreshSummary2(args.newsHeader.url)
            swipeRefresh.isRefreshing = false

            /*   viewModel.refreshSummary(args.newsHeader.url).invokeOnCompletion { jobError ->
                   if (jobError == null) {
                       swipeRefresh.isRefreshing = false
                   } else {
                       Log.d("View model job error", jobError.message.toString())
                   }
               }
   */

        }

        viewModel.downloadSummary(args.newsHeader.url)


        // Download image
        lifecycleScope.launch(Dispatchers.IO) {

            Log.d(TAG, "Coil image url = ${args.newsHeader.imageUrl.toString()}")

            binding.newsArticleImage.load(args.newsHeader.imageUrl) {
                transformations(RoundedCornersTransformation(8f))
                crossfade(500)
                error(R.drawable._04_error)
                scale(Scale.FIT)
            }
        }


        // Download summary
/*        lifecycleScope.launch(Dispatchers.IO) {

            try {
                viewModel.downloadSummary(args.newsHeader.url)
            } catch (socketTimeoutException: SocketTimeoutException) {
                Log.e(TAG, "Socket timeout ${socketTimeoutException.message}")
            } catch (e: Exception) {

                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Download summary exception ${e.message} ${e.printStackTrace()}")
                    binding.newsSummary.visibility = View.GONE
                    binding.errorSummary.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                }

            }

        }*/


        viewModel.selectSummary(args.newsHeader.url).observe(viewLifecycleOwner) { summary ->

            Log.i(TAG, "Summary null or blank ${summary.isNullOrBlank()}")

            if (!summary.isNullOrBlank()) {
                binding.newsSummary.text = summary
            }

        }


        viewModel.downloadSummaryError.observe(viewLifecycleOwner) { isError ->

            Log.i(TAG, "Error downloading summary = $isError")

            if (isError) {
                binding.newsSummary.visibility = View.GONE
                binding.errorSummary.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
            } else {
                binding.newsSummary.visibility = View.VISIBLE
                binding.errorSummary.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
            }

        }

        viewModel.refreshSummaryError.observe(viewLifecycleOwner) { isError ->

            Log.i(TAG, "Error refreshing summary $isError")

            if (isError) {
                binding.newsSummary.visibility = View.GONE
                binding.errorSummary.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
            } else {
                binding.newsSummary.visibility = View.VISIBLE
                binding.errorSummary.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
            }

        }


        requireActivity().addMenuProvider(object : MenuProvider {

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_news_article, menu)
                setIconMenuBookmark(menu[0], args.newsHeader.isBookmarked)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

                return when (menuItem.itemId) {

                    R.id.bookmarkToggle -> {

                        args.newsHeader.isBookmarked = !args.newsHeader.isBookmarked

                        setIconMenuBookmark(menuItem, args.newsHeader.isBookmarked)

                        viewModel.toggleBookmark(args.newsHeader.url, args.newsHeader.isBookmarked)

                        true
                    }

                    else -> false
                }

            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)


    }


    private fun setIconMenuBookmark(menuItem: MenuItem, bookmark: Boolean) {

        val bookmarkToggleIcon =
            if (bookmark) R.drawable.ic_round_bookmark_24
            else R.drawable.ic_round_bookmark_border_24

        menuItem.setIcon(bookmarkToggleIcon)

    }

}
