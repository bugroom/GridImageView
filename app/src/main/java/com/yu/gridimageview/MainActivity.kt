package com.yu.gridimageview

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private val mData = mutableListOf<DongYu>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getData()
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(LinearItemDecoration())
        recyclerView.adapter = ImageAdapter(mData)
    }

    private fun getData() {

        val url1 = "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=3363295869,2467511306&fm=26&gp=0.jpg"

        val url2 = "https://ss0.bdstatic.com/70cFuHSh_Q1YnxGkpoWK1HF6hhy/it/u=2853553659,1775735885&fm=26&gp=0.jpg"

        val url3 = "https://t7.baidu.com/it/u=86095685,3663716840&fm=193&f=GIF"

        repeat(1000) {
            val imageData = mutableListOf<String>()
            val i = (0..3).random()
            if (i == 1) {
                imageData.add(url3)
            } else {
                for (j in 0 until 3 * i) {
                    imageData.add(when {
                        j % 3 == 1 -> url1
                        j % 3 == 0 -> url2
                        else -> url3
                    })
                }
            }
            val dongYu = DongYu(
                R.mipmap.ic_launcher_round,
                "冬日暖雨$it",
                "开心开心开心\n这是一个可以显示多ImageView的View",
                imageData
            )
            mData.add(dongYu)
        }
    }

    private class LinearItemDecoration: RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect.set(20, 20, 20, 20)
        }
    }
}