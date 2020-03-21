@file:Suppress("DEPRECATION")
package com.gmail.miloszwasacz.tictactoe9x9

/*class CloseSocketTask(private val viewModel: CommunicationViewModel): AsyncTask<Void, Void?, Void?>() {

    override fun doInBackground(vararg arg0: Void): Void? {
        for(task in viewModel.communicationTaskList) {
            if(task.status == Status.RUNNING || task.status == Status.PENDING)
            task.cancel(false)
        }
        for(task in viewModel.sendTaskList) {
            if(task.status == Status.RUNNING || task.status == Status.PENDING)
                task.cancel(false)
        }

        return null
    }*/

    /*override fun onPostExecute(result: Void?) {
        if(viewModel.socket != null && !viewModel.socket!!.isClosed) {
            viewModel.socket!!.shutdownInput()
            viewModel.socket!!.shutdownOutput()
            viewModel.socket!!.close()
        }
    }
}*/