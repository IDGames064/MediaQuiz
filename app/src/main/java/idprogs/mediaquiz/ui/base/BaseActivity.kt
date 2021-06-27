package idprogs.mediaquiz.ui.base

import android.content.Context
import android.content.Intent
import android.graphics.Color.parseColor
import android.graphics.Typeface
import android.os.Build
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_IMMERSIVE
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import idprogs.mediaquiz.utility.LANGUAGE


@Suppress("DEPRECATION")
abstract class BaseActivity: AppCompatActivity() {
    private var mProgressDialog: MyProgressDialog? = null

    fun hideLoading() {
        mProgressDialog?.hide()
    }

    fun showLoading(showProgress: Boolean = false) {
        hideLoading()
        mProgressDialog = MyProgressDialog(this, showProgress)
    }

    fun setLoadingProgress(progress: Int) {
        mProgressDialog?.setProgress(progress)
    }

    fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppContextWrapper.wrap(newBase, LANGUAGE))
    }

    fun restart() {
        val intent = intent
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        finish()
        this.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        startActivity(intent)
    }

    fun showInFullscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or SYSTEM_UI_FLAG_IMMERSIVE
        }
    }

    fun showSnackBar(message: String) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val sbView: View = snackbar.view
        sbView.setBackgroundColor(parseColor("#E0001828"))
        val textView: TextView = sbView.findViewById(com.google.android.material.R.id.snackbar_text)
        textView.setTextColor(getColor(android.R.color.white))
        textView.typeface = Typeface.create("sans-serif-thin", Typeface.NORMAL)
        textView.textSize = 16f
        textView.maxLines = 2
        snackbar.show()
    }
}

