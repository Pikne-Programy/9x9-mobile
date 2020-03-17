package com.gmail.miloszwasacz.tictactoe9x9

import android.os.AsyncTask
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

class CommunicationTask(private val activity: BoardActivity, private val result: String, private var socket: Socket, private var output: OutputStream, private var inputStream: InputStreamReader): AsyncTask<Void, Void?, Packet>() {
    private lateinit var input: BufferedReader

    override fun onPreExecute() {
        if(!socket.isConnected || socket.isClosed) {
            socket.connect(InetSocketAddress(activity.serverIP, activity.serverPORT))
            output = socket.getOutputStream()
            inputStream = InputStreamReader(socket.getInputStream())
        }
        input = BufferedReader(inputStream)

        val packet = deserializePacketFromServer(result)
        if(packet is PacketSTT) activity.updateGameState(packet)
    }

    override fun doInBackground(vararg arg0: Void): Packet {
        val packetString = input.readLine()
        val packet = deserializePacketFromServer(packetString)

        Log.i("packet", packetString)
        return packet
    }

    override fun onPostExecute(resultPacket: Packet) {
        if(resultPacket is PacketSTT) activity.updateGameState(resultPacket)
    }

    //Deserializacja pakietu z serwera w obiekt
    fun deserializePacketFromServer(input: String): Packet {
        val packetTypeJON = object: TypeToken<PacketJON>() {}.type
        val packetTypeSET = object: TypeToken<PacketSET>() {}.type
        val packetTypeSTT = object: TypeToken<PacketSTT>() {}.type
        val packetTypeVER = object: TypeToken<PacketVER>() {}.type
        val packetTypeBadErrDbgUin = object: TypeToken<PacketBadErrDbgUin>() {}.type
        val packetTypeGetPngPog = object: TypeToken<PacketGetPngPog>() {}.type

        val tempPacket = Gson().fromJson<PacketGetPngPog>(input, packetTypeGetPngPog)

        return when(tempPacket.method) {
            "JON" -> Gson().fromJson<PacketJON>(input, packetTypeJON)
            "SET" -> Gson().fromJson<PacketSET>(input, packetTypeSET)
            "STT" -> Gson().fromJson<PacketSTT>(input, packetTypeSTT)
            "VER" -> Gson().fromJson<PacketVER>(input, packetTypeVER)
            "BAD" -> Gson().fromJson<PacketBadErrDbgUin>(input, packetTypeBadErrDbgUin)
            "ERR" -> Gson().fromJson<PacketBadErrDbgUin>(input, packetTypeBadErrDbgUin)
            "DBG" -> Gson().fromJson<PacketBadErrDbgUin>(input, packetTypeBadErrDbgUin)
            "UIN" -> Gson().fromJson<PacketBadErrDbgUin>(input, packetTypeBadErrDbgUin)
            else -> Gson().fromJson<PacketGetPngPog>(input, packetTypeGetPngPog)
        }
    }
}