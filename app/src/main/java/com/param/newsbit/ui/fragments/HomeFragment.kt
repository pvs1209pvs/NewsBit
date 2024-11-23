package com.param.newsbit.ui.fragments

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.util.Pair
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.param.newsbit.R
import com.param.newsbit.databinding.FragmentHomeBinding
import com.param.newsbit.entity.News
import com.param.newsbit.model.parser.NetworkStatus
import com.param.newsbit.model.parser.NewsGenre
import com.param.newsbit.notifaction.NewsNotificationService
import com.param.newsbit.ui.adapter.AdapterNewsHead
import com.param.newsbit.ui.validator.WeeksInPastDateValidator
import com.param.newsbit.viewmodel.ViewModel
import com.param.newsbit.worker.NewsDownloadWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.ZoneId


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val TAG = javaClass.simpleName

    private val viewModel: ViewModel by viewModels()
    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapterNewsHead: AdapterNewsHead
    private lateinit var rangeDatePicker: MaterialDatePicker<Pair<Long, Long>>
    private lateinit var newsNotificationService: NewsNotificationService

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
            viewModel.downloadNews(it,20)
        }

        lifecycleScope.launch(Dispatchers.IO) {
            Log.i(TAG, "Deleting News older than one week.")
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

        val weeksInPastDateValidator = CalendarConstraints.Builder()
            .setValidator(WeeksInPastDateValidator(1))
            .build()

        rangeDatePicker = MaterialDatePicker.Builder
            .dateRangePicker()
            .setTheme(R.style.News_Bit_Range_Date_Picker)
            .setCalendarConstraints(weeksInPastDateValidator)
            .build()

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        newsNotificationService = NewsNotificationService(requireActivity())

        with(NotificationManagerCompat.from(requireContext())) {
            if (ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@with
            }
        }

        val workRequest = PeriodicWorkRequestBuilder<NewsDownloadWorker>(
            Duration.ofHours(2),
            Duration.ofMinutes(1)
        ).setConstraints(Constraints.Builder().build()).build()


        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            "NewsDownloadedWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )

        binding.allNewsRV.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterNewsHead
        }

        binding.chipGroup.setOnCheckedStateChangeListener { _, selectedChips ->

            val selectedGenre = selectedGenre(selectedChips)
            Log.i(TAG, "Genre Chip: $selectedGenre")

            viewModel.newsFilter.value = viewModel.newsFilter.value?.copy(genre = selectedGenre)

        }

        viewModel.getNewsByGenreDateTitle().observe(viewLifecycleOwner) {
            adapterNewsHead.submitData(viewLifecycleOwner.lifecycle, it)
        }

        viewModel.downloadNewsError.observe(viewLifecycleOwner) { networkStatus ->

            Log.i(TAG, "Downloading News Network Status: $networkStatus")

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

                            rangeDatePicker.show(childFragmentManager, "rangeDatePicker")

                            rangeDatePicker.addOnPositiveButtonClickListener {

                                val start = Instant
                                    .ofEpochMilli(it.first + 86400000)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()

                                val end = Instant
                                    .ofEpochMilli(it.second + 86400000)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()

                                Log.i(TAG, "onMenuItemSelected: rangeDatePicker: $start $end")

                                viewModel.newsFilter.value =
                                    viewModel.newsFilter.value?.copy(startDate = start)

                                viewModel.newsFilter.value =
                                    viewModel.newsFilter.value?.copy(endDate = end)

                            }

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