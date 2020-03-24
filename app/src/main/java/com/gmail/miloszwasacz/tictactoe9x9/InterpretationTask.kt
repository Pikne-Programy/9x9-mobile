package com.gmail.miloszwasacz.tictactoe9x9

import android.app.Application
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
                    val result = viewModel.createBoardState(resultPacket)
                    if(result == null) {
                        viewModel.debugMsg.value = Event(PacketBadErrDbgUin(method = "ERR", params = ParamsBadErrDbgUin(viewModel.getApplication<Application>().getString(R.string.warning_invalid_stt)), time = resultPacket.time))
                    }
                    else {
                        viewModel.connectDialog.value = Event(false)
                        viewModel.currentGameState.value = Event(result)
                    }
                    Log.i("packetSTT", resultPacket.toString())
                }
                //Wysłanie odpowiedzi na pakiet "PNG"
                is PacketGetPngPog -> {
                    if(resultPacket.method == "PNG") {
                        viewModel.sendPOG()
                    }
                }
                //Wyświetlanie info o oprogramowaniu
                is PacketVER -> {
                    viewModel.versionPacket.value = Event(resultPacket)
                    Log.i("packetVER", resultPacket.toString())
                }
                //Zapisywanie błędów itp. w Log'u
                is PacketBadErrDbgUin -> {
                    viewModel.debugMsg.value = Event(resultPacket)
                    Log.i("packetMSG", resultPacket.params.msg)
                }
            }
        }
        else {
            Log.i("state", (resultPacket as PacketBadErrDbgUin).params.msg)
        }
    }
}