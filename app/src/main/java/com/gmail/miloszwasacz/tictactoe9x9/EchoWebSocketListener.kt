package com.gmail.miloszwasacz.tictactoe9x9

import android.os.AsyncTask
import com.google.gson.Gson
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener


class EchoWebSocketListener(private val viewModel: CommunicationViewModel, private val room: String): WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: Response) {
        val packet = PacketVER(params = ParamsVER(null, null, null, null, nick = null, fullNick = null), time = (System.currentTimeMillis()/1000L).toInt())
        webSocket.send(Gson().toJson(packet))
        TimeoutTask(viewModel, room).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        val task = InterpretationTask(viewModel, text, room)
        viewModel.interpretationTaskList.add(task)
        task.execute()
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(viewModel.NORMAL_CLOSURE_STATUS, null)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        val task = InterpretationTask(viewModel, null, room)
        viewModel.interpretationTaskList.add(task)
        task.execute()
        viewModel.writeToLog(t.message ?: "connection error")
    }
}