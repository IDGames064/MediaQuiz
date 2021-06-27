package idprogs.mediaquiz.ui.quiz.letterquiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import idprogs.mediaquiz.R
import idprogs.mediaquiz.data.api.model.Movie
import idprogs.mediaquiz.databinding.FragmentLetterquizBinding
import idprogs.mediaquiz.ui.base.BaseFragment
import idprogs.mediaquiz.ui.quiz.QuizViewModel
import idprogs.mediaquiz.utility.CommonUtils
import idprogs.mediaquiz.utility.CommonUtils.Companion.getWidth

@AndroidEntryPoint
class LetterQuizFragment: BaseFragment() {
    private val TAG = "LetterQuizFragment"
    private var taskElements = ElementList()

    private lateinit var inputPanel: InputPanel

    private lateinit var mBinding: FragmentLetterquizBinding
    private val mViewModel: QuizViewModel by viewModels(
        ownerProducer = { requireActivity() }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeToLiveData()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_letterquiz, container, false)
        return mBinding.root
    }

    private fun displayAnswer(movie: Movie) {
        mBinding.fbAnswer.removeAllViews()
        taskElements.removeAll()
        val words: MutableList<String> = movie.title.split("\\s+".toRegex()).map { word -> word.replace("""^[,.]""".toRegex(),"") }.toMutableList()
        var i = 0
        val maxLength = resources.displayMetrics.widthPixels / CommonUtils.dp2px(30) - 2
        while (i < words.count()) {
            if (words[i].getWidth() > maxLength + 0.5) {
                words.add(i + 1, words[i].substring(maxLength))
                words[i] = words[i].substring(0, maxLength)+"↵"
            }
            i++
        }
        for (word in words) {
            val ll = LinearLayout(context)
            for (idx in word.indices) {
                val type = if (word[idx].isLetterOrDigit()) ElementType.SYMBOL else ElementType.SEPARATOR
                val elem = Element(type, word[idx].toUpperCase(), requireContext())
                if (type == ElementType.SYMBOL)  taskElements.addElement(elem)
                elem.attach(ll)
            }
            val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            layoutParams.setMargins(15, 0, 15, 5)
            mBinding.fbAnswer.addView(ll, layoutParams)
        }

        inputPanel = InputPanel(mBinding.container, taskElements)
        inputPanel.onChange {
            CommonUtils.vibratePhone(requireContext())
            mViewModel.onAnswerChanged(it)
        }
    }

    private fun subscribeToLiveData() {
        mViewModel.movieLiveData.observe(this, {
            if (it != null) displayAnswer(it)
        })

        mViewModel.questionAnswered.observe(this, {
            if (it) {
                inputPanel.hideHint()
                inputPanel.reveal()
            }
        })
    }

}
