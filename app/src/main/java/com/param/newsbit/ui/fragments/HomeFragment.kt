package com.param.newsbit.ui.fragments

import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Constraints
import android.view.ViewGroup.LayoutParams
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_SLIDE
import com.google.android.material.snackbar.Snackbar
import com.param.newsbit.R
import com.param.newsbit.databinding.FragmentHomeBinding
import com.param.newsbit.entity.News
import com.param.newsbit.entity.NetworkStatus
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
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val TAG = javaClass.simpleName

    private val viewModel: ViewModel by viewModels()
    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapterNewsHead: AdapterNewsHead
    private lateinit var rangeDatePicker: MaterialDatePicker<Pair<Long, Long>>

    @Inject
    lateinit var newsNotificationService: NewsNotificationService

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val navigateOnClick: (News) -> Unit = {

            val bundle = bundleOf(
                "url" to it.url,
                "title" to it.title,
                "date" to it.pubDate.toString(),
                "isBook" to it.isBookmarked,
                "imgUrl" to it.imageUrl
            )

            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                requireActivity()
                    .supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.detailsFrag, NewsArticleFragment::class.java, bundle)
                    .commit()

            } else {
                findNavController().navigate(R.id.actionHomeToNewsArticle, bundle)
            }
        }

        val bookmarkOnClick: (News) -> Unit = {
            viewModel.toggleBookmark(it.url)
        }

        adapterNewsHead = AdapterNewsHead(navigateOnClick, bookmarkOnClick)

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
            Duration.ofHours(12),
            Duration.ofMinutes(15)
        ).setConstraints(Constraints.Builder().build()).build()

        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            NewsDownloadWorker::class.java.simpleName,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )

        NewsGenre.TITLES.forEach {
            viewModel.downloadNews(
                genre = it,
                limit = 50,
                onStart = { viewModel.newsStatus.value = NetworkStatus.IN_PROGRESS },
                onSuccess = { viewModel.newsStatus.postValue(NetworkStatus.SUCCESS) },
                onError = { viewModel.newsStatus.postValue(NetworkStatus.ERROR) },

                )
        }

        setupMenu()

        val bottomNavView =
            requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        val bottomNavHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            bottomNavView.height * 1f,
            resources.displayMetrics
        )

        binding.newsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterNewsHead
        }

        binding.newsRefresher.apply {

            isRefreshing = true

            setOnRefreshListener {
                viewModel.downloadNews(
                    genre = selectedGenre(binding.chipGroup.checkedChipIds),
                    limit = 50,
                    onStart = { viewModel.newsRefreshStatus.value = NetworkStatus.IN_PROGRESS },
                    onSuccess = { viewModel.newsRefreshStatus.postValue(NetworkStatus.SUCCESS) },
                    onError = { viewModel.newsRefreshStatus.postValue(NetworkStatus.ERROR) }
                )
            }

        }

        binding.chipGroup.setOnCheckedStateChangeListener { _, selectedChips ->

            val selectedGenre = selectedGenre(selectedChips)

            Log.i(TAG, "Genre Chip: $selectedGenre")

            viewModel.newsFilter.value = viewModel.newsFilter.value?.copy(genre = selectedGenre)

        }

        viewModel.getNewsByGenreDateTitle().observe(viewLifecycleOwner) {
            adapterNewsHead.submitData(viewLifecycleOwner.lifecycle, it)
        }

        viewModel.newsRefreshStatus.observe(viewLifecycleOwner) {

            Log.i(TAG, "Refreshing News Status: $it")

            when (it) {

                NetworkStatus.IN_PROGRESS -> {
                    binding.newsRefresher.isRefreshing = true
                }

                NetworkStatus.SUCCESS -> {
                    binding.newsRefresher.isRefreshing = false
                }

                NetworkStatus.ERROR -> {
                    binding.newsRefresher.isRefreshing = false

                    Snackbar
                        .make(binding.root, "Failed to download news", Snackbar.LENGTH_LONG)
                        .setAnchorView(requireActivity().findViewById(R.id.bottomNavigationView))
                        .setAnimationMode(ANIMATION_MODE_SLIDE)
                        .show()

                }

                NetworkStatus.NOT_STARTED -> {
                    binding.newsRefresher.isRefreshing = false
                }

                else -> {}

            }

        }

        viewModel.newsStatus.observe(viewLifecycleOwner) {

            Log.i(TAG, "Downloading News Status: $it")

            when (it) {

                NetworkStatus.IN_PROGRESS -> {
                    binding.newsRecyclerView.visibility = View.GONE
                    binding.homeProgressBar.visibility = View.VISIBLE
                }

                NetworkStatus.SUCCESS -> {
                    binding.newsRecyclerView.visibility = View.VISIBLE
                    binding.homeProgressBar.visibility = View.GONE
                }

                NetworkStatus.ERROR -> {
                    binding.newsRecyclerView.visibility = View.GONE
                    binding.homeProgressBar.visibility = View.GONE

                    Snackbar
                        .make(binding.root, "Failed to download news", Snackbar.LENGTH_LONG)
                        .setAnchorView(requireActivity().findViewById(R.id.bottomNavigationView))
                        .setAnimationMode(ANIMATION_MODE_SLIDE)
                        .show()

                }

                NetworkStatus.NOT_STARTED -> {
                    binding.homeProgressBar.visibility = View.GONE
                }

                else -> {}

            }

        }

    }

    private fun setupMenu() {

        binding.homeToolbar.inflateMenu(R.menu.menu_home)

        binding.homeToolbar.setOnMenuItemClickListener {

            when (it.itemId) {

                R.id.search_item -> {

                    val searchView =
                        binding.homeToolbar.menu.findItem(R.id.search_item).actionView as SearchView

                    searchView.apply {

                        setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                            override fun onQueryTextSubmit(query: String?) = false

                            override fun onQueryTextChange(newText: String?): Boolean {
                                viewModel.newsFilter.value =
                                    viewModel.newsFilter.value?.copy(searchQuery = newText ?: "")
                                return true
                            }

                        })

                    }

                    true
                }

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