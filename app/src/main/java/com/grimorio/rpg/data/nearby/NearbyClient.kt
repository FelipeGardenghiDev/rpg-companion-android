package com.grimorio.rpg.data.nearby

import com.grimorio.rpg.domain.model.P2PConnectionState
import com.grimorio.rpg.domain.model.P2PEndpoint
import com.grimorio.rpg.domain.model.P2PRole
import kotlinx.coroutines.flow.Flow

/**
 * Interface abstrata para o cliente de conexões de proximidade P2P.
 * Permite desacoplar a camada de repositório da API concreta do Google Nearby Connections
 * e viabiliza testes unitários rápidos e determinísticos com Fakes.
 */
interface NearbyClient {
    val connectionState: Flow<P2PConnectionState>
    val currentRole: Flow<P2PRole>
    val connectedEndpoints: Flow<List<P2PEndpoint>>
    val discoveredEndpoints: Flow<List<P2PEndpoint>>
    val incomingPayloads: Flow<ByteArray>

    fun startAdvertising(hostName: String)
    fun startDiscovery()
    fun connectToEndpoint(endpointId: String, clientName: String)
    fun sendPayloadToAll(bytes: ByteArray)
    fun stopAll()
}
