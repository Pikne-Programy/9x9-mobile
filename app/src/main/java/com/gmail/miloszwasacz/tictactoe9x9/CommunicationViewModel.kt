@file:Suppress("DEPRECATION")
package com.gmail.miloszwasacz.tictactoe9x9

import android.app.Application
import android.app.Dialog
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


open class CommunicationViewModel(application: Application): AndroidViewModel(application) {
    //IP i port serwera
    private val serverIP = (PreferenceManager.getDefaultSharedPreferences(application).getString(application.getString(R.string.key_ip), application.getString(R.string.default_ip)) ?: application.getString(R.string.default_ip)).toString()
    private val serverPORT = tryParse(PreferenceManager.getDefaultSharedPreferences(application).getString(application.getString(R.string.key_port), application.getString(R.string.default_port)) ?: application.getString(R.string.default_port), 65535)

    //Socket
    private val client = OkHttpClient()
    private var socket: WebSocket? = null
    val NORMAL_CLOSURE_STATUS = 1000
    var timeout = false
    var roomName = "public"

    //Listy Tasków i Dialogów oraz Eventy
    var dialogs = ArrayList<Dialog>()
    val interpretationTaskList = ArrayList<InterpretationTask>()
    var dialog = MutableLiveData<Event<DialogType?>>()
    var currentGameState = MutableLiveData<Event<BoardModel>>()
    var serverError = MutableLiveData<Event<Boolean>>()
    var wrongSocket = MutableLiveData<Event<Boolean>>()

    //Łączenie z serwerem
    fun connect() {
        dialog.value = Event(DialogType.CONNECT)
        wrongSocket.value = Event(false)
        val request = Request.Builder().url("ws://$serverIP:$serverPORT").build()
        val listener = EchoWebSocketListener(this@CommunicationViewModel)
        socket = client.newWebSocket(request, listener)
    }

    //Wysysłanie prośby o dołączenie do pokoju
    fun sendJON() {
        dialog.value = Event(DialogType.JOIN)
        val packet = PacketJON(params = ParamsJON(roomName), time = (System.currentTimeMillis()/1000L).toInt())
        socket?.send(Gson().toJson(packet))
    }

    //Wysyłanie ruchu gracza
    fun sendMove(x: Int, y: Int) {
        val packet = PacketSET(params = ParamsSET(x, y), time = (System.currentTimeMillis()/1000L).toInt())
        socket?.send(Gson().toJson(packet))
    }

    //Wysyłanie odpowiedzi na Ping
    fun sendPOG() {
        val packet = PacketGetPngPog(method = "POG", params = ParamsGetPngPog(), time = (System.currentTimeMillis()/1000L).toInt())
        socket?.send(Gson().toJson(packet))
    }

    //Wysłanie prośby o pakiet STT
    fun sendGET() {
        val packet = PacketGetPngPog(method = "GET", params = ParamsGetPngPog(), time = (System.currentTimeMillis()/1000L).toInt())
        socket?.send(Gson().toJson(packet))
    }

    //Zamykanie Socketa
    override fun onCleared() {
        for(task in interpretationTaskList) {
            if(task.status == AsyncTask.Status.RUNNING || task.status == AsyncTask.Status.PENDING)
                task.cancel(false)
        }
        socket?.close(NORMAL_CLOSURE_STATUS, null)
        client.dispatcher.executorService.shutdown()
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
            BoardModel(board, bigBoard, whoWon, you, move, lastMove, marked)
        }
        catch(e: IllegalArgumentException) {
            null
        }
    }

    //tryParse int
    fun tryParse(value: String, defaultVal: Int) = try {
        Integer.parseInt(value)
    } catch (e: NumberFormatException) {
        defaultVal
    }

    //Zapisywanie do Loga
    fun writeToLog (data: String) {
        if (Environment.MEDIA_MOUNTED != Environment.getExternalStorageState()) {
            return
        }
        val file = File(getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "log.txt")
        try {
            file.createNewFile()
            val outputStream = FileOutputStream(file, true)

            val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
            val currentDateAndTime: String = sdf.format(Date())

            outputStream.write(("$currentDateAndTime/: $data\r\n").toByteArray())
            outputStream.close()
        }
        catch(e: IOException) {
            Log.e("Exception", "File write failed: $e")
        }
    }
}