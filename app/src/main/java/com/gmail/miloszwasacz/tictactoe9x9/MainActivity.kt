package com.gmail.miloszwasacz.tictactoe9x9

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity: AppCompatActivity() {
    lateinit var serverIP: String
    var serverPort = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        serverIP = resources.getString(R.string.serverIP)
        serverPort = resources.getInteger(R.integer.serverPort)

        buttonJoin.setOnClickListener {
            //startActivity(Intent(this@MainActivity, BoardActivity::class.java))
            ConnectTask(this).execute()
        }
    }
}

