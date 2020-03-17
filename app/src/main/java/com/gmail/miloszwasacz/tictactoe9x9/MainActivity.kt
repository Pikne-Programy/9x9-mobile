package com.gmail.miloszwasacz.tictactoe9x9

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonJoin.setOnClickListener {
            val intent = Intent(this@MainActivity, BoardActivity::class.java)
            intent.putExtra("EXTRA_ROOM_NAME", when(editTextRoomName.text.toString().trim()) {
                "" -> "public"
                else -> editTextRoomName.text.toString().trim()
            })
            startActivity(intent)
        }
    }
}

