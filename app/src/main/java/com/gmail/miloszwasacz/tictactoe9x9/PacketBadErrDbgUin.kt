package com.gmail.miloszwasacz.tictactoe9x9

class PacketBadErrDbgUin(status: Int = 0, method: String, val params: ParamsBadErrDbgUin, time: Int): Packet(status, method, time)