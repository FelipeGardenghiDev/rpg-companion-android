package com.grimorio.rpg.data.repository

import com.google.common.truth.Truth.assertThat
import com.grimorio.rpg.data.nearby.NearbyClient
import com.grimorio.rpg.domain.model.DiceType
import com.grimorio.rpg.domain.model.DieValue
import com.grimorio.rpg.domain.model.P2PConnectionState
import com.grimorio.rpg.domain.model.P2PEndpoint
import com.grimorio.rpg.domain.model.P2PMessage
import com.grimorio.rpg.domain.model.P2PMessageType
import com.grimorio.rpg.domain.model.P2PRole
import com.grimorio.rpg.domain.model.RollResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test

class FakeNearbyClient : NearbyClient {
    val _connectionState = MutableStateFlow(P2PConnectionState.DISCONNECTED)
    override val connectionState = _connectionState.asStateFlow()

    val _currentRole = MutableStateFlow(P2PRole.DISCONNECTED)
    override val currentRole = _currentRole.asStateFlow()

    val _connectedEndpoints = MutableStateFlow<List<P2PEndpoint>>(emptyList())
    override val connectedEndpoints = _connectedEndpoints.asStateFlow()

    val _discoveredEndpoints = MutableStateFlow<List<P2PEndpoint>>(emptyList())
    override val discoveredEndpoints = _discoveredEndpoints.asStateFlow()

    val _incomingPayloads = MutableSharedFlow<ByteArray>(extraBufferCapacity = 64)
    override val incomingPayloads = _incomingPayloads.asSharedFlow()

    val sentPayloads = mutableListOf<ByteArray>()
    var lastAdvertisedName: String? = null
    var discoveryStarted = false
    var lastConnectedEndpointId: String? = null
    var lastConnectedClientName: String? = null
    var stopped = false

    override fun startAdvertising(hostName: String) {
        lastAdvertisedName = hostName
        _currentRole.value = P2PRole.HOST
        _connectionState.value = P2PConnectionState.ADVERTISING
    }

    override fun startDiscovery() {
        discoveryStarted = true
        _currentRole.value = P2PRole.CLIENT
        _connectionState.value = P2PConnectionState.DISCOVERING
    }

    override fun connectToEndpoint(endpointId: String, clientName: String) {
        lastConnectedEndpointId = endpointId
        lastConnectedClientName = clientName
    }

    override fun sendPayloadToAll(bytes: ByteArray) {
        sentPayloads.add(bytes)
    }

    override fun stopAll() {
        stopped = true
        _connectionState.value = P2PConnectionState.DISCONNECTED
        _currentRole.value = P2PRole.DISCONNECTED
        _connectedEndpoints.value = emptyList()
        _discoveredEndpoints.value = emptyList()
    }
}

class P2PPartyRepositoryImplTest {

    private lateinit var fakeClient: FakeNearbyClient
    private lateinit var repository: P2PPartyRepositoryImpl
    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setUp() {
        fakeClient = FakeNearbyClient()
        repository = P2PPartyRepositoryImpl(fakeClient, json)
    }

    @Test
    fun startHosting_updatesRoleAndState() = runTest {
        repository.startHosting("Taverna do Dragão")

        assertThat(fakeClient.lastAdvertisedName).isEqualTo("Taverna do Dragão")
        assertThat(repository.currentRole.first()).isEqualTo(P2PRole.HOST)
        assertThat(repository.connectionState.first()).isEqualTo(P2PConnectionState.ADVERTISING)
    }

    @Test
    fun startDiscovering_updatesRoleAndState() = runTest {
        repository.startDiscovering()

        assertThat(fakeClient.discoveryStarted).isTrue()
        assertThat(repository.currentRole.first()).isEqualTo(P2PRole.CLIENT)
        assertThat(repository.connectionState.first()).isEqualTo(P2PConnectionState.DISCOVERING)
    }

    @Test
    fun connectToTable_callsClientConnect() = runTest {
        val endpoint = P2PEndpoint(id = "endp-123", name = "Mesa do Mestre")
        repository.connectToTable(endpoint, "Aragorn")

        assertThat(fakeClient.lastConnectedEndpointId).isEqualTo("endp-123")
        assertThat(fakeClient.lastConnectedClientName).isEqualTo("Aragorn")
    }

    @Test
    fun broadcastDiceRoll_encodesAndSendsP2PMessage() = runTest {
        val rollResult = RollResult(
            formula = "1d20 + 5",
            individualDice = listOf(DieValue(DiceType.D20, 20)),
            modifier = 5,
            total = 25,
            isCriticalHit = true,
            isCriticalFail = false
        )

        repository.broadcastDiceRoll("Gandalf", rollResult, "Bola de Fogo")

        assertThat(fakeClient.sentPayloads).hasSize(1)
        val sentJson = String(fakeClient.sentPayloads.first(), Charsets.UTF_8)
        val decoded = json.decodeFromString<P2PMessage>(sentJson)

        assertThat(decoded.type).isEqualTo(P2PMessageType.DICE_ROLL)
        assertThat(decoded.senderName).isEqualTo("Gandalf")
        assertThat(decoded.rollResult?.total).isEqualTo(25)
        assertThat(decoded.rollResult?.isCriticalHit).isTrue()
        assertThat(decoded.macroName).isEqualTo("Bola de Fogo")
    }

    @Test
    fun broadcastChatMessage_encodesAndSendsP2PMessage() = runTest {
        repository.broadcastChatMessage("Legolas", "Eles estão levando os hobbits para Isengard!")

        assertThat(fakeClient.sentPayloads).hasSize(1)
        val sentJson = String(fakeClient.sentPayloads.first(), Charsets.UTF_8)
        val decoded = json.decodeFromString<P2PMessage>(sentJson)

        assertThat(decoded.type).isEqualTo(P2PMessageType.CHAT_MESSAGE)
        assertThat(decoded.senderName).isEqualTo("Legolas")
        assertThat(decoded.text).isEqualTo("Eles estão levando os hobbits para Isengard!")
    }

    @Test
    fun incomingMessages_decodesValidJsonPayloads() = runTest {
        val message = P2PMessage(
            type = P2PMessageType.CHAT_MESSAGE,
            senderName = "Gimli",
            text = "E o meu machado!"
        )
        val bytes = json.encodeToString(message).toByteArray(Charsets.UTF_8)

        fakeClient._incomingPayloads.tryEmit(bytes)

        val received = repository.incomingMessages.first()
        assertThat(received.senderName).isEqualTo("Gimli")
        assertThat(received.text).isEqualTo("E o meu machado!")
    }

    @Test
    fun disconnect_stopsAllAndClearsState() = runTest {
        repository.startHosting("Mesa Aberta")
        repository.disconnect()

        assertThat(fakeClient.stopped).isTrue()
        assertThat(repository.connectionState.first()).isEqualTo(P2PConnectionState.DISCONNECTED)
        assertThat(repository.currentRole.first()).isEqualTo(P2PRole.DISCONNECTED)
    }
}
