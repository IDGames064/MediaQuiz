package idprogs.mediaquiz.ui.quiz.optionquiz


import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import idprogs.mediaquiz.R
import idprogs.mediaquiz.databinding.FragmentOptionquizBinding
import idprogs.mediaquiz.ui.base.BaseFragment
import idprogs.mediaquiz.ui.quiz.QuizViewModel
import idprogs.mediaquiz.utility.CommonUtils
import idprogs.mediaquiz.utility.CommonUtils.Companion.getSymbols


@AndroidEntryPoint
class OptionQuizFragment: BaseFragment() {
    private val TAG = "OptionQuizFragment"

    private var btnCorrect: Button? = null
    private var btnCurrent: Button? = null

    private lateinit var mBinding: FragmentOptionquizBinding
    private val mViewModel: QuizViewModel by viewModels(
        ownerProducer = { requireActivity() }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeToLiveData()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_optionquiz, container, false)
        return mBinding.root
    }

    private fun displayAnswer(options: List<String>) {
        mBinding.llOptions.removeAllViews()
        for (option in options) {
            val view = View.inflate(context, R.layout.option_view, null) as Button
            if (option == mViewModel.movieLiveData.value?.title || option == mViewModel.artistLiveData.value?.name) btnCorrect = view
            view.text = option
            view.setOnClickListener {
                btnCurrent = view
                if (mViewModel.questionAnswered.value == false) {
                    CommonUtils.vibratePhone(requireContext())
                    mViewModel.onAnswerGot(view.text.toString().getSymbols())
                }
            }
            mBinding.llOptions.addView(view)
        }
    }

    private fun displayResults() {
        if (btnCurrent != null && btnCorrect != null) {
            btnCurrent!!.backgroundTintList = ColorStateList.valueOf(
                this.requireContext().getColor(
                    R.color.red_700A
                )
            )
            btnCorrect!!.backgroundTintList = ColorStateList.valueOf(
                this.requireContext().getColor(
                    R.color.green_500A
                )
            )
            mViewModel.onAnswerGot(btnCorrect!!.text.toString().getSymbols())
            btnCorrect = null
            btnCurrent = null
        }
    }

    private fun subscribeToLiveData() {
        mViewModel.answerOptions.observe(this, {
            if (it != null) displayAnswer(it)
        })

        mViewModel.questionAnswered.observe(this, {
            //if (it) answer.revealAll()
            displayResults()
        })
    }

}

