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
import com.google.android.material.datepicker.MaterialDatePicker
import com.param.newsbit.databinding.FragmentHomeBinding
import com.param.newsbit.model.ViewModel
import com.param.newsbit.model.entity.News
import com.param.newsbit.model.parser.FeedURL
import com.param.newsbit.ui.adapter.AdapterNewsHead
import kotlinx.android.synthetic.main.fragment_home.view.*
import java.time.LocalDate
import java.util.Calendar

class HomeFragment : Fragment() {

    private lateinit var adapterNewsHead: AdapterNewsHead
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: ViewModel by viewModels()

    private var genre = "Top"

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
            viewModel.toogleBookmark(it.url, !it.isBookmarked)
        }

        adapterNewsHead = AdapterNewsHead(navigateOnClick, bookmarkOnClick)

        // Set up RecyclerView News
        view.allNewsRV.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterNewsHead
        }

//        // Date Picker
//        val datePicker = createDatePicker()
//        binding.imageButton.setOnClickListener {
//            datePicker.show(requireActivity().supportFragmentManager, datePicker.toString())
//        }
//
//        binding.textView.text = LocalDate.now().toString()


    }

    override fun onStart() {
        super.onStart()

        val now = LocalDate.now()

        // Chip group on click
        binding.chipGroup.setOnCheckedStateChangeListener { _, _ ->

            genre = selectedGenre()

            viewModel.smartSelect(genre, now)
            viewModel.selectNews(genre, now).observe(viewLifecycleOwner) { newsList ->
                Log.d(javaClass.simpleName, "News from chips ${newsList.size}")
                adapterNewsHead.setList(newsList)
            }

        }

    }


    override fun onResume() {
        super.onResume()

        val now = LocalDate.now()

        // To start with Top Stories when you first open the app.
        Log.d("Auto-load genre", genre)
        viewModel.smartSelect(genre, now)

        viewModel.selectNews(genre, now).observe(viewLifecycleOwner) { newsList ->
            Log.d("Auto-load news", "$genre ${newsList.size}")
            adapterNewsHead.setList(newsList)
        }

    }

    private fun createDatePicker(): MaterialDatePicker<Long> {

        val picker = MaterialDatePicker.Builder.datePicker()
            .setSelection(Calendar.getInstance().timeInMillis)
            .build()

//        picker.addOnPositiveButtonClickListener {
//            binding.textView.text = LocalDate.ofEpochDay(Duration.ofMillis(it).toDays()).toString()
//        }

        return picker

    }


    private fun createGenreChipGroup(genres: Set<String>) {

        val styleChipChoice = com.google.android.material.R.style.Widget_MaterialComponents_Chip_Choice

        genres.forEachIndexed { index, genre ->
            val chip = Chip(requireContext()).apply {
                setChipDrawable(ChipDrawable.createFromAttributes(requireContext(), null, 0, styleChipChoice))
                text = genre
                isCheckable = true
                id = index
            }
            binding.chipGroup.addView(chip)
        }

        binding.chipGroup.check(0)


    }

    private fun selectedGenre(): String {
        val checkedItem = binding.chipGroup.checkedChipIds.first()
        val genre = FeedURL.genre.keys.toList()[checkedItem]
        return genre
    }


}