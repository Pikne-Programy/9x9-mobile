package com.gmail.miloszwasacz.tictactoe9x9

import android.util.Log
import com.google.gson.Gson
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener


class EchoWebSocketListener(private val viewModel: CommunicationViewModel, private val room: String): WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: Response) {
        val packet = Gson().toJson(PacketJON(params = ParamsJON(room), time = (System.currentTimeMillis()/1000L).toInt()))
        webSocket.send(packet + "\r\n")
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
        Log.i("Error", t.message ?: "connection error")
    }
}