package com.gmail.miloszwasacz.tictactoe9x9

import android.app.Application
import android.os.AsyncTask
import com.google.gson.Gson
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener


class EchoWebSocketListener(private val viewModel: CommunicationViewModel): WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: Response) {
        val packet = PacketVER(params = ParamsVER(protocolVersion = viewModel.getApplication<Application>().getString(R.string.protocol_version)), time = (System.currentTimeMillis()/1000L).toInt())
        webSocket.send(Gson().toJson(packet))
        TimeoutTask(viewModel).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        val task = InterpretationTask(viewModel, text)
        viewModel.interpretationTaskList.add(task)
        task.execute()
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(viewModel.NORMAL_CLOSURE_STATUS, null)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        val task = InterpretationTask(viewModel, null)
        viewModel.interpretationTaskList.add(task)
        task.execute()
        viewModel.writeToLog(t.message ?: "connection error")
    }
}