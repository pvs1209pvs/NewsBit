package com.param.newsbit.ui.adapter.diffutils

import androidx.recyclerview.widget.DiffUtil
import com.param.newsbit.entity.News

class NewsDiffUtil(
    private val oldList: List<News>,
    private val newList: List<News>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition].url == newList[newItemPosition].url

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition] == newList[newItemPosition]

}