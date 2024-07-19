package com.param.newsbit.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.param.newsbit.databinding.FragmentHomeBinding
import com.param.newsbit.viewmodel.ViewModel
import com.param.newsbit.entity.News
import com.param.newsbit.model.parser.NetworkStatus
import com.param.newsbit.ui.adapter.AdapterNewsHead
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val TAG = javaClass.simpleName

    private lateinit var adapterNewsHead: AdapterNewsHead
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: ViewModel by viewModels()

    private val newsCategories = setOf(
        "Top Stories",
        "Business",
        "Real Estate",
        "Opinion",
        "Politics",
        "Life"
    )


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

        val navigateOnClick: (News) -> Unit = {
            val action = HomeFragmentDirections.actionHomeToNewsArticle(it)
            findNavController().navigate(action)
        }

        val bookmarkOnClick: (News) -> Unit = {
            viewModel.toggleBookmark(it.url, !it.isBookmarked)
        }

        adapterNewsHead = AdapterNewsHead(navigateOnClick, bookmarkOnClick)

        // Set up RecyclerView News
        binding.allNewsRV.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterNewsHead
        }

        // Update Genre Chip
        binding.chipGroup.setOnCheckedStateChangeListener { _, selectedChips ->
            Log.i(TAG, "$selectedChips chip selected")
            viewModel.chipGenre.value = selectedGenre(selectedChips)
        }


        // Get News by Genre
        viewModel.chipGenre.observe(viewLifecycleOwner) { genre ->
            viewModel.downloadRetro(genre)
        }

        // Display News by Genre
        viewModel.selectNews().observe(viewLifecycleOwner) {
            Log.i(TAG, "Display news = $it")
            adapterNewsHead.setList(it)
        }


        // Handle error downloading all news
        viewModel.downloadNewsError.observe(viewLifecycleOwner) { networkStatus ->

            Log.i(TAG, "Downloading all news network status = $networkStatus")

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

        newsCategories.forEachIndexed { index, genre ->
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

    private fun selectedGenre(selectedChips: List<Int>) =
        newsCategories.toList()[selectedChips.first()]


}