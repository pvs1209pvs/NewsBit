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
import com.param.newsbit.model.parser.FeedURL
import com.param.newsbit.ui.adapter.AdapterNewsHead
import kotlinx.android.synthetic.main.fragment_home.view.*

class HomeFragment : Fragment() {

    private lateinit var adapterNewsHead: AdapterNewsHead
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: ViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        createGenreChipGroup(FeedURL.genre.keys)
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
        view.allNewsRV.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterNewsHead
        }

        binding.chipGroup.setOnCheckedStateChangeListener { _, selectedChips ->
            Log.d(javaClass.simpleName, "Selected chip = $selectedChips")
            viewModel.chipGenre.value = selectedGenre(selectedChips)
        }

        viewModel.chipGenre.observe(viewLifecycleOwner){
            Log.d(javaClass.simpleName,"Downloading from internet for $it")
            viewModel.downloadFromInternet(it)
        }

        viewModel.selectNews().observe(viewLifecycleOwner){
            Log.d(javaClass.simpleName, "Display news = $it")
            adapterNewsHead.setList(it)
        }

    }

    private fun createGenreChipGroup(genres: Set<String>) {

        val styleChipChoice =
            com.google.android.material.R.style.Widget_MaterialComponents_Chip_Choice

        genres.forEachIndexed { index, genre ->
            val chip = Chip(requireContext()).apply {
                setChipDrawable(
                    ChipDrawable.createFromAttributes(
                        requireContext(),
                        null,
                        0,
                        styleChipChoice
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
        FeedURL.genre.keys.toList()[selectedChips.first()]


}