package com.gmail.miloszwasacz.tictactoe9x9

import android.os.AsyncTask
import android.util.Log
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket


class ConnectTask(private var viewModel: CommunicationViewModel, private var roomName: String = "public"): AsyncTask<Void, Void?, String?>() {
    private lateinit var socket: Socket
    private lateinit var output: OutputStream
    private lateinit var inputStream: InputStreamReader
    private val serverIP = viewModel.serverIP
    private val serverPORT = viewModel.serverPORT

    override fun onPreExecute() {
        viewModel.dialogId.value = Event(viewModel.connectDialogId)
        viewModel.wrongSocket.value = Event(false)
        socket = Socket()
    }

    override fun doInBackground(vararg arg0: Void): String? {
        try {
            socket.connect(InetSocketAddress(serverIP, serverPORT))
            output = socket.getOutputStream()
            val packet = Gson().toJson(PacketJON(params = ParamsJON(roomName), time = (System.currentTimeMillis()/1000L).toInt()))
            output.write(packet.toByteArray(charset("UTF-8")))

            inputStream = InputStreamReader(socket.getInputStream())
            val input = BufferedReader(inputStream)

            val resultJSON = input.readLine()
            Log.i("packet", resultJSON)

            return resultJSON
        }
        catch(e: IOException) {
            return null
        }
    }

    override fun onPostExecute(result: String?) {
        if(result != null) {
            viewModel.socket = socket
            viewModel.output = output
            viewModel.inputStream = inputStream
            if(viewModel.deserializePacketFromServer(result).method == "STT") viewModel.dialogId.value = Event(viewModel.removeDialog)
            val resultPacket = viewModel.deserializePacketFromServer(result)
            viewModel.communicate(resultPacket)
        }
        else {
            viewModel.wrongSocket.value = Event(true)
        }
    }
}