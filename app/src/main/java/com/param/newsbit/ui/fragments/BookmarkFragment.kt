package com.param.newsbit.ui.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.param.newsbit.R
import com.param.newsbit.databinding.FragmentBookmarkBinding
import com.param.newsbit.entity.News
import com.param.newsbit.viewmodel.ViewModel
import com.param.newsbit.ui.adapter.AdapterNewsBookmark
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookmarkFragment : Fragment() {

    private lateinit var binding: FragmentBookmarkBinding
    private lateinit var adapterNewsBookmark: AdapterNewsBookmark
    private val viewModel: ViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookmarkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)




        val bookmarkedNewsOnClick: (news: News) -> Unit = {

            val bundle = bundleOf(
                "url" to it.url,
                "title" to it.title,
                "date" to it.pubDate.toString(),
                "isBook" to it.isBookmarked,
                "imgUrl" to it.imageUrl
            )

            when(resources.configuration.orientation){

                Configuration.ORIENTATION_LANDSCAPE -> {
                    requireActivity()
                        .supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.detailsFrag, NewsArticleFragment::class.java, bundle)
                        .commit()
                }

                Configuration.ORIENTATION_PORTRAIT -> {
                    findNavController().navigate(
                        R.id.action_bookmarkFragment_to_newsArticleFragment,
                        bundle
                    )
                }

                else -> throw IllegalStateException("Bloody heLLLL.")

            }

        }

        adapterNewsBookmark = AdapterNewsBookmark(bookmarkedNewsOnClick)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("Fragment Bookmark", "onViewCreated")


        binding.bookmarksNews.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterNewsBookmark
        }

        viewModel.selectNewsBookmark().observe(viewLifecycleOwner) {
            Log.d("bookmark frag", "${it.size} bookmarked items")
            adapterNewsBookmark.setList(it)

        }

    }


}