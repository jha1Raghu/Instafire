package com.example.instafire

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instafire.databinding.ItemPostBinding
import com.example.instafire.models.Post

class PostAdapter(val context:Context, val posts:List<Post>) : RecyclerView.Adapter<PostAdapter.ViewHolder>(){

    private lateinit var binding: ItemPostBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(context).inflate(R.layout.item_post,parent,false)
        binding= ItemPostBinding.bind(view)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount()=posts.size

    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        fun bind(post: Post) {
            binding.tvUsername.text= post.user?.name
            binding.tvDescription.text=post.description
            Glide.with(context).load(post.imageUrl).into(binding.ivPost)
            binding.tvRelativeTime.text=DateUtils.getRelativeTimeSpanString(post.creationTimeMs)
        }
    }
}