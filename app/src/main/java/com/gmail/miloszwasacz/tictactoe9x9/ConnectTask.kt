package com.gmail.miloszwasacz.tictactoe9x9

import android.app.Activity
import android.os.AsyncTask
import android.util.Log
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*


class ConnectTask(var activity: Activity) : AsyncTask<Void, Void?, Void?>() {
    private val socket = Socket()

    override fun doInBackground(vararg arg0: Void): Void? {
        socket.connect(InetSocketAddress("85.198.250.135", 4780))
        val out: OutputStream = socket.getOutputStream()
        val packet = jacksonObjectMapper().writeValueAsString(Packet(method = "JON", params = ParamsJON(), time = Calendar.getInstance().timeInMillis))
        out.write(packet.toByteArray(charset("UTF-8")))

        val input = BufferedReader(InputStreamReader(socket.getInputStream()))
        val result = input.readLine()
        /*
        var result = ""
        var value: Int
        while(put.read().also { value = it } != -1) {

            // converts int to character
            val c = value.toChar()

            // prints character
            result += c
        }*/
        Log.i("result", result)

        return null
    }

    override fun onPostExecute(result: Void?) {
        //Toast.makeText(activity, "Połączono", Toast.LENGTH_SHORT).show()
        //activity.startActivity(Intent(activity, BoardActivity::class.java))
    }
}