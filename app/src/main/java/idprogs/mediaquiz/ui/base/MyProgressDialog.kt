package idprogs.mediaquiz.ui.base

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.ProgressBar
import android.widget.TextView
import idprogs.mediaquiz.R

@Suppress("DEPRECATION")
class MyProgressDialog(val context: Context, showPercent: Boolean = false) {
    private val progressDialog = ProgressDialog(context)
    private val pbLoading: ProgressBar?
    private val tvLoading: TextView?
    init {
        progressDialog.show()
        progressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        if (showPercent) progressDialog.setContentView(R.layout.progress_dialog_percent) else progressDialog.setContentView(R.layout.progress_dialog)
        progressDialog.isIndeterminate = !showPercent
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)
        pbLoading = progressDialog.findViewById(R.id.pbLoading)
        tvLoading = progressDialog.findViewById(R.id.tvLoading)
        setProgress(0)
    }

    fun setProgress(progress: Int) {
        pbLoading?.progress = progress
        tvLoading?.text = "$progress%"
    }

    fun show() {
        progressDialog.show()
    }

    fun hide() {
        if (progressDialog.isShowing) {
            setProgress(0)
            progressDialog.cancel()
        }
    }

    fun isShowing() = progressDialog.isShowing
}