package com.gmail.miloszwasacz.tictactoe9x9

import android.app.Activity
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket


class ConnectTask(var activity: Activity?) : AsyncTask<Void, Void?, Void?>() {

    override fun doInBackground(vararg arg0: Void): Void? {
        val socket = Socket()
        socket.connect(InetSocketAddress("85.198.250.135", 4780))


        val out: OutputStream = socket.getOutputStream()
        out.write("This is my message".toByteArray(charset("UTF-8")))

        val input = BufferedReader(InputStreamReader(socket.getInputStream()))
        val result = input.readLine()
        Log.i("result", result)

        return null
    }

    override fun onPostExecute(result: Void?) {
        Toast.makeText(activity, "Połączono", Toast.LENGTH_SHORT).show()
    }
}