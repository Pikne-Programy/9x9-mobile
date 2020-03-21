@file:Suppress("InflateParams", "SetTextI18n")
package com.gmail.miloszwasacz.tictactoe9x9

import android.os.AsyncTask
import android.util.Log
import java.io.BufferedReader

class CommunicationTask(private val viewModel: CommunicationViewModel, private val inputPacket: Packet?/*, private var socket: Socket, private var output: OutputStream, private var inputStream: InputStreamReader*/): AsyncTask<Void, Void?, Packet>() {
    private lateinit var input: BufferedReader
    private var inputStream = viewModel.inputStream

    override fun onPreExecute() {
        input = BufferedReader(inputStream)

        if(inputPacket != null && inputPacket is PacketSTT) {
            viewModel.currentGameState.value = Event(viewModel.createBoardState(inputPacket))
        }
    }

    override fun doInBackground(vararg arg0: Void): Packet {
        return if(!isCancelled) {
            val packetString = input.readLine()
            val packet = viewModel.deserializePacketFromServer(packetString)

            Log.i("packet", packetString)
            packet
        }
        else PacketBadErrDbgUin(method = "DBG", params = ParamsBadErrDbgUin("Task cancelled"), time = (System.currentTimeMillis()/1000L).toInt())
    }

    override fun onPostExecute(resultPacket: Packet) {
        if(!isCancelled) {
            when(resultPacket) {
                //Odbieranie planszy
                is PacketSTT -> {
                    viewModel.dialogId.value = Event(viewModel.removeDialog)
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
                    viewModel.versionPacket = resultPacket
                    viewModel.dialogId.value = Event(viewModel.versionDialogId)
                    viewModel.communicate(null)
                }
                //Zapisywanie błędów itp. w Log'u
                else -> {
                    Log.i("packetMSG", (resultPacket as PacketBadErrDbgUin).params.msg)

                    viewModel.communicate(null)
                }
            }
        }
        else {
            Log.i("state", (resultPacket as PacketBadErrDbgUin).params.msg)
        }
    }
}