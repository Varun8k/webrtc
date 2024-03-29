import android.util.Log
import com.codewithkael.webrtcprojectforrecord.models.MessageModel
import com.codewithkael.webrtcprojectforrecord.utils.Constants.ANSWER_RECEIVED
import com.codewithkael.webrtcprojectforrecord.utils.Constants.CALL_RESPONSE
import com.codewithkael.webrtcprojectforrecord.utils.Constants.ICE_CANDIDATE
import com.codewithkael.webrtcprojectforrecord.utils.Constants.OFFER_RECEIVED
import com.codewithkael.webrtcprojectforrecord.utils.Constants.USER_ALREADY_EXISTS
import com.codewithkael.webrtcprojectforrecord.utils.NewMessageInterface
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONException
import org.json.JSONObject
import java.net.URISyntaxException

class SocketRepository(private val messageInterface: NewMessageInterface) {
    private var socket: Socket? = null
    private var userName: String? = null
    private val TAG = "SocketRepository"

    fun initSocket(username: String) {
        userName = username

        val serverUrl = "http://192.168.1.70:3000"
        try {
            val options = IO.Options()
            options.reconnection = true
            options.forceNew = true
            socket = IO.socket(serverUrl,options)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Socket connected")
                sendMessageToSocket(MessageModel("store_user", username, null, null))
            }
            socket?.on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "Socket disconnected")
            }
            socket?.on(Socket.EVENT_CONNECT_ERROR) {
                val e = it[0] as Exception
                Log.e(TAG, "Socket connection error: $e")
            }
            socket?.on(CALL_RESPONSE) { args ->
                args?.let { value ->
                    if (value.isNotEmpty()) {
                        val message = value[0]
                        messageInterface.onNewMessage(
                            Gson().fromJson(
                                message.toString(),
                                MessageModel::class.java
                            )
                        )
                    }
                }
            }
            socket?.on(OFFER_RECEIVED) { args ->
                args?.let { value ->
                    if (value.isNotEmpty()) {
                        val message = value[0]
                        messageInterface.onNewMessage(
                            Gson().fromJson(
                                message.toString(),
                                MessageModel::class.java
                            )
                        )
                    }
                }
            }
            socket?.on(USER_ALREADY_EXISTS) { args ->
                args?.let { value ->
                    if (value.isNotEmpty()) {
                        val message = value[0]
                        messageInterface.onNewMessage(
                            Gson().fromJson(
                                message.toString(),
                                MessageModel::class.java
                            )
                        )
                    }
                }
            }
            socket?.on(ANSWER_RECEIVED) { args ->
                args?.let { value ->
                    if (value.isNotEmpty()) {
                        val message = value[0]
                        messageInterface.onNewMessage(
                            Gson().fromJson(
                                message.toString(),
                                MessageModel::class.java
                            )
                        )
                    }
                }
            }
            socket?.on(ICE_CANDIDATE) { args ->
                args?.let { value ->
                    if (value.isNotEmpty()) {
                        val message = value[0]
                        messageInterface.onNewMessage(
                            Gson().fromJson(
                                message.toString(),
                                MessageModel::class.java
                            )
                        )
                    }
                }
            }
            socket?.connect()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    fun sendMessageToSocket(message: MessageModel) {
        try {
            Log.d(TAG, "sendMessageToSocket: $message")
            val jsonMessage = JSONObject().apply {
                put("type", message.type)
                put("name", message.name)
                put("target", message.target)
                putOpt("data", message.data)
            }
            socket?.emit("message", jsonMessage)
        } catch (e: Exception) {
            Log.e(TAG, "sendMessageToSocket error: $e")
        }
    }

    fun disconnect(){
        socket?.disconnect()
        socket?.off()
    }
}