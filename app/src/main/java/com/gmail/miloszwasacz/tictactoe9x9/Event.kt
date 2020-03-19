package com.gmail.miloszwasacz.tictactoe9x9

open class Event<out T>(private val content: T) {

    //Returns the content and prevents its use again.
    fun getContent(): T? {
        return content
    }

     //Returns the content, even if it's already been handled.
    fun peekContent(): T = content
}