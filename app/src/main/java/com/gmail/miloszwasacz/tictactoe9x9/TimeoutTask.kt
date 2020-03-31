package com.gmail.miloszwasacz.tictactoe9x9

import android.os.AsyncTask
import android.util.Log


class TimeoutTask(private val viewModel: CommunicationViewModel, val roomName: String): AsyncTask<Void, Void?, Void?>() {
    override fun doInBackground(vararg params: Void?): Void? {
        try {
            Thread.sleep(10000)
        }
        catch(ie: InterruptedException) {
            Log.w("warning", "Sleep interrupted")
        }
        return null
    }

    override fun onPostExecute(result: Void?) {
        if(!viewModel.timeout) {
            viewModel.timeout = true
            viewModel.sendJON(roomName)
        }
    }
}