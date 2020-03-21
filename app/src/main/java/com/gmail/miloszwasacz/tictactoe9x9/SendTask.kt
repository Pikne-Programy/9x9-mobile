package com.gmail.miloszwasacz.tictactoe9x9

/*class SendTask(viewModel: CommunicationViewModel, private var packet: Packet): AsyncTask<Void, Void?, Void?>() {
    private var output = viewModel.output

    override fun doInBackground(vararg arg0: Void): Void? {
        val packetJSON = Gson().toJson(packet)
        output.write(packetJSON.toByteArray(charset("UTF-8")))

        Log.i("status", "SENT: $packetJSON")

        return null
    }
}*/