@file:Suppress("DEPRECATION", "SetTextI18n", "InflateParams")
package com.gmail.miloszwasacz.tictactoe9x9

import android.app.Dialog
import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.gridlayout.widget.GridLayout
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_board.*


class BoardActivity: AppCompatActivity() {
    private val buttons = ArrayList<ArrayList<ImageView>>()
    private val bigButtons = ArrayList<ArrayList<ImageView>>()
    private lateinit var gameState: BoardModel
    private lateinit var viewModel: CommunicationViewModel
    private var currentDialog = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board)

        //Ustawienie nazwy pokoju
        val intent = intent
        val roomName = intent.getStringExtra("EXTRA_ROOM_NAME")
        supportActionBar!!.title = roomName

        //Łączenie z serwerem
        val model: CommunicationViewModel by viewModels()
        viewModel = model
        currentDialog = viewModel.removeDialog
        model.dialogId.observe(this@BoardActivity, Observer { event ->
            event?.getContentIfNotHandledOrReturnNull()?.let {
                if(it == model.removeDialog) {
                    if(currentDialog != model.removeDialog) {
                        removeDialog(currentDialog)
                        currentDialog = model.removeDialog
                    }
                }
                else {
                    currentDialog = it
                    showDialog(currentDialog)
                }
            }
        })
        model.connect(roomName)
        model.currentGameState.observe(this@BoardActivity, Observer { event ->
            event?.getContentIfNotHandledOrReturnNull()?.let {
                updateGameState(it)
            }
        })

        //Generowanie małych planszy
        for(y in 0..8) {
            val arrayList = ArrayList<ImageView>()
            for(x in 0..8) {
                val button = layoutInflater.inflate(R.layout.button, gridLayoutBoard, false)
                gridLayoutBoard.addView(button)
                val params = GridLayout.LayoutParams(GridLayout.spec(y, 1f), GridLayout.spec(x, 1f))
                params.width = 0
                params.height = 0
                if(x%3 == 0 /*x == 0 || x == 3 || x == 6*/)
                    params.leftMargin = resources.getDimensionPixelSize(R.dimen.margin3dp)
                else
                    params.leftMargin = resources.getDimensionPixelSize(R.dimen.margin1dp)
                if(y%3 == 0 /*y == 0 || y == 3 || y == 6*/)
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
                        if(gameState.whoWon == "-" && gameState.you == gameState.move) {
                            val markedY = gameState.marked/3
                            val markedX = gameState.marked%3
                            val markedYRange = (markedY*3)..(markedY*3 + 2)
                            val markedXRange = (markedX*3)..(markedX*3 + 2)
                            if(gameState.marked == -1 || (markedYRange.contains(buttons.indexOf(row)) && markedXRange.contains(row.indexOf(button)))) {
                                model.sendMove(row.indexOf(button), buttons.indexOf(row))
                            }
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
    /*fun updateGameState(state: PacketSTT) {
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
        textViewYou.text = resources.getString(R.string.playerYou) + state.params.you
        if(state.params.whoWon == "-") {
            textViewActivePlayer.text = when(state.params.move) {
                state.params.you -> resources.getString(R.string.activePlayerYou)
                else -> resources.getString(R.string.activePlayerOpponent)
            }
        }
        else
            textViewActivePlayer.text = resources.getString(R.string.winner) + state.params.whoWon



        gameState = GameState(state.params.whoWon, state.params.you, state.params.move, state.params.marked)
    }*/
    private fun updateGameState(state: BoardModel) {
        //Ustawianie ikon na małych planszach
        for(y in 0..8) {
            for(x in 0..8) {
                if(state.board[y][x] == 'X')
                    buttons[y][x].setImageDrawable(resources.getDrawable(R.drawable.ic_x_icon_24dp))
                else if(state.board[y][x] == 'O')
                    buttons[y][x].setImageDrawable(resources.getDrawable(R.drawable.ic_o_icon_24dp))
            }
        }

        //Ustawianie ikon na dużej planszy
        for(yB in 0..2) {
            for(xB in 0..2) {
                if(state.bigBoard[yB][xB] != '-') {
                    for(y in (3*yB)..(3*yB + 2)) {
                        for(x in (3*xB)..(3*xB + 2))
                            buttons[y][x].visibility = View.GONE
                    }
                    bigButtons[yB][xB].visibility = View.VISIBLE

                    val drawableXId = R.drawable.ic_x_icon_24dp
                    val drawableOId = R.drawable.ic_o_icon_24dp
                    bigButtons[yB][xB].setImageDrawable(resources.getDrawable(when(state.bigBoard[yB][xB] == 'X') {
                                                                                  true -> drawableXId
                                                                                  false -> drawableOId
                                                                              }))
                }
            }
        }

        //Ustawianie aktywnych pól
        val markedY = state.marked/3
        val markedX = state.marked%3
        for(row in buttons) {
            for(button in row) {
                button.setBackgroundColor(resources.getColor(R.color.colorBackground))
            }
        }
        if(state.whoWon == "-" && state.move == state.you) {
            if(state.marked == -1) {
                for(row in buttons) {
                    for(button in row) {
                        button.setBackgroundColor(resources.getColor(R.color.colorAccent))
                    }
                }
            }
            else {
                for(y in (3*markedY)..(3*markedY + 2)) {
                    for(x in (3*markedX)..(3*markedX + 2)) {
                        buttons[y][x].setBackgroundColor(resources.getColor(R.color.colorAccent))
                    }
                }
            }
        }

        //Ustawianie aktywnego gracza/zwycięzcy
        textViewYou.text = resources.getString(R.string.playerYou) + state.you
        if(state.whoWon == "-") {
            textViewActivePlayer.text = when(state.move) {
                state.you -> resources.getString(R.string.activePlayerYou)
                else -> resources.getString(R.string.activePlayerOpponent)
            }
        }
        else {
            textViewActivePlayer.text = resources.getString(R.string.winner) + state.whoWon
        }

        gameState = state
    }

    /*
    //Obsługa strzałeczki w tył
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item!!.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //Wyjście z Activity
    override fun onBackPressed() {
        CloseSocketTask(this@BoardActivity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    //Rozłączenie się na onPause
    override fun onPause() {
        super.onPause()
        Toast.makeText(this@BoardActivity, R.string.dialogDisconnectTitle, Toast.LENGTH_SHORT).show()
        CloseSocketTask(this@BoardActivity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }*/

    //Tworzenie Dialog'ów
    override fun onCreateDialog(dialogId: Int): Dialog? {
        val dialog: Dialog
        when(dialogId) {
            //Łączenie z serwerem
            viewModel.connectDialogId -> {
                dialog = ProgressDialog(this@BoardActivity)
                dialog.setTitle(R.string.dialogJoinTitle)
                dialog.setMessage(resources.getString(R.string.dialogJoinDescription))
                dialog.setCancelable(true)
            }
            //Wersja oprogramowania
            else -> {
                val builder = AlertDialog.Builder(this@BoardActivity)
                val linearLayout = layoutInflater.inflate(R.layout.dialog_version, null, false) as LinearLayout
                val textViewName = linearLayout.findViewById<TextView>(R.id.textViewName)
                val textViewAuthor = linearLayout.findViewById<TextView>(R.id.textViewAuthor)
                val textViewVersion = linearLayout.findViewById<TextView>(R.id.textViewVersion)
                val textViewFullName = linearLayout.findViewById<TextView>(R.id.textViewFullName)
                val textViewProtocolVersion = linearLayout.findViewById<TextView>(R.id.textViewProtocolVersion)
                val textViewNick = linearLayout.findViewById<TextView>(R.id.textViewNick)
                val textViewFullNick = linearLayout.findViewById<TextView>(R.id.textViewFullNick)

                if(viewModel.versionPacket != null) {
                    textViewName.text = textViewName.text.toString() + viewModel.versionPacket!!.params.name
                    textViewAuthor.text = textViewAuthor.text.toString() + viewModel.versionPacket!!.params.author
                    textViewVersion.text = textViewVersion.text.toString() + viewModel.versionPacket!!.params.version
                    textViewFullName.text = textViewFullName.text.toString() + viewModel.versionPacket!!.params.fullName
                    textViewProtocolVersion.text = textViewProtocolVersion.text.toString() + viewModel.versionPacket!!.params.protocolVersion
                    textViewNick.text = textViewNick.text.toString() + viewModel.versionPacket!!.params.nick
                    textViewFullNick.text = textViewFullNick.text.toString() + viewModel.versionPacket!!.params.fullNick
                }

                builder
                    .setTitle(resources.getString(R.string.dialogVersionTitle))
                    .setPositiveButton(resources.getString(android.R.string.ok)) {_, _ ->
                        currentDialog = viewModel.removeDialog
                        viewModel.versionPacket = null
                    }
                    .setView(linearLayout)
                dialog = builder.create()
            }
        }
        return dialog
    }
}
