package idprogs.mediaquiz.ui.quiz.letterquiz

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import idprogs.mediaquiz.R
import idprogs.mediaquiz.databinding.InputPanelBinding

class InputPanel(parent: ViewGroup, private val taskElements: ElementList) {
    private var binding: InputPanelBinding = InputPanelBinding.inflate(LayoutInflater.from(parent.context))

    private var currentTaskElement: Element? = null
    set(e) {
        taskElements.setSelected(e)
        field = e
    }
    private var inputElements = ElementList()
    private var changeListener: ((value: String)->Unit)? = null

    init {
        binding.fbSymbols.removeAllViews()
        inputElements.copyElementList(taskElements, R.layout.answer_element_symbol_large)
        inputElements.shuffle()
        inputElements.revealAll()
        inputElements.attach(binding.fbSymbols)
        currentTaskElement = taskElements[0]

        parent.removeAllViews()
        parent.addView(binding.root)

        binding.ll.removeView(binding.btnHint)
        binding.fbSymbols.addView(binding.btnHint)

        taskElements.onClick {
            if (!it.revealed) {
                currentTaskElement = it
                if (it.currentId != null) {
                    inputElements[it.currentId]?.hidden = false
                    it.setCurrent(null, null)
                }
                changeListener?.invoke(taskElements.getValue())
            }
        }

        inputElements.onClick {
            inputElements[currentTaskElement?.currentId]?.hidden = false
            currentTaskElement?.setCurrent(it.value, it.id)
            it.hidden = true
            currentTaskElement = taskElements.getNextUnrevealed(currentTaskElement!!) ?: currentTaskElement
            changeListener?.invoke(taskElements.getValue())
        }

        binding.btnHint.setOnClickListener {
            taskElements.getWithCurrentId(currentTaskElement?.id)?.setCurrent(null, null)
            inputElements[currentTaskElement?.currentId]?.hidden = false
            inputElements[currentTaskElement?.id]?.hidden = true
            currentTaskElement?.revealed = true
            currentTaskElement = taskElements.getNextUnrevealed(currentTaskElement!!) ?: currentTaskElement
            changeListener?.invoke(taskElements.getValue())
        }
    }

    fun hideHint() {
        binding.btnHint.visibility = View.GONE
    }

    fun onChange(listener: ((value: String) -> Unit)?) {
        this.changeListener = listener
    }

    fun reveal() {
        taskElements.revealAll()
        currentTaskElement = null
    }

}