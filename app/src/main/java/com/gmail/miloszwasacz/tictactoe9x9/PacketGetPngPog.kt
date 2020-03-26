package com.gmail.miloszwasacz.tictactoe9x9

class PacketGetPngPog(status: Int = 0, method: String, val params: ParamsGetPngPog, time: Int): Packet(status, method, time)