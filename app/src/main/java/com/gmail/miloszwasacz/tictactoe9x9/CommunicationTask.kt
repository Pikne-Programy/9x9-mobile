@file:Suppress("InflateParams", "SetTextI18n")
package com.gmail.miloszwasacz.tictactoe9x9

import android.os.AsyncTask
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetSocketAddress

class CommunicationTask(private val activity: BoardActivity, private val inputPacket: Packet?/*, private var socket: Socket, private var output: OutputStream, private var inputStream: InputStreamReader*/): AsyncTask<Void, Void?, Packet>() {
    private lateinit var input: BufferedReader
    private val socket = activity.socket
    private var output = activity.output
    private var inputStream = activity.inputStream

    override fun onPreExecute() {
        if(!socket.isConnected) {
            socket.connect(InetSocketAddress(activity.serverIP, activity.serverPORT))
            output = socket.getOutputStream()
            inputStream = InputStreamReader(socket.getInputStream())
        }
        input = BufferedReader(inputStream)

        if(inputPacket != null) {
            if(inputPacket is PacketSTT)
                activity.updateGameState(inputPacket)
        }
    }

    override fun doInBackground(vararg arg0: Void): Packet {
        return if(!isCancelled) {
            val packetString = input.readLine()
            val packet = activity.deserializePacketFromServer(packetString)

            Log.i("packet", packetString)
            packet
        }
        else PacketBadErrDbgUin(method = "DBG", params = ParamsBadErrDbgUin("Task cancelled"), time = (System.currentTimeMillis()/1000L).toInt())
    }

    override fun onPostExecute(resultPacket: Packet) {
        if(!isCancelled) {
            //Zupdatowanie planszy
            when(resultPacket) {
                is PacketSTT -> {
                    activity.updateGameState(resultPacket)
                    val task = CommunicationTask(activity, null)
                    activity.communicationTaskList.add(task)
                    task.execute()
                    //CommunicationTask(activity, null).execute()
                }
                //Wysłanie odpowiedzi na pakiet "PNG"
                is PacketGetPngPog -> {
                    if(resultPacket.method == "PNG") {
                        val sendTask = SendTask(activity, PacketGetPngPog(method = "POG", params = ParamsGetPngPog(), time = (System.currentTimeMillis()/1000L).toInt())/*, socket, output*/)
                        activity.sendTaskList.add(sendTask)
                        sendTask.execute()
                    }
                    val task = CommunicationTask(activity, null)
                    activity.communicationTaskList.add(task)
                    task.execute()
                    //CommunicationTask(activity, null).execute()
                }
                //Wyświetlanie info o oprogramowaniu
                is PacketVER -> {
                    val linearLayout = activity.layoutInflater.inflate(R.layout.dialog_version, null, false) as LinearLayout
                    val textViewName = linearLayout.findViewById<TextView>(R.id.textViewName)
                    val textViewAuthor = linearLayout.findViewById<TextView>(R.id.textViewAuthor)
                    val textViewVersion = linearLayout.findViewById<TextView>(R.id.textViewVersion)
                    val textViewFullName = linearLayout.findViewById<TextView>(R.id.textViewFullName)
                    val textViewProtocolVersion = linearLayout.findViewById<TextView>(R.id.textViewProtocolVersion)
                    val textViewNick = linearLayout.findViewById<TextView>(R.id.textViewNick)
                    val textViewFullNick = linearLayout.findViewById<TextView>(R.id.textViewFullNick)

                    textViewName.text = textViewName.text.toString() + resultPacket.params.name
                    textViewAuthor.text = textViewAuthor.text.toString() + resultPacket.params.author
                    textViewVersion.text = textViewVersion.text.toString() + resultPacket.params.version
                    textViewFullName.text = textViewFullName.text.toString() + resultPacket.params.fullName
                    textViewProtocolVersion.text = textViewProtocolVersion.text.toString() + resultPacket.params.protocolVersion
                    textViewNick.text = textViewNick.text.toString() + resultPacket.params.nick
                    textViewFullNick.text = textViewFullNick.text.toString() + resultPacket.params.fullNick

                    AlertDialog.Builder(activity).setTitle(activity.resources.getString(R.string.dialogVersionTitle)).setPositiveButton(activity.resources.getString(android.R.string.ok), null).setView(linearLayout).create().show()

                    val task = CommunicationTask(activity, null)
                    activity.communicationTaskList.add(task)
                    task.execute()
                    //CommunicationTask(activity, null).execute()
                }
                //Zapisywanie błędów itp. w Log'u
                else -> {
                    Log.i("packetMSG", (resultPacket as PacketBadErrDbgUin).params.msg)

                    val task = CommunicationTask(activity, null)
                    activity.communicationTaskList.add(task)
                    task.execute()
                    //CommunicationTask(activity, null).execute()
                }
            }
        }
        else {
            Log.i("packetMSG", (resultPacket as PacketBadErrDbgUin).params.msg)
        }
    }
}