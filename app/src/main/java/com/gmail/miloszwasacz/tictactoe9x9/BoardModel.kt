package com.gmail.miloszwasacz.tictactoe9x9

data class BoardModel(var board: ArrayList<ArrayList<Char>>, var bigBoard: ArrayList<ArrayList<Char>>, var whoWon: String, var you: String, var move: String, var marked: Int)