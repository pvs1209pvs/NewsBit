package com.param.newsbit.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.param.newsbit.databinding.FragmentBookmarkBinding
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("Fragment Bookmark", "onViewCreated")

        adapterNewsBookmark = AdapterNewsBookmark()

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