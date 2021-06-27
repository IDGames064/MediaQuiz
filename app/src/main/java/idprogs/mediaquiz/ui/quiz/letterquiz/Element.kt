package idprogs.mediaquiz.ui.quiz.letterquiz

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import idprogs.mediaquiz.R
import idprogs.mediaquiz.utility.CommonUtils.Companion.isEnglish
import idprogs.mediaquiz.utility.LANGUAGE
import java.util.*


enum class ElementType {
    SYMBOL,
    SEPARATOR
}

data class Element(
    val type: ElementType,
    val value: Char,
    val context: Context,
    val layout: Int = R.layout.answer_element_symbol
) {
    private var view: View
    private var tv: TextView
    private var container: ViewGroup?
    private var clickListener: ((elem: Element)->Unit)? = null
    private var changeListener: ((elem: Element)->Unit)? = null
    var id: String
    var currentId: String? = null
    var current: Char? = null
       set(c) {
           tv.text = c?.toString()
           if (c == null) tv.visibility = View.INVISIBLE else tv.visibility = View.VISIBLE
           tv.setTextColor(context.getColor(R.color.red_100))
           if (LANGUAGE != "en" && c.isEnglish()) tv.setTypeface(null, Typeface.ITALIC) else tv.setTypeface(null, Typeface.NORMAL)
           field = c
           changeListener?.invoke(this)
       }
    var revealed: Boolean = false
       set(v) {
           current = value
           tv.setTextColor(context.getColor(R.color.amber_400))
           field = v
           changeListener?.invoke(this)
       }
    var selected: Boolean = false
       set(v) {
           if (v) container?.backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.red_200))
           else container?.backgroundTintList = null
           field = v
       }

    var hidden: Boolean = false
        set(v) {
            if (v) view.visibility = View.INVISIBLE else view.visibility = View.VISIBLE
            field = v
        }

    init {
        val id = if (type == ElementType.SYMBOL) layout else R.layout.answer_element_separator
        view = View.inflate(context, id, null)
        tv = view.findViewById(R.id.tvLetter)
        container = view.findViewById(R.id.container)
        current = null
        if (type == ElementType.SEPARATOR) revealed = true
        view.setOnClickListener {  clickListener?.invoke(this)}
        this.id = UUID.randomUUID().toString()
    }

    fun attach(viewGroup: ViewGroup) = viewGroup.addView(view)

    fun onClick(listener: ((elem: Element) -> Unit)?) {
        this.clickListener = listener
    }

    fun onChange(listener: ((elem: Element) -> Unit)?) {
        this.changeListener = listener
    }

    fun setCurrent(currentChar: Char?, currentId: String?) {
        this.currentId = currentId
        this.current = currentChar
    }

}