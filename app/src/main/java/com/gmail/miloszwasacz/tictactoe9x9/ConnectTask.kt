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
    private val socket = Socket()
    private lateinit var output: OutputStream
    private lateinit var inputStream: InputStreamReader

    override fun onPreExecute() {
        activity.showDialog(0)
    }

    override fun doInBackground(vararg arg0: Void): String {
        socket.connect(InetSocketAddress(activity.serverIP, activity.serverPORT))
        output = socket.getOutputStream()
        val packet = Gson().toJson(PacketJON(method = "JON", params = ParamsJON(roomName), time = (System.currentTimeMillis()/1000L).toInt()))
        output.write(packet.toByteArray(charset("UTF-8")))

        inputStream = InputStreamReader(socket.getInputStream())
        val input = BufferedReader(inputStream)

        val result = input.readLine()
        Log.i("packet", result)

        return result
    }

    override fun onPostExecute(result: String) {
        activity.removeDialog(0)
        CommunicationTask(activity, result, socket, output, inputStream).execute()
    }
}