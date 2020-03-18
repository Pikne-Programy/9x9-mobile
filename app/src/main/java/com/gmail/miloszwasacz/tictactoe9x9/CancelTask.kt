@file:Suppress("DEPRECATION")
package com.gmail.miloszwasacz.tictactoe9x9

import android.os.AsyncTask

class CancelTask(private val activity: BoardActivity): AsyncTask<Void, Void?, Void?>() {

    override fun onPreExecute() {
        activity.showDialog(activity.disconnectDialogId)
    }

    override fun doInBackground(vararg arg0: Void): Void? {
        for(task in activity.communicationTaskList) {
            if(task.status == Status.RUNNING || task.status == Status.PENDING)
            task.cancel(false)
        }
        for(task in activity.sendTaskList) {
            if(task.status == Status.RUNNING || task.status == Status.PENDING)
                task.cancel(false)
        }

        return null
    }

    override fun onPostExecute(result: Void?) {
        if(!activity.socket.isClosed) {
            activity.socket.shutdownInput()
            activity.socket.shutdownOutput()
            activity.socket.close()
        }
        activity.removeDialog(activity.disconnectDialogId)
        activity.finish()
    }
}