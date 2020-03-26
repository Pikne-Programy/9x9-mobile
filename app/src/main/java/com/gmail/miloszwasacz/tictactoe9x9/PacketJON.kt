package com.gmail.miloszwasacz.tictactoe9x9

class PacketJON(status: Int = 0, method: String = "JON", val params: ParamsJON, time: Int): Packet(status, method, time)