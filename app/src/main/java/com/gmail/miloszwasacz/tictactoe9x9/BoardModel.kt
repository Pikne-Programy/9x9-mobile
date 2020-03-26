package com.gmail.miloszwasacz.tictactoe9x9

data class BoardModel(val board: ArrayList<ArrayList<Char>>, val bigBoard: ArrayList<ArrayList<Char>>, val whoWon: String, val you: String, val move: String, val lastMove: LastMove?, val marked: Int)