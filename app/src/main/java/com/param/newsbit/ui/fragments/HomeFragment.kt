package com.param.newsbit.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.param.newsbit.R
import com.param.newsbit.databinding.FragmentHomeBinding
import com.param.newsbit.entity.News
import com.param.newsbit.model.parser.NetworkStatus
import com.param.newsbit.model.parser.NewsGenre
import com.param.newsbit.ui.adapter.AdapterNewsHead
import com.param.newsbit.viewmodel.ViewModel
import com.param.newsbit.worker.NewsDownloadWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val TAG = javaClass.simpleName

    private val viewModel: ViewModel by viewModels()
    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapterNewsHead: AdapterNewsHead
    private lateinit var datePickerFragment: DatePickerFragment

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


        NewsGenre.TITLES.forEach {
            viewModel.downloadNews(it)
        }

        lifecycleScope.launch(Dispatchers.IO) {
            Log.i(TAG, "Delete news older than one week")
            viewModel.deleteOlderThanWeek()
        }


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


        binding.allNewsRV.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterNewsHead
        }

        binding.chipGroup.setOnCheckedStateChangeListener { _, selectedChips ->

            val selectedGenre = selectedGenre(selectedChips)
            Log.i(TAG, "Genre chip selected: $selectedGenre")

            viewModel.newsFilter.value = viewModel.newsFilter.value?.copy(genre = selectedGenre)

        }

        datePickerFragment = DatePickerFragment { date ->
            Log.i(TAG, "DatePicker date: $date")
            viewModel.newsFilter.value = viewModel.newsFilter.value?.copy(date = date)

        }

        viewModel.getNewsByGenreDateTitle().observe(viewLifecycleOwner) {
            adapterNewsHead.submitData(viewLifecycleOwner.lifecycle, it)
        }

        viewModel.downloadNewsError.observe(viewLifecycleOwner) { networkStatus ->

            Log.i(TAG, "Network status when downloading news: $networkStatus")

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


        requireActivity().addMenuProvider(

            object : MenuProvider {

                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_home, menu)
                }

                override fun onPrepareMenu(menu: Menu) {

                    val searchItem = menu.findItem(R.id.search_item).actionView as SearchView

                    searchItem.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                        override fun onQueryTextSubmit(query: String?) = false

                        override fun onQueryTextChange(newText: String?): Boolean {
                            viewModel.newsFilter.value =
                                viewModel.newsFilter.value?.copy(searchQuery = newText ?: "")
                            return true
                        }

                    })

                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

                    return when (menuItem.itemId) {

                        R.id.date_picker -> {
                            datePickerFragment.show(childFragmentManager, "date picker")
                            true
                        }

                        else -> false
                    }

                }

            }, viewLifecycleOwner, Lifecycle.State.RESUMED

        )

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