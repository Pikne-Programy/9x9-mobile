@file:Suppress("DEPRECATION")
package com.gmail.miloszwasacz.tictactoe9x9

import android.app.Application
import android.app.Dialog
import android.os.AsyncTask
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Socket

open class CommunicationViewModel(application: Application): AndroidViewModel(application) {
    //Dialogi
    var dialogs = ArrayList<Dialog>()
    var connectDialog = MutableLiveData<Event<Boolean>>()
    var versionPacket = MutableLiveData<Event<PacketVER?>>()
    var debugMsg = MutableLiveData<Event<PacketBadErrDbgUin>>()

    //IP i port serwera
    val serverIP = (PreferenceManager.getDefaultSharedPreferences(application).getString(application.getString(R.string.key_ip), application.getString(R.string.default_ip)) ?: application.getString(R.string.default_ip)).toString()
    val serverPORT = (PreferenceManager.getDefaultSharedPreferences(application).getString(application.getString(R.string.key_port), application.getString(R.string.default_port)) ?: application.getString(R.string.default_port)).toInt()

    //Socket
    var socket: Socket? = null
    lateinit var output: OutputStream
    lateinit var inputStream: InputStreamReader

    //Listy AsyncTask'ów
    val communicationTaskList = ArrayList<CommunicationTask>()
    val sendTaskList = ArrayList<SendTask>()

    var currentGameState = MutableLiveData<Event<BoardModel?>>()
    var wrongSocket = MutableLiveData<Event<Boolean>>()

    //Łączenie z serwerem
    fun connect(roomName: String) {
        ConnectTask(this@CommunicationViewModel, roomName).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    //Odbieranie pakietu
    fun communicate(packet: Packet?) {
        val task = CommunicationTask(this@CommunicationViewModel, packet)
        communicationTaskList.add(task)
        task.execute()
    }

    //Wysyłanie ruchu gracza
    fun sendMove(x: Int, y: Int) {
        val task = SendTask(this@CommunicationViewModel, PacketSET(params = ParamsSET(x, y), time = (System.currentTimeMillis()/1000L).toInt()))
        sendTaskList.add(task)
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    //Wysyłanie odpowiedzi na Ping
    fun sendPOG() {
        val task = SendTask(this@CommunicationViewModel, PacketGetPngPog(method = "POG", params = ParamsGetPngPog(), time = (System.currentTimeMillis()/1000L).toInt()))
        sendTaskList.add(task)
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    //Zamykanie Socketa
    override fun onCleared() {
        CloseSocketTask(this@CommunicationViewModel).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        super.onCleared()
    }

    //Deserializacja pakietu z serwera w obiekt
    fun deserializePacketFromServer(input: String): Packet {
        val packetTypeSTT = object: TypeToken<PacketSTT>() {}.type
        val packetTypeVER = object: TypeToken<PacketVER>() {}.type
        val packetTypeBadErrDbgUin = object: TypeToken<PacketBadErrDbgUin>() {}.type
        val packetTypeGetPngPog = object: TypeToken<PacketGetPngPog>() {}.type

        val tempPacket = Gson().fromJson<PacketGetPngPog>(input, packetTypeGetPngPog)

        return when(tempPacket.method) {
            "STT" -> Gson().fromJson<PacketSTT>(input, packetTypeSTT)
            "VER" -> Gson().fromJson<PacketVER>(input, packetTypeVER)
            "BAD" -> Gson().fromJson<PacketBadErrDbgUin>(input, packetTypeBadErrDbgUin)
            "ERR" -> Gson().fromJson<PacketBadErrDbgUin>(input, packetTypeBadErrDbgUin)
            "DBG" -> Gson().fromJson<PacketBadErrDbgUin>(input, packetTypeBadErrDbgUin)
            "UIN" -> Gson().fromJson<PacketBadErrDbgUin>(input, packetTypeBadErrDbgUin)
            else -> Gson().fromJson<PacketGetPngPog>(input, packetTypeGetPngPog)
        }
    }

    //Konwersja pakietu na stan gry
    fun createBoardState(packet: PacketSTT): BoardModel? {
        //Konwersja planszy
        val board = ArrayList<ArrayList<Char>>()
        val bigBoard = ArrayList<ArrayList<Char>>()
        for(y in 0..8) {
            board.add(ArrayList())
            for(x in 0..8)
                board[y].add(packet.params.board[y*9 + x])
        }
        for(y in 0..2) {
            bigBoard.add(ArrayList())
            for(x in 0..2)
                bigBoard[y].add(packet.params.bigBoard[y*3 + x])
        }
        //Ustawianie końca gry
        val isEnded = packet.params.isEnded
        //Ustawianie zwycięzcy
        val whoWon = packet.params.whoWon
        //Ustawianie gracza
        val you = packet.params.you
        //Ustawianie aktywnego gracza
        val move = packet.params.move
        //Ustawianie ostatniego ruchu
        val lastMove = packet.params.lastMove
        //Ustawianie aktywnego pola na planszy
        val marked = packet.params.marked

        return  try {
            BoardModel(board, bigBoard, isEnded, whoWon, you, move, lastMove, marked)
        }
        catch(e: IllegalArgumentException) {
            null
        }

    }
}