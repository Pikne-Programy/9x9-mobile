package com.gmail.miloszwasacz.tictactoe9x9

class PacketSTT(status: Int = 0, method: String = "STT", var params: ParamsSTT, time: Int): Packet(status, method, time)