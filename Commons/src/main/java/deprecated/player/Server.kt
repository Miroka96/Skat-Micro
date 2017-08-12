package skat.player

import java.net.ServerSocket
import java.net.Socket
import java.util.*

/**
 * Created by mirko on 04.03.16.
 */
class Server : Runnable {
    var server: ServerSocket? = null
    var listener: Thread? = null
    val clients = ArrayList<Socket>(3)

    constructor(port: Int) {
        try {
            server = ServerSocket(port)
            listener = Thread(this)
            listener!!.start()
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    fun pullClient(): Socket {
        synchronized(clients) {
            val ret = clients[0]
            clients.removeAt(0)
            return ret
        }
    }

    override fun run() {
        try {
            while (true) {
                synchronized(clients) {
                    clients.add(server!!.accept())
                }
            }
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }


}