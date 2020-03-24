@file:Suppress("InflateParams", "SetTextI18n")
package com.gmail.miloszwasacz.tictactoe9x9

import android.os.AsyncTask
import android.util.Log
import java.io.BufferedReader
import java.io.IOException

class CommunicationTask(private val viewModel: CommunicationViewModel, private val inputPacket: Packet?/*, private var socket: Socket, private var output: OutputStream, private var inputStream: InputStreamReader*/): AsyncTask<Void, Void?, Packet>() {
    private lateinit var input: BufferedReader
    private var inputStream = viewModel.inputStream

    override fun onPreExecute() {
        input = BufferedReader(inputStream)

        if(inputPacket != null) {
            when(inputPacket) {
                is PacketSTT -> {
                    viewModel.connectDialog.value = Event(false)
                    viewModel.currentGameState.value = Event(viewModel.createBoardState(inputPacket))
                }
                is PacketVER -> {
                    viewModel.connectDialog.value = Event(false)
                    viewModel.versionPacket.value = Event(inputPacket)
                }
                is PacketBadErrDbgUin -> {
                    viewModel.connectDialog.value = Event(false)
                    viewModel.debugMsg.value = Event(inputPacket)
                    Log.i("packetMSG", inputPacket.params.msg)
                }
            }
        }
    }

    override fun doInBackground(vararg arg0: Void): Packet {
        return try {
            if(!isCancelled) {
                val packetString = input.readLine()
                val packet = viewModel.deserializePacketFromServer(packetString)

                Log.i("packet", packetString)
                packet
            }
            else PacketBadErrDbgUin(method = "DBG", params = ParamsBadErrDbgUin("Task cancelled"), time = (System.currentTimeMillis()/1000L).toInt())
        }
        catch(e: IOException) {
            Log.i("error", e.toString())
            PacketBadErrDbgUin(method = "ERR", params = ParamsBadErrDbgUin(e.toString()), time = (System.currentTimeMillis()/1000L).toInt())
        }
    }

    override fun onPostExecute(resultPacket: Packet) {
        if(!isCancelled) {
            when(resultPacket) {
                //Odbieranie planszy
                is PacketSTT -> {
                    if(viewModel.connectDialog.value == Event(true)) {
                        viewModel.connectDialog.value = Event(false)
                    }
                    viewModel.currentGameState.value = Event(viewModel.createBoardState(resultPacket))
                    viewModel.communicate(null)
                }
                //Wysłanie odpowiedzi na pakiet "PNG"
                is PacketGetPngPog -> {
                    if(resultPacket.method == "PNG") {
                        viewModel.sendPOG()
                    }
                    viewModel.communicate(null)
                }
                //Wyświetlanie info o oprogramowaniu
                is PacketVER -> {
                    if(viewModel.connectDialog.value == Event(true)) {
                        viewModel.connectDialog.value = Event(false)
                    }
                    viewModel.versionPacket.value = Event(resultPacket)
                    viewModel.communicate(null)
                }
                //Zapisywanie błędów itp. w Log'u
                else -> {
                    if(viewModel.connectDialog.value == Event(true)) {
                        viewModel.connectDialog.value = Event(false)
                    }
                    viewModel.debugMsg.value = Event(resultPacket as PacketBadErrDbgUin)
                    Log.i("packetMSG", resultPacket.params.msg)

                    viewModel.communicate(null)
                }
            }
        }
        else {
            Log.i("state", (resultPacket as PacketBadErrDbgUin).params.msg)
        }
    }
}