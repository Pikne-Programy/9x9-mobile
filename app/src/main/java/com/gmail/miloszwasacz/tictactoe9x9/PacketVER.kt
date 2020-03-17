package com.gmail.miloszwasacz.tictactoe9x9

class PacketVER(status: Int = 0, method: String = "VER", var params: ParamsVER, time: Int): Packet(status, method, time)