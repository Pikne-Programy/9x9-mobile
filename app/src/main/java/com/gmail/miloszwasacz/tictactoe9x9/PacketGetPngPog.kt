package com.gmail.miloszwasacz.tictactoe9x9

class PacketGetPngPog(status: Int = 0, method: String, var params: ParamsGetPngPog, time: Int): Packet(status, method, time)