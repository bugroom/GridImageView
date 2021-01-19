package com.yu.gridimageview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.gridimageview.yu.GridImageView
import com.gridimageview.yu.OnImageItemClickListener

class ImageAdapter(private val mData: MutableList<DongYu>) :
    RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val name: TextView = itemView.findViewById(R.id.name)
        val content: TextView = itemView.findViewById(R.id.content)
        val gridImageView: GridImageView = itemView.findViewById(R.id.gridImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        val holder = ViewHolder(itemView)
        holder.gridImageView.setOnImageItemClickListener(object : OnImageItemClickListener {
            override fun onImageItemClick(parent: ViewGroup, v: View, position: Int) {
                Toast.makeText(parent.context, position.toString(), Toast.LENGTH_SHORT).show()
            }
        })
        return holder
    }

    override fun getItemCount() = mData.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = mData[position]
        holder.name.text = data.name
        holder.content.text = (data.content + data.images.size)
        holder.icon.setImageResource(data.icon)
        if (data.images.size == 1) {
            // 请设置实际图片宽高，否则可能造成显示错误
            holder.gridImageView.setImageViewSize(400, 2650)
            //  Glide.with(holder.itemView.context).load(data.images[0]).into(holder.roundImageView)
        }
        holder.gridImageView.setImageUrls(data.images)
    }
}