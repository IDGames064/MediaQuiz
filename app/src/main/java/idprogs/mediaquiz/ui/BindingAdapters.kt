package idprogs.mediaquiz.ui

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import idprogs.mediaquiz.utility.BASE_IMAGE_URL

@BindingAdapter("android:text")
fun setText(view: TextView, count: Int) {
    view.text = count.toString()
}

@BindingAdapter("android:enabled")
fun setEnabled(view: Button, count: Int) {
    view.isEnabled = count > 0
}

@BindingAdapter("android:visibility")
fun setVisible(view: Button, value: Boolean) {
    if (value) view.visibility = View.VISIBLE else view.visibility = View.INVISIBLE
}

@BindingAdapter("android:visibility")
fun setVisible(view: TextView, value: Boolean) {
    if (value) view.visibility = View.VISIBLE else view.visibility = View.INVISIBLE
}

@BindingAdapter("android:visibility")
fun setVisible(view: TextView, count: Int) {
    if (count > 0) view.visibility = View.VISIBLE else view.visibility = View.GONE
}

@BindingAdapter("android:visibility")
fun setVisible(view: ImageView, value: Boolean) {
    if (value) view.visibility = View.VISIBLE else view.visibility = View.GONE
}

@BindingAdapter("android:visibility")
fun setVisible(view: ViewGroup, value: Boolean) {
    if (value) view.visibility = View.VISIBLE else view.visibility = View.GONE
}

@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, url: String?) {
    if (!url.isNullOrEmpty()) {
        Glide.with(view.context).load(BASE_IMAGE_URL + url).into(view)
    }
}
