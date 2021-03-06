@file:Suppress("DEPRECATION", "SetTextI18n", "InflateParams")
package com.gmail.miloszwasacz.tictactoe9x9

import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Vibrator
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.gridlayout.widget.GridLayout
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_board.*


class BoardActivity: AppCompatActivity() {
    private val buttons = ArrayList<ArrayList<ImageView>>()
    private val bigButtons = ArrayList<ArrayList<ImageView>>()
    private lateinit var gameState: BoardModel
    private lateinit var viewModel: CommunicationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board)

        //Ustawienie nazwy pokoju
        val intent = intent
        val roomName = intent.getStringExtra("EXTRA_ROOM_NAME")
        supportActionBar!!.title = roomName

        //Ustawianie ViewModelu
        val model: CommunicationViewModel by viewModels()
        viewModel = model
        model.roomName = roomName

        //Dialogi
        model.dialog.observe(this@BoardActivity, Observer {event ->
            for(dialog in model.dialogs) {
                if(dialog.isShowing) {
                    dialog.dismiss()
                }
                model.dialogs.remove(dialog)
            }
            event?.getContent()?.let {
                val dialog: Dialog
                when(it) {
                    DialogType.CONNECT -> {
                        dialog = ProgressDialog(ContextThemeWrapper(this@BoardActivity, theme))
                        dialog.setTitle(R.string.dialog_connect_title)
                        dialog.setMessage(resources.getString(R.string.dialog_connect_description))
                        dialog.setCancelable(true)
                        dialog.setOnCancelListener {
                            viewModel.dialog.value = Event(null)
                            finish()
                        }
                    }
                    DialogType.JOIN -> {
                        dialog = ProgressDialog(ContextThemeWrapper(this@BoardActivity, theme))
                        dialog.setTitle(R.string.dialog_join_title)
                        dialog.setMessage(resources.getString(R.string.dialog_join_description))
                        dialog.setCancelable(true)
                        dialog.setOnCancelListener {
                            viewModel.dialog.value = Event(null)
                            finish()
                        }
                    }
                    DialogType.WRONG_PROTOCOL -> {
                        dialog = AlertDialog.Builder(ContextThemeWrapper(this@BoardActivity, theme)).create()
                        dialog.setTitle(R.string.dialog_wrong_protocol_title)
                        dialog.setMessage(resources.getString(R.string.dialog_wrong_protocol_description_other))
                        dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.button_play), DialogInterface.OnClickListener {_, _ ->
                            model.sendJON()
                            viewModel.dialog.value = Event(DialogType.JOIN)
                        })
                        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel), DialogInterface.OnClickListener {_, _ ->
                            viewModel.dialog.value = Event(null)
                            finish()
                        })
                        dialog.setOnCancelListener {
                            viewModel.dialog.value = Event(null)
                            finish()
                        }
                    }
                    DialogType.OLD_PROTOCOL -> {
                        dialog = AlertDialog.Builder(ContextThemeWrapper(this@BoardActivity, theme)).create()
                        dialog.setTitle(R.string.dialog_wrong_protocol_title)
                        dialog.setMessage(resources.getString(R.string.dialog_wrong_protocol_description_old))
                        dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.button_play), DialogInterface.OnClickListener {_, _ ->
                            model.sendJON()
                            viewModel.dialog.value = Event(DialogType.JOIN)
                        })
                        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel), DialogInterface.OnClickListener {_, _ ->
                            viewModel.dialog.value = Event(null)
                            finish()
                        })
                        dialog.setOnCancelListener {
                            viewModel.dialog.value = Event(null)
                            finish()
                        }
                    }
                }
                model.dialogs.add(dialog)
                dialog.show()
            }
        })

        //Łączenie z serwerem
        if(savedInstanceState == null) {
            model.connect()
        }

        //Obsługa aktualnego stanu gry
        model.currentGameState.observe(this@BoardActivity, Observer { event ->
            event?.getContent()?.let {
                updateGameState(it)
            }
        })

        //Obsługa socketa w razie błędu
        model.wrongSocket.observe(this@BoardActivity, Observer { event ->
            event?.getContent()?.let {
                if(it) {
                    Toast.makeText(this@BoardActivity, R.string.warning_connection_error, Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        })
        model.serverError.observe(this@BoardActivity, Observer { event ->
            event?.getContent()?.let {
                if(it) {
                    Toast.makeText(this@BoardActivity, R.string.warning_server_error, Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        })

        //Generowanie małych planszy
        for(y in 0..8) {
            val arrayList = ArrayList<ImageView>()
            for(x in 0..8) {
                val button = layoutInflater.inflate(R.layout.button, gridLayoutBoard, false) as ImageView
                gridLayoutBoard.addView(button)
                val params = GridLayout.LayoutParams(GridLayout.spec(y, 1f), GridLayout.spec(x, 1f))
                params.width = 0
                params.height = 0
                if(x%3 == 0)
                    params.leftMargin = resources.getDimensionPixelSize(R.dimen.margin3dp)
                else
                    params.leftMargin = resources.getDimensionPixelSize(R.dimen.margin1dp)
                if(y%3 == 0)
                    params.topMargin = resources.getDimensionPixelSize(R.dimen.margin3dp)
                else
                    params.topMargin = resources.getDimensionPixelSize(R.dimen.margin1dp)
                if(x == 8)
                    params.rightMargin = resources.getDimensionPixelSize(R.dimen.margin3dp)
                if(y == 8)
                    params.bottomMargin = resources.getDimensionPixelSize(R.dimen.margin3dp)

                button.layoutParams = params

                //Kolor tła i ikonki
                val typedValue = TypedValue()
                if(theme.resolveAttribute(android.R.attr.windowBackground, typedValue, true)) {
                    button.setBackgroundColor(typedValue.data)
                }
                button.setColorFilter(ContextCompat.getColor(this@BoardActivity, when(PreferenceManager.getDefaultSharedPreferences(this@BoardActivity).getString(getString(R.string.key_theme), "AppTheme")) {
                    getString(R.string.theme_value_dark) -> android.R.color.white
                    else -> android.R.color.black
                }), android.graphics.PorterDuff.Mode.SRC_IN)

                arrayList += button
            }
            buttons += arrayList
        }
        //Generowanie dużej planszy
        for(y in 0..2) {
            val arrayList = ArrayList<ImageView>()
            for(x in 0..2) {
                val button = layoutInflater.inflate(R.layout.button, gridLayoutBoard, false) as ImageView
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

                //Kolor tła i ikonki
                val typedValue = TypedValue()
                if(theme.resolveAttribute(android.R.attr.windowBackground, typedValue, true)) {
                    button.setBackgroundColor(typedValue.data)
                }
                button.setColorFilter(ContextCompat.getColor(this@BoardActivity, when(PreferenceManager.getDefaultSharedPreferences(this@BoardActivity).getString(getString(R.string.key_theme), "AppTheme")) {
                    getString(R.string.theme_value_dark) -> android.R.color.white
                    else -> android.R.color.black
                }), android.graphics.PorterDuff.Mode.SRC_IN)

                button.visibility = View.INVISIBLE
                arrayList += button
            }
            bigButtons += arrayList
        }

        //Obsługa kliknięć w guziki
        for(row in buttons) {
            for(button in row) {
                button.setOnClickListener {
                    if(button.drawable == null) {
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
                            Toast.makeText(this@BoardActivity, R.string.warning_invalid_board, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    //Aktualizacja stanu gry
    private fun updateGameState(state: BoardModel) {
        val indexRange = 0..8

        //Resetowanie planszy
        for(row in bigButtons) {
            for(button in row) {
                button.setImageDrawable(null)
                button.visibility = View.INVISIBLE
            }
        }
        for(row in buttons) {
            for(button in row) {
                button.setImageDrawable(null)
                button.visibility = View.VISIBLE
            }
        }

        //Ustawianie ikon na małych planszach
        for(y in indexRange) {
            for(x in 0..8) {
                when {
                    state.board[y][x] == 'X' -> buttons[y][x].setImageDrawable(resources.getDrawable(R.drawable.ic_x_icon_24dp))
                    state.board[y][x] == 'O' -> buttons[y][x].setImageDrawable(resources.getDrawable(R.drawable.ic_o_icon_24dp))
                }
            }
        }

        //Ustawianie ikon na dużej planszy
        for(yB in 0..2) {
            for(xB in 0..2) {
                if(state.bigBoard[yB][xB] != '-' && state.bigBoard[yB][xB] != '+') {
                    for(y in (3*yB)..(3*yB + 2)) {
                        for(x in (3*xB)..(3*xB + 2)) buttons[y][x].visibility = View.INVISIBLE
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
                val typedValue = TypedValue()
                if(theme.resolveAttribute(android.R.attr.windowBackground, typedValue, true)) {
                    button.setBackgroundColor(typedValue.data)
                }
            }
        }
        if(state.whoWon == "-") {
            if(state.marked == -1) {
                for(row in buttons) {
                    for(button in row) {
                        if(state.bigBoard[buttons.indexOf(row)/3][row.indexOf(button)/3] != '+') {
                            button.setBackgroundColor(resources.getColor(when(state.move) {
                                                                             state.you -> R.color.colorAccent
                                                                             else -> R.color.colorAccentLight
                                                                         }))
                        }
                    }
                }
            }
            else if(indexRange.contains(markedY) && indexRange.contains(markedX)) {
                for(y in (3*markedY)..(3*markedY + 2)) {
                    for(x in (3*markedX)..(3*markedX + 2)) {
                        buttons[y][x].setBackgroundColor(resources.getColor(when(state.move) {
                                                                                state.you -> R.color.colorAccent
                                                                                else -> R.color.colorAccentLight
                                                                            }))
                    }
                }
            }
        }

        //Ustawianie kolorów ikonek na buttonach
        for(row in buttons) {
            for(button in row) {
                button.setColorFilter(ContextCompat.getColor(this@BoardActivity, when((button.background as ColorDrawable).color) {
                    getColor(R.color.colorAccent) -> android.R.color.black
                    getColor(R.color.colorAccentLight) -> android.R.color.black
                    else -> {
                        when(PreferenceManager.getDefaultSharedPreferences(this@BoardActivity).getString(getString(R.string.key_theme), "AppTheme")) {
                            getString(R.string.theme_value_dark) -> android.R.color.white
                            else -> android.R.color.black
                        }
                    }
                }), android.graphics.PorterDuff.Mode.SRC_IN)
            }
        }
        for(row in bigButtons) {
            for(button in row) {
                button.setColorFilter(ContextCompat.getColor(this@BoardActivity, when(PreferenceManager.getDefaultSharedPreferences(this@BoardActivity).getString(getString(R.string.key_theme), "AppTheme")) {
                    getString(R.string.theme_value_dark) -> android.R.color.white
                    else -> android.R.color.black
                }), android.graphics.PorterDuff.Mode.SRC_IN)
            }
        }

        //Ustawianie ostatniego ruchu
        if(state.lastMove != null) {
            val x = state.lastMove.x
            val y = state.lastMove.y
            if(indexRange.contains(x) && indexRange.contains(y)) {
                if(buttons[y][x].visibility == View.INVISIBLE) {
                    bigButtons[y/3][x/3].setColorFilter(ContextCompat.getColor(this@BoardActivity, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN)
                }
                else {
                    buttons[y][x].setColorFilter(ContextCompat.getColor(this@BoardActivity, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN)
                }
            }
        }

        //Ustawianie aktywnego gracza/zwycięzcy
        textViewYou.text = resources.getString(R.string.label_player_you) + state.you
        when(state.whoWon) {
            //Remis
            "+" -> {
                textViewActivePlayer.text = resources.getString(R.string.label_tie)
            }
            //Następna tura
            "-" -> {
                textViewActivePlayer.text = when(state.move) {
                    state.you -> resources.getString(R.string.label_active_player_you)
                    else -> resources.getString(R.string.label_active_player_opponent)
                }
            }
            //Ktoś wygrał
            else -> {
                textViewActivePlayer.text = resources.getString(R.string.label_winner) + state.whoWon
            }
        }

        //Wysyłanie powiadomienia
        if(state.move == state.you) {
            val mediaPlayer = MediaPlayer.create(this@BoardActivity, R.raw.your_move)
            mediaPlayer.setOnCompletionListener {
                mediaPlayer.release()
            }
            val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val pattern = longArrayOf(0, 100, 1000)
            when(PreferenceManager.getDefaultSharedPreferences(this@BoardActivity).getString(getString(R.string.key_notifications), "Sound")) {
                //Dźwięk i wibracje
                getString(R.string.notifications_value_all) -> {
                    mediaPlayer.start()
                    v.vibrate(pattern, -1)
                }
                //Dźwięk
                getString(R.string.notifications_value_sound) -> {
                    mediaPlayer.start()
                }
                //Wibracje
                getString(R.string.notifications_value_vibration) -> {
                    v.vibrate(pattern, -1)
                }
            }
        }

        gameState = state
    }

    //Updatowanie Theme'a
    override fun onResume() {
        super.onResume()
        when(PreferenceManager.getDefaultSharedPreferences(this@BoardActivity).getString(getString(R.string.key_theme), "AppTheme")) {
            getString(R.string.theme_value_dark) -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        delegate.applyDayNight()
    }
}
