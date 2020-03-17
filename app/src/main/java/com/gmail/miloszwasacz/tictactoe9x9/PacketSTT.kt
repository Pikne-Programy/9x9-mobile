package com.gmail.miloszwasacz.tictactoe9x9

class PacketSTT(status: Int = 0, method: String, var params: ParamsSTT, time: Int): Packet(status, method, time)