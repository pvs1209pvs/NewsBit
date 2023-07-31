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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class NewsArticleFragment : Fragment() {

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

        viewModel.selectSummary(args.newsHeader.url).observe(viewLifecycleOwner) {

            if (it == null) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.newsSummary.text = it
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

                        viewModel.toogleBookmark(args.newsHeader.url, args.newsHeader.isBookmarked)

                        true
                    }

                    else -> false
                }

            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        lifecycleScope.launch(Dispatchers.IO) {

            Log.d("Coil downloading from", args.newsHeader.imageUrl.toString())

            binding.newsArticleImage.load(args.newsHeader.imageUrl) {
                transformations(RoundedCornersTransformation(8f))
                crossfade(500)
                error(R.drawable._04_error)
                scale(Scale.FIT)
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
