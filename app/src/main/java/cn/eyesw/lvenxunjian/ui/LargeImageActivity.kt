package cn.eyesw.lvenxunjian.ui

import cn.eyesw.lvenxunjian.R
import cn.eyesw.lvenxunjian.base.BaseActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_large_image.*

class LargeImageActivity : BaseActivity() {

    override fun getContentLayoutRes(): Int = R.layout.activity_large_image

    override fun initToolbar() {

    }

    override fun initView() {
        val url = intent.getStringExtra("url")
        Picasso.with(mContext).load(url).into(large_image_view)
        large_image_view.setOnClickListener { finish() }
    }

}
