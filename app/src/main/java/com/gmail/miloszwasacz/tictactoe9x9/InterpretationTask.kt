package com.gmail.miloszwasacz.tictactoe9x9

import android.os.AsyncTask
import android.util.Log

class InterpretationTask(private val viewModel: CommunicationViewModel, private val inputPacket: String?): AsyncTask<Void, Void?, Packet?>() {

    override fun doInBackground(vararg arg0: Void): Packet? {
        return if(!isCancelled) {
            if(inputPacket != null) {
                viewModel.deserializePacketFromServer(inputPacket)
            }
            else null
        }
        else PacketBadErrDbgUin(method = "DBG", params = ParamsBadErrDbgUin("Task cancelled"), time = (System.currentTimeMillis()/1000L).toInt())
    }

    override fun onPostExecute(resultPacket: Packet?) {
        if(!isCancelled) {
            if(resultPacket != null) {
                when(resultPacket) {
                    //Odbieranie planszy
                    is PacketSTT -> {
                        val result = viewModel.createBoardState(resultPacket)
                        if(result == null) {
                            Log.i("invalidSTT", "Invalid STT packet")
                            viewModel.sendGET()
                        }
                        else {
                            viewModel.connectDialog.value = Event(false)
                            viewModel.currentGameState.value = Event(result)
                            Log.i("packetSTT", resultPacket.toString())
                        }
                    }
                    //Wysłanie odpowiedzi na pakiet "PNG"
                    is PacketGetPngPog -> {
                        if(resultPacket.method == "PNG") {
                            viewModel.sendPOG()
                        }
                    }
                    //Wyświetlanie info o oprogramowaniu
                    is PacketVER -> {
                        Log.i("packetVER", resultPacket.toString())
                    }
                    //Zapisywanie błędów itp. w Log'u
                    is PacketBadErrDbgUin -> {
                        Log.i("packetMessage", resultPacket.params.msg)
                        if(resultPacket.method == "ERR") {
                            viewModel.serverError.value = Event(true)
                        }
                    }
                }
            }
            //Błąd komunikacji z serwerem
            else {
                viewModel.wrongSocket.value = Event(true)
            }
        }
    }
}