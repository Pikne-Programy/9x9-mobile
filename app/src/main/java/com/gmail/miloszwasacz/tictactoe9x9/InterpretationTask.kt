package com.gmail.miloszwasacz.tictactoe9x9

import android.os.AsyncTask
import android.util.Log

class InterpretationTask(private val viewModel: CommunicationViewModel, private val inputPacket: String): AsyncTask<Void, Void?, Packet>() {

    override fun doInBackground(vararg arg0: Void): Packet {
        return if(!isCancelled) {
            viewModel.deserializePacketFromServer(inputPacket)
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
                }
                //Wysłanie odpowiedzi na pakiet "PNG"
                is PacketGetPngPog -> {
                    if(resultPacket.method == "PNG") {
                        viewModel.sendPOG()
                    }
                }
                //Wyświetlanie info o oprogramowaniu
                is PacketVER -> {
                    viewModel.versionPacket = resultPacket
                    viewModel.dialogId.value = Event(viewModel.versionDialogId)
                }
                //Zapisywanie błędów itp. w Log'u
                else -> {
                    viewModel.debugMsg.value = Event(resultPacket as PacketBadErrDbgUin)
                    Log.i("packetMSG", resultPacket.params.msg)
                }
            }
        }
        else {
            Log.i("state", (resultPacket as PacketBadErrDbgUin).params.msg)
        }
    }
}