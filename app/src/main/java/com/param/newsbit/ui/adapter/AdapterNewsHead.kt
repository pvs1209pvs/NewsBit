package com.param.newsbit.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import com.param.newsbit.R
import com.param.newsbit.databinding.ItemNewsHeadBinding
import com.param.newsbit.entity.News
import com.param.newsbit.ui.adapter.diffutils.NewsDiffUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdapterNewsHead(
    private val myOnClick: (News) -> Unit,
    private val bookmarkOnClick: (News) -> Unit
) : RecyclerView.Adapter<AdapterNewsHead.ViewHolderNewsHead>() {

    inner class ViewHolderNewsHead(val binding: ItemNewsHeadBinding) :
        RecyclerView.ViewHolder(binding.root)


    private val list = mutableListOf<News>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderNewsHead {

        return ViewHolderNewsHead(
            ItemNewsHeadBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolderNewsHead, position: Int) {

        val news = list[position]

        CoroutineScope(Dispatchers.Default).launch {

            holder.binding.imageView.load(news.imageUrl) {
                scale(Scale.FILL)
                transformations(RoundedCornersTransformation(8f))
                crossfade(100)
            }

        }

        holder.binding.apply {
            newsTitle.text = news.title
            newsGenre.text = news.pubDate.run { "$dayOfMonth $month, $year" }

            val bookmarkImageRes =
                if (news.isBookmarked) R.drawable.ic_round_bookmark_24 else R.drawable.ic_round_bookmark_border_24
            bookmark.setImageResource(bookmarkImageRes)
        }


        holder.binding.newsHead.setOnClickListener {
            myOnClick(news)
        }

        holder.binding.bookmark.setOnClickListener {
            bookmarkOnClick(news)
        }

    }

    override fun getItemCount() = list.size

    fun setList(newList: List<News>) {

        val diffResult = DiffUtil.calculateDiff(NewsDiffUtil(list, newList))

        list.clear()
        list.addAll(newList)

        diffResult.dispatchUpdatesTo(this)

    }

}