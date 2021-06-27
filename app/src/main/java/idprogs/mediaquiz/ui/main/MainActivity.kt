package idprogs.mediaquiz.ui.main

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import idprogs.mediaquiz.BR
import idprogs.mediaquiz.BuildConfig
import idprogs.mediaquiz.R
import idprogs.mediaquiz.databinding.ActivityMainBinding
import idprogs.mediaquiz.ui.base.BaseActivity
import idprogs.mediaquiz.ui.quiz.QuizActivity
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity: BaseActivity() {
    @Inject
    lateinit var settings: SettingsPopup
    private val mViewModel: MainViewModel by viewModels()
    private lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mBinding.lifecycleOwner = this
        mBinding.setVariable(BR.viewModel, mViewModel)
        mBinding.executePendingBindings()
        subscribeToLiveData()
        settings.onDismiss {
           mViewModel.onSetPreferences()
        }
        showInFullscreen()
    }

    private fun openQuizActivity() {
        startActivity(QuizActivity.newIntent(this))
    }

    private fun subscribeToLiveData() {
        lifecycleScope.launchWhenStarted {
           mViewModel.mainEvent.collect {
              when (it) {
                  MainViewModel.Event.ShowInfo -> showInfo()
                  MainViewModel.Event.OpenQuizActivity -> openQuizActivity()
                  MainViewModel.Event.Restart -> restart()
                  MainViewModel.Event.ShowPreferences -> showPreferences()
                  is MainViewModel.Event.LoadingProgress -> setLoadingProgress(it.progress)
                  is MainViewModel.Event.Loading -> if (it.value) showLoading(true) else hideLoading()
                  is MainViewModel.Event.Error -> showError(getString(it.messageRes))
                  MainViewModel.Event.Empty -> Unit
              }
           }
       }
    }

    private fun showPreferences() {
        settings.popup()
    }

    private fun showInfo() {
        val msg = "Media Quiz v.${BuildConfig.VERSION_NAME}\nby Igor Darchenko, 2021"
        showSnackBar(msg)
    }
}