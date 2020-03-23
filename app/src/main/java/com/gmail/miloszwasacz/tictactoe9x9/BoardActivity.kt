@file:Suppress("DEPRECATION", "SetTextI18n", "InflateParams")
package com.gmail.miloszwasacz.tictactoe9x9

import android.app.Application
import android.app.Dialog
import android.app.ProgressDialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
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
    private var currentDialog = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        when(PreferenceManager.getDefaultSharedPreferences(this@BoardActivity).getString(getString(R.string.key_theme), "AppTheme")) {
            getString(R.string.theme_dark) -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
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
            event?.getContent()?.let {
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
        if(savedInstanceState == null) {
            model.connect(roomName)
        }
        model.currentGameState.observe(this@BoardActivity, Observer { event ->
            event?.getContent()?.let {
                updateGameState(it)
            }
        })
        model.wrongSocket.observe(this@BoardActivity, Observer { event ->
            event?.getContent()?.let {
                if(it) {
                    Toast.makeText(this@BoardActivity, R.string.warning_invalid_ip_port, Toast.LENGTH_SHORT).show()
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

                //Kolor tła i ikonki
                val typedValue = TypedValue()
                if(theme.resolveAttribute(android.R.attr.windowBackground, typedValue, true)) {
                    button.setBackgroundColor(typedValue.data)
                }
                button.setColorFilter(ContextCompat.getColor(this@BoardActivity, when(PreferenceManager.getDefaultSharedPreferences(this@BoardActivity).getString(getString(R.string.key_theme), "AppTheme")) {
                    getString(R.string.theme_dark) -> android.R.color.white
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
                    getString(R.string.theme_dark) -> android.R.color.white
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
                            Toast.makeText(this@BoardActivity, R.string.warning_invalid_game, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    //Aktualizacja stanu gry
    private fun updateGameState(state: BoardModel) {
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
                            buttons[y][x].visibility = View.INVISIBLE
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
                        button.setBackgroundColor(resources.getColor(when(state.move) {
                                                      state.you -> R.color.colorAccent
                                                      else -> R.color.colorAccentLight
                                                  }))
                    }
                }
            }
            else {
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
        for(row in buttons) {
            for(button in row) {
                button.setColorFilter(ContextCompat.getColor(this@BoardActivity, when((button.background as ColorDrawable).color) {
                    getColor(R.color.colorAccent) -> android.R.color.black
                    getColor(R.color.colorAccentLight) -> android.R.color.black
                    else -> {
                        when(PreferenceManager.getDefaultSharedPreferences(this@BoardActivity).getString(getString(R.string.key_theme), "AppTheme")) {
                            getString(R.string.theme_dark) -> android.R.color.white
                            else -> android.R.color.black
                        }
                    }
                }), android.graphics.PorterDuff.Mode.SRC_IN)
            }
        }

        //Ustawianie aktywnego gracza/zwycięzcy
        textViewYou.text = resources.getString(R.string.label_player_you) + state.you
        if(state.whoWon == "-") {
            textViewActivePlayer.text = when(state.move) {
                state.you -> resources.getString(R.string.label_active_player_you)
                else -> resources.getString(R.string.label_active_player_opponent)
            }
        }
        else {
            textViewActivePlayer.text = resources.getString(R.string.label_winner) + state.whoWon
        }

        gameState = state
    }

    //Tworzenie Dialog'ów
    override fun onCreateDialog(dialogId: Int): Dialog? {
        val dialog: Dialog?
        when(dialogId) {
            //Łączenie z serwerem
            viewModel.connectDialogId -> {
                dialog = ProgressDialog(ContextThemeWrapper(this@BoardActivity, theme))
                dialog.setTitle(R.string.dialog_join_title)
                dialog.setMessage(resources.getString(R.string.dialog_join_description))
                dialog.setCancelable(true)
                dialog.setOnCancelListener {
                    finish()
                }
            }
            //Wersja oprogramowania
            viewModel.versionDialogId -> {
                val builder = AlertDialog.Builder(ContextThemeWrapper(this@BoardActivity, theme))
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
                    .setTitle(resources.getString(R.string.dialog_version_title))
                    .setPositiveButton(resources.getString(android.R.string.ok)) {_, _ ->
                        currentDialog = viewModel.removeDialog
                        viewModel.versionPacket = null
                    }
                    .setView(linearLayout)
                dialog = builder.create()
            }
            //Info o błędach i debugu
            else -> {
                if(PreferenceManager.getDefaultSharedPreferences(viewModel.getApplication()).getBoolean(viewModel.getApplication<Application>().getString(R.string.key_debug_info), false)) {
                    val builder = AlertDialog.Builder(ContextThemeWrapper(this@BoardActivity, theme))
                    builder.setTitle(when(viewModel.debugPacket?.method) {
                                         "ERR" -> R.string.dialog_debug_title_error
                                         else -> R.string.dialog_debug_title_debug
                                     }).setMessage(viewModel.debugPacket?.params!!.msg).setPositiveButton(resources.getString(android.R.string.ok)) {_, _ ->
                            currentDialog = viewModel.removeDialog
                        }
                    dialog = builder.create()
                }
                else {
                    dialog = null
                }
                viewModel.debugPacket = null
            }
        }
        return dialog
    }
}
