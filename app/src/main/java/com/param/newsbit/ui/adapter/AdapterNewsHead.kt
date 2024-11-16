package com.param.newsbit.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import com.param.newsbit.R
import com.param.newsbit.databinding.ItemNewsHeadBinding
import com.param.newsbit.entity.News
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdapterNewsHead(
    private val myOnClick: (News) -> Unit,
    private val bookmarkOnClick: (News) -> Unit
) : PagingDataAdapter<News, AdapterNewsHead.ViewHolderNewsHead>(DF) {

    inner class ViewHolderNewsHead(val binding: ItemNewsHeadBinding) : RecyclerView.ViewHolder(binding.root)

    companion object{
        private val DF = object : DiffUtil.ItemCallback<News>() {
            override fun areItemsTheSame(oldItem: News, newItem: News) = oldItem.url == newItem.url
            override fun areContentsTheSame(oldItem: News, newItem: News) = oldItem == newItem
        }
    }

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

        val news = getItem(position) ?: return

        CoroutineScope(Dispatchers.Default).launch {

            holder.binding.imageView.load(news.imageUrl) {
                scale(Scale.FILL)
                transformations(RoundedCornersTransformation(8f))
                crossfade(100)
            }

        }

        holder.binding.apply {

            newsTitle.text = news.title
            newsDate.text = news.pubDate.run { "$dayOfMonth $month, $year" }

            val bookmarkImageRes =
                if (news.isBookmarked) R.drawable.ic_round_bookmark_24
                else R.drawable.ic_round_bookmark_border_24

            bookmark.setImageResource(bookmarkImageRes)
        }


        holder.binding.newsHead.setOnClickListener {
            myOnClick(news)
        }

        holder.binding.bookmark.setOnClickListener {
            bookmarkOnClick(news)
        }

    }

}