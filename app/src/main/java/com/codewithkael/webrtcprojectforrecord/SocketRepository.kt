import android.util.Log
import com.codewithkael.webrtcprojectforrecord.models.MessageModel
import com.codewithkael.webrtcprojectforrecord.utils.NewMessageInterface
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONException
import org.json.JSONObject

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
            socket = IO.socket(serverUrl, options)
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
//            socket?.on(Socket.EVENT_CONNECT_TIMEOUT) {
//                Log.e(TAG, "Socket connection timeout")
//            }
            socket?.on("message") { args ->
                val message = args[0] as JSONObject
                try {
                    val messageModel = MessageModel(
                        message.getString("type"),
                        message.getString("name"),
                        message.optString("data"),
                        message.optJSONObject("data")
                    )
                    messageInterface.onNewMessage(messageModel)
                } catch (e: JSONException) {
                    Log.e(TAG, "Error parsing message: $e")
                }
            }
            socket?.connect()
        } catch (e: Exception) {
            Log.e(TAG, "Socket connection error: $e")
        }
    }

    fun sendMessageToSocket(message: MessageModel) {
        try {
            Log.d(TAG, "sendMessageToSocket: $message")
            val jsonMessage = JSONObject().apply {
                put("type", message.type)
                put("name", message.name)
                putOpt("data", message.data)
            }
            socket?.emit("message", jsonMessage)
        } catch (e: Exception) {
            Log.e(TAG, "sendMessageToSocket error: $e")
        }
    }
}
