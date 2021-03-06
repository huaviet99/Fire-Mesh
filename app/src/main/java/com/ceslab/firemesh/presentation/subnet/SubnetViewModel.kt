package com.ceslab.firemesh.presentation.subnet

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ceslab.firemesh.meshmodule.bluetoothmesh.BluetoothMeshManager
import com.ceslab.firemesh.meshmodule.bluetoothmesh.MeshConnectionManager
import com.ceslab.firemesh.meshmodule.listener.ConnectionMessageListener
import com.ceslab.firemesh.meshmodule.listener.ConnectionStatusListener
import com.ceslab.firemesh.meshmodule.model.MeshStatus
import com.siliconlab.bluetoothmesh.adk.ErrorType
import timber.log.Timber
import javax.inject.Inject

class SubnetViewModel @Inject constructor(
    bluetoothMeshManager: BluetoothMeshManager,
    private val meshConnectionManager: MeshConnectionManager
) : ViewModel() {

    private val meshStatus = MutableLiveData<MeshStatus>()
    private val connectionMessage = MutableLiveData<ConnectionMessageListener.MessageType>()
    private val errorMessage = MutableLiveData<ErrorType>()
    private val currentSubnet = bluetoothMeshManager.currentSubnet!!

    fun connectToSubnet(){
        Timber.d("connectToSubnet")
        meshConnectionManager.apply {
            addMeshConnectionListener(connectionStatusListener)
            addMeshMessageListener(connectionMessageListener)
            connect(currentSubnet)
        }
    }

    fun disconnectFromSubnet(){
        Timber.d("disconnectFromSubnet")
        meshConnectionManager.apply {
            removeMeshConnectionListener(connectionStatusListener)
            removeMeshMessageListener(connectionMessageListener)
            disconnect()
        }
    }

    fun changeMeshStatus(meshStatus: MeshStatus){
        Timber.d("changeMeshStatus: $meshStatus")
        when (meshStatus) {
            MeshStatus.MESH_CONNECTING -> {
                    meshConnectionManager.disconnect()
            }
            MeshStatus.MESH_CONNECTED -> {
                    meshConnectionManager.disconnect()
            }
            MeshStatus.MESH_DISCONNECTED -> {
                meshConnectionManager.connect(currentSubnet)
            }
        }
    }

    fun getNodeListSize(): Int{
        return currentSubnet.nodes.size
    }

    fun getGroupListSize(): Int{
        return currentSubnet.groups.size
    }

    fun getMeshStatus(): LiveData<MeshStatus> {
        return meshStatus
    }

    fun getConnectionMessage(): LiveData<ConnectionMessageListener.MessageType> {
        return connectionMessage
    }

    fun getErrorMessage(): LiveData<ErrorType> {
        return errorMessage
    }

    private val connectionStatusListener = object : ConnectionStatusListener {
        override fun connecting() {
            Timber.d("connecting")
            meshStatus.value = MeshStatus.MESH_CONNECTING
        }

        override fun connected() {
            Timber.d("connected")
            meshStatus.value = MeshStatus.MESH_CONNECTED

        }

        override fun disconnected() {
            Timber.d("disconnected")
            meshStatus.value = MeshStatus.MESH_DISCONNECTED
        }
    }
    private val connectionMessageListener = object : ConnectionMessageListener {
        override fun connectionMessage(messageType: ConnectionMessageListener.MessageType) {
            Timber.d("connectionMessage: $messageType")
            connectionMessage.value = messageType
        }

        override fun connectionErrorMessage(error: ErrorType) {
            Timber.e("connectionErrorMessage: $error")
            errorMessage.value = error
        }
    }
}