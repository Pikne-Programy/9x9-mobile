@file:Suppress("DEPRECATION")
package com.gmail.miloszwasacz.tictactoe9x9

import android.os.AsyncTask
import android.util.Log
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket


class ConnectTask(private val activity: BoardActivity, private var roomName: String = "public"): AsyncTask<Void, Void?, String>() {
    private lateinit var socket: Socket
    private lateinit var output: OutputStream
    private lateinit var inputStream: InputStreamReader
    private val serverIP = activity.serverIP
    private val serverPORT = activity.serverPORT

    override fun onPreExecute() {
        activity.showDialog(activity.connectDialogId)
        socket = Socket()
    }

    override fun doInBackground(vararg arg0: Void): String {
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

    override fun onPostExecute(result: String) {
        activity.socket = socket
        activity.output = output
        activity.inputStream = inputStream
        activity.removeDialog(activity.connectDialogId)
        val resultPacket = activity.deserializePacketFromServer(result)
        val task = CommunicationTask(activity, resultPacket)
        activity.communicationTaskList.add(task)
        task.execute()
        //CommunicationTask(activity, resultPacket).execute()
    }
}