package com.grimorio.rpg.presentation.party

import com.google.common.truth.Truth.assertThat
import com.grimorio.rpg.domain.model.DiceType
import com.grimorio.rpg.domain.model.DieValue
import com.grimorio.rpg.domain.model.P2PConnectionState
import com.grimorio.rpg.domain.model.P2PEndpoint
import com.grimorio.rpg.domain.model.P2PMessage
import com.grimorio.rpg.domain.model.P2PMessageType
import com.grimorio.rpg.domain.model.P2PRole
import com.grimorio.rpg.domain.model.RollResult
import com.grimorio.rpg.domain.repository.P2PPartyRepository
import com.grimorio.rpg.presentation.dice.FakeHapticManager
import com.grimorio.rpg.presentation.dice.FakeSoundManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class FakePartyRepository : P2PPartyRepository {
    val _connectionState = MutableStateFlow(P2PConnectionState.DISCONNECTED)
    override val connectionState: Flow<P2PConnectionState> = _connectionState.asStateFlow()

    val _connectedEndpoints = MutableStateFlow<List<P2PEndpoint>>(emptyList())
    override val connectedEndpoints: Flow<List<P2PEndpoint>> = _connectedEndpoints.asStateFlow()

    val _discoveredEndpoints = MutableStateFlow<List<P2PEndpoint>>(emptyList())
    override val discoveredEndpoints: Flow<List<P2PEndpoint>> = _discoveredEndpoints.asStateFlow()

    val _incomingMessages = MutableSharedFlow<P2PMessage>(extraBufferCapacity = 64)
    override val incomingMessages: Flow<P2PMessage> = _incomingMessages.asSharedFlow()

    val _currentRole = MutableStateFlow(P2PRole.DISCONNECTED)
    override val currentRole: Flow<P2PRole> = _currentRole.asStateFlow()

    var hostedSessionName: String? = null
    var isDiscovering = false
    var connectedEndpoint: P2PEndpoint? = null
    var connectedClientName: String? = null
    var isDisconnected = false
    val broadcastedRolls = mutableListOf<RollResult>()
    val broadcastedChats = mutableListOf<Pair<String, String>>()

    override suspend fun startHosting(tableSessionName: String) {
        hostedSessionName = tableSessionName
        _currentRole.value = P2PRole.HOST
        _connectionState.value = P2PConnectionState.ADVERTISING
    }

    override suspend fun startDiscovering() {
        isDiscovering = true
        _currentRole.value = P2PRole.CLIENT
        _connectionState.value = P2PConnectionState.DISCOVERING
    }

    override suspend fun connectToTable(endpoint: P2PEndpoint, clientName: String) {
        connectedEndpoint = endpoint
        connectedClientName = clientName
        _connectionState.value = P2PConnectionState.CONNECTING
    }

    override suspend fun disconnect() {
        isDisconnected = true
        _currentRole.value = P2PRole.DISCONNECTED
        _connectionState.value = P2PConnectionState.DISCONNECTED
    }

    override suspend fun broadcastDiceRoll(playerName: String, rollResult: RollResult, macroName: String?) {
        broadcastedRolls.add(rollResult)
    }

    override suspend fun broadcastChatMessage(senderName: String, text: String) {
        broadcastedChats.add(senderName to text)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PartyViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakePartyRepository
    private lateinit var fakeHaptic: FakeHapticManager
    private lateinit var fakeSound: FakeSoundManager
    private lateinit var viewModel: PartyViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakePartyRepository()
        fakeHaptic = FakeHapticManager()
        fakeSound = FakeSoundManager()
        viewModel = PartyViewModel(fakeRepository, fakeHaptic, fakeSound)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isDisconnected() {
        val state = viewModel.uiState.value
        assertThat(state.connectionState).isEqualTo(P2PConnectionState.DISCONNECTED)
        assertThat(state.role).isEqualTo(P2PRole.DISCONNECTED)
        assertThat(state.connectedPeers).isEmpty()
        assertThat(state.tableFeed).isEmpty()
    }

    @Test
    fun startHosting_callsRepositoryAndClosesDialog() = runTest {
        viewModel.openHostDialog()
        assertThat(viewModel.uiState.value.isHostDialogOpen).isTrue()

        viewModel.startHosting("Covil do Lich")
        advanceUntilIdle()

        assertThat(fakeRepository.hostedSessionName).isEqualTo("Covil do Lich")
        assertThat(viewModel.uiState.value.tableSessionName).isEqualTo("Covil do Lich")
        assertThat(viewModel.uiState.value.isHostDialogOpen).isFalse()
        assertThat(fakeSound.critHitCount).isEqualTo(1)
        assertThat(fakeHaptic.critHitCount).isEqualTo(1)
    }

    @Test
    fun startDiscovering_callsRepository() = runTest {
        viewModel.startDiscovering()
        advanceUntilIdle()

        assertThat(fakeRepository.isDiscovering).isTrue()
        assertThat(fakeSound.clickCount).isEqualTo(1)
        assertThat(fakeHaptic.clickCount).isEqualTo(1)
    }

    @Test
    fun joinTable_callsRepositoryConnect() = runTest {
        val endpoint = P2PEndpoint("table-1", "Mesa de D&D 5e")
        viewModel.joinTable(endpoint)
        advanceUntilIdle()

        assertThat(fakeRepository.connectedEndpoint).isEqualTo(endpoint)
        assertThat(fakeSound.clickCount).isEqualTo(1)
    }

    @Test
    fun sendChatMessage_ignoresEmptyMessage() = runTest {
        viewModel.sendChatMessage("   ")
        advanceUntilIdle()

        assertThat(fakeRepository.broadcastedChats).isEmpty()
    }

    @Test
    fun sendChatMessage_broadcastsValidText() = runTest {
        viewModel.sendChatMessage("Ataquem pelos flancos!")
        advanceUntilIdle()

        assertThat(fakeRepository.broadcastedChats).hasSize(1)
        assertThat(fakeRepository.broadcastedChats.first().second).isEqualTo("Ataquem pelos flancos!")
    }

    @Test
    fun incomingDiceRoll_withCriticalHit_playsCriticalSensoryFeedback() = runTest {
        advanceUntilIdle()

        val roll = RollResult(
            formula = "1d20 + 3",
            individualDice = listOf(DieValue(DiceType.D20, 20)),
            modifier = 3,
            total = 23,
            isCriticalHit = true,
            isCriticalFail = false
        )
        val msg = P2PMessage(
            type = P2PMessageType.DICE_ROLL,
            senderName = "Mago",
            rollResult = roll
        )

        fakeRepository._incomingMessages.tryEmit(msg)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.tableFeed).contains(msg)
        assertThat(fakeSound.critHitCount).isEqualTo(1)
        assertThat(fakeHaptic.critHitCount).isEqualTo(1)
    }

    @Test
    fun incomingChatMessage_addsToFeedAndClicks() = runTest {
        advanceUntilIdle()

        val msg = P2PMessage(
            type = P2PMessageType.CHAT_MESSAGE,
            senderName = "Ladino",
            text = "Encontrei uma armadilha!"
        )

        fakeRepository._incomingMessages.tryEmit(msg)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.tableFeed).contains(msg)
        assertThat(fakeSound.clickCount).isEqualTo(1)
        assertThat(fakeHaptic.clickCount).isEqualTo(1)
    }

    @Test
    fun disconnect_callsRepository() = runTest {
        viewModel.disconnect()
        advanceUntilIdle()

        assertThat(fakeRepository.isDisconnected).isTrue()
    }

    @Test
    fun hostDialog_toggle() {
        viewModel.openHostDialog()
        assertThat(viewModel.uiState.value.isHostDialogOpen).isTrue()

        viewModel.dismissHostDialog()
        assertThat(viewModel.uiState.value.isHostDialogOpen).isFalse()
    }
}
