package com.gmail.miloszwasacz.tictactoe9x9

data class BoardModel(var board: ArrayList<ArrayList<Char>>, var bigBoard: ArrayList<ArrayList<Char>>, var isEnded: Boolean, var whoWon: String, var you: String, var move: String, var lastMove: LastMove, var marked: Int)