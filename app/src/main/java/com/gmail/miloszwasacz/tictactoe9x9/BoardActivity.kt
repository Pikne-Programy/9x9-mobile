@file:Suppress("DEPRECATION", "SetTextI18n")
package com.gmail.miloszwasacz.tictactoe9x9

import android.app.Dialog
import android.app.ProgressDialog
import android.os.AsyncTask
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.gridlayout.widget.GridLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_board.*
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Socket


class BoardActivity: AppCompatActivity() {
    val connectDialogId = 0
    val disconnectDialogId = 1
    val serverIP = "85.198.250.135"
    val serverPORT = 4780
    lateinit var socket: Socket
    lateinit var output: OutputStream
    lateinit var inputStream: InputStreamReader
    val communicationTaskList = ArrayList<CommunicationTask>()
    val sendTaskList = ArrayList<SendTask>()

    private val buttons = ArrayList<ArrayList<ImageView>>()
    private val bigButtons = ArrayList<ArrayList<ImageView>>()
    private lateinit var gameState: GameState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board)

        //Ustawienie nazwy pokoju
        val intent = intent
        val roomName = intent.getStringExtra("EXTRA_ROOM_NAME")
        supportActionBar!!.title = roomName

        //Łączenie z serwerem
        ConnectTask(this, roomName).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

        //Generowanie małych planszy
        for(y in 0..8) {
            val arrayList = ArrayList<ImageView>()
            for(x in 0..8) {
                val button = layoutInflater.inflate(R.layout.button, gridLayoutBoard, false)
                gridLayoutBoard.addView(button)
                val params = GridLayout.LayoutParams(GridLayout.spec(y, 1f), GridLayout.spec(x, 1f))
                params.width = 0
                params.height = 0
                if(x == 0 || x == 3 || x == 6)
                    params.leftMargin = resources.getDimensionPixelSize(R.dimen.margin3dp)
                else
                    params.leftMargin = resources.getDimensionPixelSize(R.dimen.margin1dp)
                if(y == 0 || y == 3 || y == 6)
                    params.topMargin = resources.getDimensionPixelSize(R.dimen.margin3dp)
                else
                    params.topMargin = resources.getDimensionPixelSize(R.dimen.margin1dp)
                if(x == 8)
                    params.rightMargin = resources.getDimensionPixelSize(R.dimen.margin3dp)
                if(y == 8)
                    params.bottomMargin = resources.getDimensionPixelSize(R.dimen.margin3dp)

                button.layoutParams = params
                arrayList += button as ImageView
            }
            buttons += arrayList
        }
        //Generowanie dużej planszy
        for(y in 0..2) {
            val arrayList = ArrayList<ImageView>()
            for(x in 0..2) {
                val button = layoutInflater.inflate(R.layout.button, gridLayoutBoard, false)
                gridLayoutBoard.addView(button)
                val params = GridLayout.LayoutParams(GridLayout.spec(y*3, 3, 1f), GridLayout.spec(x*3, 3, 1f))
                params.width = 0
                params.height = 0
                params.leftMargin = resources.getDimensionPixelSize(R.dimen.margin3dp)
                params.topMargin = resources.getDimensionPixelSize(R.dimen.margin3dp)
                if(x == 2)
                    params.rightMargin = resources.getDimensionPixelSize(R.dimen.margin3dp)
                if(y == 2)
                    params.bottomMargin = resources.getDimensionPixelSize(R.dimen.margin3dp)

                button.layoutParams = params
                button.visibility = View.GONE
                arrayList += button as ImageView
            }
            bigButtons += arrayList
        }

        //Obsługa kliknięć w guziki
        for(row in buttons) {
            for(button in row) {
                button.setOnClickListener {
                    try {
                        if(gameState.winner == "-" && gameState.you == gameState.move) {
                            val markedY = gameState.marked/3
                            val markedX = gameState.marked%3
                            val markedYRange = (markedY*3)..(markedY*3 +2)
                            val markedXRange = (markedX*3)..(markedX*3 +2)
                            if(gameState.marked == -1 || (markedYRange.contains(buttons.indexOf(row)) && markedXRange.contains(row.indexOf(button)))) {
                                val task = SendTask(this@BoardActivity, PacketSET(params = ParamsSET(row.indexOf(button), buttons.indexOf(row)), time = (System.currentTimeMillis()/1000L).toInt()))
                                sendTaskList.add(task)
                                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                            }
                            //else if(markedYRange.contains(buttons.indexOf(row)) && markedXRange.contains(row.indexOf(button)))
                                //SendTask(this@BoardActivity, PacketSET(params = ParamsSET(row.indexOf(button), buttons.indexOf(row)), time = (System.currentTimeMillis()/1000L).toInt())).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                        }
                    }
                    catch(e: UninitializedPropertyAccessException) {
                        Toast.makeText(this@BoardActivity, R.string.warningInvalidGame, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    //Aktualizacja stanu gry
    fun updateGameState(state: PacketSTT) {
        val boardState = ArrayList<ArrayList<Char>>()
        val bigBoardState = ArrayList<ArrayList<Char>>()

        //Konwersja Stringów na ArrayListy
        for(y in 0..8) {
            boardState.add(ArrayList())
            for(x in 0..8)
                boardState[y].add(state.params.board[y*9 + x])
        }
        for(y in 0..2) {
            bigBoardState.add(ArrayList())
            for(x in 0..2)
                bigBoardState[y].add(state.params.bigBoard[y*3 + x])
        }

        //Ustawianie ikon na małych planszach
        for(y in 0..8) {
            for(x in 0..8) {
                if(boardState[y][x] == 'X')
                    buttons[y][x].setImageDrawable(resources.getDrawable(R.drawable.ic_x_icon_24dp))
                else if(boardState[y][x] == 'O')
                    buttons[y][x].setImageDrawable(resources.getDrawable(R.drawable.ic_o_icon_24dp))
            }
        }

        //Ustawianie ikon na dużej planszy
        for(yB in 0..2) {
            for(xB in 0..2) {
                if(bigBoardState[yB][xB] != '-') {
                    for(y in (3*yB)..(3*yB + 2)) {
                        for(x in (3*xB)..(3*xB + 2))
                            buttons[y][x].visibility = View.GONE
                    }
                    bigButtons[yB][xB].visibility = View.VISIBLE

                    val drawableXId = R.drawable.ic_x_icon_24dp
                    val drawableOId = R.drawable.ic_o_icon_24dp
                    bigButtons[yB][xB].setImageDrawable(resources.getDrawable(when(bigBoardState[yB][xB] == 'X') {
                                                                                  true -> drawableXId
                                                                                  false -> drawableOId
                                                                              }))
                }
            }
        }

        //Ustawianie aktywnych pól
        val markedY = state.params.marked/3
        val markedX = state.params.marked%3
        for(row in buttons) {
            for(button in row) {
                button.setBackgroundColor(resources.getColor(R.color.colorBackground))
            }
        }
        if(state.params.whoWon == "-" && state.params.move == state.params.you) {
            if(state.params.marked == -1) {
                for(row in buttons) {
                    for(button in row) {
                        button.setBackgroundColor(resources.getColor(R.color.colorAccent))
                    }
                }
            }
            else {
                for(y in (3*markedY)..(3*markedY + 2)) {
                    for(x in (3*markedX)..(3*markedX + 2)) buttons[y][x].setBackgroundColor(resources.getColor(R.color.colorAccent))
                }
            }
        }

        //Ustawianie aktywnego gracza/zwycięzcy
        if(state.params.whoWon == "-") {
            textView.text = when(state.params.move) {
                state.params.you -> resources.getString(R.string.activePlayerYou)
                else -> resources.getString(R.string.activePlayerOpponent)
            }
        }
        else
            textView.text = resources.getString(R.string.winner) + state.params.whoWon

        gameState = GameState(state.params.whoWon, state.params.you, state.params.move, state.params.marked)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item!!.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onBackPressed() {
        CancelTask(this@BoardActivity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    //Dialog "Proszę czekać"
    override fun onCreateDialog(dialogId: Int): Dialog? {
        val dialog = ProgressDialog(this)
        when(dialogId) {
            connectDialogId -> {
                dialog.setTitle(R.string.dialogJoinTitle)
                dialog.setMessage(resources.getString(R.string.dialogJoinDescription))
                dialog.setCancelable(true)
            }
            else -> {
                dialog.setTitle(R.string.dialogDisconnectTitle)
                dialog.setMessage(resources.getString(R.string.dialogDisconnectDescription))
                dialog.setCancelable(false)
            }
        }
        return dialog
    }

    //Deserializacja pakietu z serwera w obiekt
    fun deserializePacketFromServer(input: String): Packet {
        //val packetTypeJON = object: TypeToken<PacketJON>() {}.type
        //val packetTypeSET = object: TypeToken<PacketSET>() {}.type
        val packetTypeSTT = object: TypeToken<PacketSTT>() {}.type
        val packetTypeVER = object: TypeToken<PacketVER>() {}.type
        val packetTypeBadErrDbgUin = object: TypeToken<PacketBadErrDbgUin>() {}.type
        val packetTypeGetPngPog = object: TypeToken<PacketGetPngPog>() {}.type

        val tempPacket = Gson().fromJson<PacketGetPngPog>(input, packetTypeGetPngPog)

        return when(tempPacket.method) {
            //"JON" -> Gson().fromJson<PacketJON>(input, packetTypeJON)
            //"SET" -> Gson().fromJson<PacketSET>(input, packetTypeSET)
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
