package com.gmail.miloszwasacz.tictactoe9x9

class PacketSET(status: Int = 0, method: String, var params: ParamsSET, time: Int): Packet(status, method, time)