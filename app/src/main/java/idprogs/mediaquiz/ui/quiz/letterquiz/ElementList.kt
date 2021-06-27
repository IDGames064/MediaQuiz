package idprogs.mediaquiz.ui.quiz.letterquiz

import android.view.ViewGroup

class ElementList {
    private val elements = mutableListOf<Element>()

    fun addElement(element: Element) = elements.add(element)
    fun copyElementList(elementList: ElementList, layout: Int) {
        for (elem in elementList.elements) {
            val newElem = Element(elem.type, elem.value, elem.context, layout)
            newElem.id = elem.id
            elements.add(newElem)
        }
    }

    operator fun get(i: Int): Element? {
        if (i < elements.size) return elements[i]
        return null
    }

    operator fun get(id: String?): Element? {
        for (element in elements)
            if (element.id == id) return element
        return null
    }

    fun getWithCurrentId(id: String?): Element? {
        for (element in elements)
            if (element.currentId == id) return element
        return null
    }

    private fun getNext(element: Element): Element? {
         for (i in elements.indices) {
             if (elements[i] === element && i < elements.size-1) return elements[i+1]
         }
        return null
    }

    fun getNextUnrevealed(element: Element): Element? {
        var elem = getNext(element)
        while (elem != null && elem.revealed) elem = getNext(elem)
        return elem
    }

    fun revealAll() {
        for (element in elements) {
            element.revealed = true
        }
    }

    fun removeAll() = elements.clear()
    fun getValue(): String {
        var result = ""
        for (element in elements) {
            if (element.type == ElementType.SYMBOL && element.current != null) result += element.current
        }
        return result
    }
    fun shuffle() {
        elements.shuffle()
    }

    fun setSelected(element: Element?) {
        for (elem in elements) {
            elem.selected = elem === element
        }
    }

    fun attach(viewGroup: ViewGroup) {
        for (elem in elements) {
            elem.attach(viewGroup)
        }
    }

    fun onClick(listener:  ((elem: Element)->Unit)?) {
        for (elem in elements) {
            if (elem.type == ElementType.SYMBOL) elem.onClick(listener)
        }
    }

    fun onChange(listener:  ((elem: Element)->Unit)?) {
        for (elem in elements) {
            if (elem.type == ElementType.SYMBOL) elem.onChange(listener)
        }
    }

}