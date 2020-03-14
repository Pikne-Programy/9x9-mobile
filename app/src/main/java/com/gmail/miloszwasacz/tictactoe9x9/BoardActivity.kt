package com.gmail.miloszwasacz.tictactoe9x9

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.gridlayout.widget.GridLayout
import kotlinx.android.synthetic.main.activity_board.*

class BoardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board)

        val buttons = ArrayList<ArrayList<ImageView>>()

        for (y in 0..8) {
            val arrayList = ArrayList<ImageView>()
            for (x in 0..8) {
                val button = layoutInflater.inflate(R.layout.button, gridLayout, false)
                gridLayout.addView(button)
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
                if (x == 8)
                    params.rightMargin = resources.getDimensionPixelSize(R.dimen.margin3dp)
                if (y == 8)
                    params.bottomMargin = resources.getDimensionPixelSize(R.dimen.margin3dp)

                button.layoutParams = params
                arrayList += button as ImageView
            }
            buttons += arrayList
        }

        for(row in buttons) {
            for(button in row) {
                button.setOnClickListener {

                }
            }
        }
    }
}
