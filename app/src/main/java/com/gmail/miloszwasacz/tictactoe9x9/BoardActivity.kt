@file:Suppress("DEPRECATION")
package com.gmail.miloszwasacz.tictactoe9x9

import android.app.Dialog
import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.gridlayout.widget.GridLayout
import kotlinx.android.synthetic.main.activity_board.*


class BoardActivity: AppCompatActivity() {
    val serverIP = "85.198.250.135"
    val serverPORT = 4780

    private val buttons = ArrayList<ArrayList<ImageView>>()
    private val bigButtons = ArrayList<ArrayList<ImageView>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board)

        //Ustawienie nazwy pokoju
        val intent = intent
        val roomName = intent.getStringExtra("EXTRA_ROOM_NAME")
        supportActionBar!!.title = roomName

        //Łączenie z serwerem
        ConnectTask(this, roomName).execute()

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
    }

    //Dialog "Proszę czekać"
    override fun onCreateDialog(dialogId: Int): Dialog? {
        val dialog = ProgressDialog(this)
        dialog.setTitle(resources.getString(R.string.dialogJoinTitle))
        dialog.setMessage(resources.getString(R.string.dialogJoinDescription))
        dialog.setCancelable(false)
        return dialog
    }
}
