package com.glodanif.bluetoothchat.data.model

import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import com.glodanif.bluetoothchat.data.entity.ChatMessage
import com.glodanif.bluetoothchat.data.entity.Conversation
import com.glodanif.bluetoothchat.data.entity.Message
import com.glodanif.bluetoothchat.data.entity.MessageType
import com.glodanif.bluetoothchat.data.service.BluetoothConnectionService
import java.io.File

class BluetoothConnectorImpl(private val context: Context) : BluetoothConnector {

    private var prepareListener: OnPrepareListener? = null
    private var messageListener: OnMessageListener? = null
    private var connectListener: OnConnectionListener? = null
    private var fileListener: OnFileListener? = null

    private var service: BluetoothConnectionService? = null
    private var bound = false

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            service = (binder as BluetoothConnectionService.ConnectionBinder).getService()
            service?.setConnectionListener(connectionListenerInner)
            service?.setMessageListener(messageListenerInner)
            service?.setFileListener(fileListenerInner)

            bound = true
            prepareListener?.onPrepared()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            service?.setConnectionListener(null)
            service?.setMessageListener(null)
            service?.setFileListener(null)
            service = null

            bound = false
            prepareListener?.onError()
        }
    }

    private val connectionListenerInner = object : OnConnectionListener {

        override fun onConnected(device: BluetoothDevice) {
            connectListener?.onConnected(device)
        }

        override fun onConnectionWithdrawn() {
            connectListener?.onConnectionWithdrawn()
        }

        override fun onConnectionAccepted() {
            connectListener?.onConnectionAccepted()
        }

        override fun onConnectionRejected() {
            connectListener?.onConnectionRejected()
        }

        override fun onConnecting() {
            connectListener?.onConnecting()
        }

        override fun onConnectedIn(conversation: Conversation) {
            connectListener?.onConnectedIn(conversation)
        }

        override fun onConnectedOut(conversation: Conversation) {
            connectListener?.onConnectedOut(conversation)
        }

        override fun onConnectionLost() {
            connectListener?.onConnectionLost()
        }

        override fun onConnectionFailed() {
            connectListener?.onConnectionFailed()
        }

        override fun onDisconnected() {
            connectListener?.onDisconnected()
        }

        override fun onConnectionDestroyed() {
            connectListener?.onConnectionDestroyed()
        }
    }

    private val messageListenerInner = object : OnMessageListener {

        override fun onMessageReceived(message: ChatMessage) {
            messageListener?.onMessageReceived(message)
        }

        override fun onMessageSent(message: ChatMessage) {
            messageListener?.onMessageSent(message)
        }

        override fun onMessageDelivered(id: String) {
            messageListener?.onMessageDelivered(id)
        }

        override fun onMessageNotDelivered(id: String) {
            messageListener?.onMessageNotDelivered(id)
        }

        override fun onMessageSeen(id: String) {
            messageListener?.onMessageSeen(id)
        }
    }

    private val fileListenerInner = object : OnFileListener {

        override fun onFileSendingStarted(fileAddress: String?, fileSize: Long) {
            fileListener?.onFileSendingStarted(fileAddress, fileSize)
        }

        override fun onFileSendingProgress(sentBytes: Long, totalBytes: Long) {
            fileListener?.onFileSendingProgress(sentBytes, totalBytes)
        }

        override fun onFileSendingFinished() {
            fileListener?.onFileSendingFinished()
        }

        override fun onFileSendingFailed() {
            fileListener?.onFileSendingFailed()
        }

        override fun onFileReceivingStarted(fileSize: Long) {
            fileListener?.onFileReceivingStarted(fileSize)
        }

        override fun onFileReceivingProgress(sentBytes: Long, totalBytes: Long) {
            fileListener?.onFileReceivingProgress(sentBytes, totalBytes)
        }

        override fun onFileReceivingFinished() {
            fileListener?.onFileReceivingFinished()
        }

        override fun onFileReceivingFailed() {
            fileListener?.onFileReceivingFailed()
        }

        override fun onFileTransferCanceled(byPartner: Boolean) {
            fileListener?.onFileTransferCanceled(byPartner)
        }
    }

    override fun prepare() {
        service = null
        bound = false
        if (!BluetoothConnectionService.isRunning) {
            BluetoothConnectionService.start(context)
        }
        BluetoothConnectionService.bind(context, connection)
    }

    override fun release() {
        if (bound) {
            context.unbindService(connection)
            bound = false
            service = null
        }
    }

    override fun isConnectionPrepared(): Boolean {
        return bound
    }

    override fun setOnConnectListener(listener: OnConnectionListener?) {
        this.connectListener = listener
    }

    override fun setOnPrepareListener(listener: OnPrepareListener?) {
        this.prepareListener = listener
    }

    override fun setOnMessageListener(listener: OnMessageListener?) {
        this.messageListener = listener
    }

    override fun setOnFileListener(listener: OnFileListener?) {
        this.fileListener = listener
    }

    override fun connect(device: BluetoothDevice) {
        service?.connect(device)
    }

    override fun stop() {
        service?.stop()
    }

    override fun sendMessage(message: String) {
        val chatMessage = Message(System.nanoTime().toString(), message, Message.Type.MESSAGE)
        service?.sendMessage(chatMessage)
    }

    override fun sendFile(file: File) {
        service?.sendFile(file, MessageType.IMAGE)
    }

    override fun cancelFileTransfer() {
        service?.cancelFileTransfer()
    }

    override fun restart() {
        service?.disconnect()
    }

    override fun isConnected(): Boolean {
        return if (service == null) false else service!!.isConnected()
    }

    override fun isConnectedOrPending(): Boolean {
        return if (service == null) false else service!!.isConnectedOrPending()
    }

    override fun isPending(): Boolean {
        return if (service == null) false else service!!.isPending()
    }

    override fun getCurrentConversation(): Conversation? {
        return service?.getCurrentConversation()
    }

    override fun acceptConnection() {
        val settings = SettingsManagerImpl(context)
        val message = Message.createAcceptConnectionMessage(settings.getUserName(), settings.getUserColor())
        service?.sendMessage(message)
    }

    override fun rejectConnection() {
        val settings = SettingsManagerImpl(context)
        val message = Message.createRejectConnectionMessage(settings.getUserName(), settings.getUserColor())
        service?.sendMessage(message)
    }

    override fun sendDisconnectRequest() {
        val message = Message.createDisconnectMessage()
        service?.sendMessage(message)
    }
}