package com.param.newsbit.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import com.param.newsbit.databinding.ItemNewsBookmarkBinding
import com.param.newsbit.entity.News
import com.param.newsbit.ui.adapter.diffutils.NewsDiffUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdapterNewsBookmark(
    private val bookmarkedNewsOnClick: (news: News) -> Unit
) : RecyclerView.Adapter<AdapterNewsBookmark.ViewHolderNewsBookmark>() {

    inner class ViewHolderNewsBookmark(val binding: ItemNewsBookmarkBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val list = mutableListOf<News>()

    fun setList(newList: List<News>) {

        val diffResult = DiffUtil.calculateDiff(NewsDiffUtil(list, newList))

        list.clear()
        list.addAll(newList)

        diffResult.dispatchUpdatesTo(this)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolderNewsBookmark(
            ItemNewsBookmarkBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolderNewsBookmark, position: Int) {

        val news = list[position]

        if (news.imageUrl == null) {
            holder.binding.newsBookmarkImage.visibility = View.GONE
        } else {

            CoroutineScope(Dispatchers.IO).launch {
                holder.binding.newsBookmarkImage.load(news.imageUrl) {
                    scale(Scale.FILL)
                    transformations(RoundedCornersTransformation(8f))
                    crossfade(100)
                }
            }

        }


        holder.binding.apply {
            newsBookmarkTitle.text = news.title
            newsBookmarkDate.text = news.pubDate.run { "$dayOfMonth $month, $year" }
            newsBookmarkGenre.text = news.genre
        }

        holder.binding.root.setOnClickListener {
            bookmarkedNewsOnClick(news)
        }

    }

    override fun getItemCount() = list.size
}