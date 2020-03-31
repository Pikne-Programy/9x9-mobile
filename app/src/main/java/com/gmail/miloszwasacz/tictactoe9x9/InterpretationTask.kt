package com.gmail.miloszwasacz.tictactoe9x9

import android.app.Application
import android.os.AsyncTask
import com.google.gson.Gson

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
                            viewModel.writeToLog("Invalid STT packet: ${Gson().toJson(resultPacket)}")
                            viewModel.sendGET()
                        }
                        else {
                            viewModel.dialog.value = Event(null)
                            viewModel.currentGameState.value = Event(result)
                            viewModel.writeToLog(Gson().toJson(resultPacket))
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
                        viewModel.writeToLog(Gson().toJson(resultPacket))
                        if(!viewModel.timeout) {
                            viewModel.timeout = true
                            when {
                                resultPacket.params.protocolVersion > viewModel.getApplication<Application>().getString(R.string.protocol_version) -> viewModel.dialog.value = Event(DialogType.OLD_PROTOCOL)
                                resultPacket.params.protocolVersion < viewModel.getApplication<Application>().getString(R.string.protocol_version) -> viewModel.dialog.value = Event(DialogType.WRONG_PROTOCOL)
                                else -> {
                                    viewModel.dialog.value = Event(null)
                                    viewModel.sendJON()
                                }
                            }
                        }
                    }
                    //Zapisywanie błędów itp. w Log'u
                    is PacketBadErrDbgUin -> {
                        viewModel.writeToLog(Gson().toJson(resultPacket))
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