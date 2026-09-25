package com.grimorio.rpg.data.nearby

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.grimorio.rpg.domain.model.P2PConnectionState
import com.grimorio.rpg.domain.model.P2PEndpoint
import com.grimorio.rpg.domain.model.P2PRole
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Cliente nativo encapsulando a API Google Nearby Connections para P2P local (Bluetooth / Wi-Fi Direct).
 */
class NearbyConnectionsClient(
    context: Context,
    private val serviceId: String = "com.grimorio.rpg.table",
    private val strategy: Strategy = Strategy.P2P_STAR
) : NearbyClient {
    private val connectionsClient: ConnectionsClient = Nearby.getConnectionsClient(context)

    private val _connectionState = MutableStateFlow(P2PConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<P2PConnectionState> = _connectionState.asStateFlow()

    private val _currentRole = MutableStateFlow(P2PRole.DISCONNECTED)
    override val currentRole: StateFlow<P2PRole> = _currentRole.asStateFlow()

    private val _connectedEndpoints = MutableStateFlow<List<P2PEndpoint>>(emptyList())
    override val connectedEndpoints: StateFlow<List<P2PEndpoint>> = _connectedEndpoints.asStateFlow()

    private val _discoveredEndpoints = MutableStateFlow<List<P2PEndpoint>>(emptyList())
    override val discoveredEndpoints: StateFlow<List<P2PEndpoint>> = _discoveredEndpoints.asStateFlow()

    private val _incomingPayloads = MutableSharedFlow<ByteArray>(extraBufferCapacity = 64)
    override val incomingPayloads: SharedFlow<ByteArray> = _incomingPayloads.asSharedFlow()

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                payload.asBytes()?.let { bytes ->
                    _incomingPayloads.tryEmit(bytes)
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) = Unit
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            // Em mesas locais amigáveis, aceita a conexão automaticamente
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
            if (resolution.status.statusCode == ConnectionsStatusCodes.STATUS_OK) {
                _connectedEndpoints.update { current ->
                    val existing = current.filterNot { it.id == endpointId }
                    existing + P2PEndpoint(id = endpointId, name = endpointId, isConnected = true)
                }
                _connectionState.value = P2PConnectionState.CONNECTED
            }
        }

        override fun onDisconnected(endpointId: String) {
            _connectedEndpoints.update { current ->
                current.filterNot { it.id == endpointId }
            }
            if (_connectedEndpoints.value.isEmpty() && _currentRole.value == P2PRole.CLIENT) {
                _connectionState.value = P2PConnectionState.DISCONNECTED
            }
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            _discoveredEndpoints.update { current ->
                val filtered = current.filterNot { it.id == endpointId }
                filtered + P2PEndpoint(id = endpointId, name = info.endpointName, isConnected = false)
            }
        }

        override fun onEndpointLost(endpointId: String) {
            _discoveredEndpoints.update { current ->
                current.filterNot { it.id == endpointId }
            }
        }
    }

    override fun startAdvertising(hostName: String) {
        stopAll()
        _currentRole.value = P2PRole.HOST
        _connectionState.value = P2PConnectionState.ADVERTISING

        val options = AdvertisingOptions.Builder().setStrategy(strategy).build()
        connectionsClient.startAdvertising(
            hostName,
            serviceId,
            connectionLifecycleCallback,
            options
        ).addOnFailureListener {
            _connectionState.value = P2PConnectionState.DISCONNECTED
            _currentRole.value = P2PRole.DISCONNECTED
        }
    }

    override fun startDiscovery() {
        stopAll()
        _currentRole.value = P2PRole.CLIENT
        _connectionState.value = P2PConnectionState.DISCOVERING
        _discoveredEndpoints.value = emptyList()

        val options = DiscoveryOptions.Builder().setStrategy(strategy).build()
        connectionsClient.startDiscovery(
            serviceId,
            endpointDiscoveryCallback,
            options
        ).addOnFailureListener {
            _connectionState.value = P2PConnectionState.DISCONNECTED
            _currentRole.value = P2PRole.DISCONNECTED
        }
    }

    override fun connectToEndpoint(endpointId: String, clientName: String) {
        connectionsClient.requestConnection(
            clientName,
            endpointId,
            connectionLifecycleCallback
        ).addOnFailureListener {
            // Falha ao conectar
        }
    }

    override fun sendPayloadToAll(bytes: ByteArray) {
        val endpointIds = _connectedEndpoints.value.map { it.id }
        if (endpointIds.isNotEmpty()) {
            connectionsClient.sendPayload(endpointIds, Payload.fromBytes(bytes))
        }
    }

    override fun stopAll() {
        connectionsClient.stopAdvertising()
        connectionsClient.stopDiscovery()
        connectionsClient.stopAllEndpoints()
        _connectedEndpoints.value = emptyList()
        _discoveredEndpoints.value = emptyList()
        _connectionState.value = P2PConnectionState.DISCONNECTED
        _currentRole.value = P2PRole.DISCONNECTED
    }
}
