package com.param.newsbit.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.param.newsbit.databinding.FragmentHomeBinding
import com.param.newsbit.viewmodel.ViewModel
import com.param.newsbit.entity.News
import com.param.newsbit.model.parser.NetworkStatus
import com.param.newsbit.model.parser.NewsGenre
import com.param.newsbit.ui.adapter.AdapterNewsHead
import com.param.newsbit.worker.NewsDownloadWorker
import dagger.hilt.android.AndroidEntryPoint
import java.time.Duration


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val TAG = javaClass.simpleName

    private val viewModel: ViewModel by viewModels()
    private lateinit var adapterNewsHead: AdapterNewsHead
    private lateinit var binding: FragmentHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val navigateOnClick: (News) -> Unit = {
            val action = HomeFragmentDirections.actionHomeToNewsArticle(
                it.url,
                it.title,
                it.pubDate.toString(),
                it.isBookmarked,
                it.imageUrl
            )
            findNavController().navigate(action)
        }

        val bookmarkOnClick: (News) -> Unit = {
            viewModel.toggleBookmark(it.url, !it.isBookmarked)
        }

        adapterNewsHead = AdapterNewsHead(navigateOnClick, bookmarkOnClick)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        createGenreChipGroup()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        val workManagerConstraints = Constraints.Builder().build()

        val workRequest = PeriodicWorkRequestBuilder<NewsDownloadWorker>(Duration.ofHours(6))
            .setConstraints(workManagerConstraints)
            .build()

        WorkManager.getInstance(requireContext()).enqueue(workRequest)

        // Set up RecyclerView News
        binding.allNewsRV.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterNewsHead
        }


        // Update Genre Chip
        binding.chipGroup.setOnCheckedStateChangeListener { _, selectedChips ->
            val selectedGenre = selectedGenre(selectedChips)
            Log.i(TAG, "$selectedGenre selected")
            viewModel.chipGenre.value = selectedGenre
        }


        // Download news by genre
        viewModel.chipGenre.observe(viewLifecycleOwner) {
            viewModel.downloadNews(it)
        }


        viewModel.getNews().observe(viewLifecycleOwner) {
            adapterNewsHead.submitData(viewLifecycleOwner.lifecycle, it)
        }


        binding.searchText.doOnTextChanged { text, start, before, count ->
            viewModel.searchQuery.value = text.toString()
        }

        viewModel.getNewByTitle().observe(viewLifecycleOwner) {
            adapterNewsHead.submitData(viewLifecycleOwner.lifecycle, it)
        }


//        binding.go.setOnClickListener {
//            val searchText = binding.searchText.text.toString()
//            viewModel.getNewByTitle(searchText).observe(viewLifecycleOwner){
//                adapterNewsHead.submitData(viewLifecycleOwner.lifecycle, it)
//            }
//        }


        // Handle error downloading all news
        viewModel.downloadNewsError.observe(viewLifecycleOwner) { networkStatus ->

            Log.i(TAG, "Downloading all news network status: $networkStatus")

            when (networkStatus) {

                NetworkStatus.SUCCESS -> {
                    binding.allNewsRV.visibility = View.VISIBLE
                    binding.homeProgressBar.visibility = View.GONE
                }

                NetworkStatus.IN_PROGRESS -> {
                    binding.allNewsRV.visibility = View.GONE
                    binding.homeProgressBar.visibility = View.VISIBLE
                }

                NetworkStatus.ERROR -> {
                    binding.allNewsRV.visibility = View.GONE
                    binding.homeProgressBar.visibility = View.GONE
                }

                else -> {}

            }

        }

    }

    private fun createGenreChipGroup() {

        val chipStyle = com.google.android.material.R.style.Widget_MaterialComponents_Chip_Choice

        NewsGenre.TITLES.forEachIndexed { index, genre ->
            val chip = Chip(requireContext()).apply {
                setChipDrawable(
                    ChipDrawable.createFromAttributes(
                        requireContext(),
                        null,
                        0,
                        chipStyle
                    )
                )
                text = genre
                isCheckable = true
                id = index
            }
            binding.chipGroup.addView(chip)
        }

        binding.chipGroup.check(0)

    }

    private fun selectedGenre(selectedChips: List<Int>) = NewsGenre.TITLES[selectedChips.first()]


}