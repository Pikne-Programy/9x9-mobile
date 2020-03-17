package com.gmail.miloszwasacz.tictactoe9x9

import android.os.AsyncTask
import android.util.Log
import com.google.gson.Gson
import java.net.InetSocketAddress

class SendTask(private val activity: BoardActivity, private var packet: Packet/*, private var socket: Socket, private var output: OutputStream*/): AsyncTask<Void, Void?, Void?>() {
    private val socket = activity.socket
    private var output = activity.output

    override fun onPreExecute() {
        if(!socket.isConnected) {
            socket.connect(InetSocketAddress(activity.serverIP, activity.serverPORT))
            output = socket.getOutputStream()
        }
    }

    override fun doInBackground(vararg arg0: Void): Void? {
        val packetJSON = Gson().toJson(packet)
        output.write(packetJSON.toByteArray(charset("UTF-8")))

        Log.i("status", "SENT: $packetJSON")

        return null
    }
}