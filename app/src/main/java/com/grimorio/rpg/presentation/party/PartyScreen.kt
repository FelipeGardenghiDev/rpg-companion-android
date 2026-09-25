package com.grimorio.rpg.presentation.party

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grimorio.rpg.core.designsystem.theme.ArcanePurple
import com.grimorio.rpg.core.designsystem.theme.BorderRunic
import com.grimorio.rpg.core.designsystem.theme.CriticalFailure
import com.grimorio.rpg.core.designsystem.theme.CriticalSuccess
import com.grimorio.rpg.core.designsystem.theme.DarkBackground
import com.grimorio.rpg.core.designsystem.theme.DarkSurface
import com.grimorio.rpg.core.designsystem.theme.DarkSurfaceCard
import com.grimorio.rpg.core.designsystem.theme.DarkSurfaceVariant
import com.grimorio.rpg.core.designsystem.theme.GoldPrimary
import com.grimorio.rpg.domain.model.P2PConnectionState
import com.grimorio.rpg.domain.model.P2PMessage
import com.grimorio.rpg.domain.model.P2PMessageType
import com.grimorio.rpg.domain.model.P2PRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyScreen(
    modifier: Modifier = Modifier,
    viewModel: PartyViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "MESA LOCAL P2P",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = GoldPrimary
                            )
                            Text(
                                text = "Bluetooth & Wi-Fi Direct (Sem Internet)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    if (uiState.connectionState != P2PConnectionState.DISCONNECTED) {
                        IconButton(onClick = { viewModel.disconnect() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Desconectar",
                                tint = CriticalFailure
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(2.dp)) }

            // 1. CARD DE CONEXÃO & PAPEL (MESTRE / JOGADOR)
            item {
                when (uiState.connectionState) {
                    P2PConnectionState.DISCONNECTED -> {
                        DisconnectedSetupCard(
                            onHostClick = { viewModel.openHostDialog() },
                            onDiscoverClick = { viewModel.startDiscovering() }
                        )
                    }
                    P2PConnectionState.ADVERTISING -> {
                        HostActiveCard(
                            sessionName = uiState.tableSessionName,
                            connectedPeers = uiState.connectedPeers,
                            onCloseTable = { viewModel.disconnect() }
                        )
                    }
                    P2PConnectionState.DISCOVERING -> {
                        DiscoveringCard(
                            discoveredTables = uiState.discoveredTables,
                            onJoin = { viewModel.joinTable(it) },
                            onCancel = { viewModel.disconnect() }
                        )
                    }
                    P2PConnectionState.CONNECTED -> {
                        ConnectedClientCard(
                            onDisconnect = { viewModel.disconnect() }
                        )
                    }
                }
            }

            // 2. MURAL DA MESA AO VIVO (LIVE FEED)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WifiTethering,
                            contentDescription = null,
                            tint = if (uiState.connectionState == P2PConnectionState.CONNECTED || uiState.connectionState == P2PConnectionState.ADVERTISING) CriticalSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mural da Mesa em Tempo Real (${uiState.tableFeed.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (uiState.tableFeed.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceCard)
                            .border(1.dp, BorderRunic, RoundedCornerShape(12.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = null,
                                tint = BorderRunic,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Nenhuma atividade registrada na mesa ainda.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Conecte os aparelhos para transmitir rolagens instantaneamente!",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(uiState.tableFeed) { msg ->
                    FeedMessageCard(message = msg)
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    if (uiState.isHostDialogOpen) {
        HostTableDialog(
            defaultName = uiState.tableSessionName,
            onDismiss = { viewModel.dismissHostDialog() },
            onConfirm = { name -> viewModel.startHosting(name) }
        )
    }
}

@Composable
private fun DisconnectedSetupCard(
    onHostClick: () -> Unit,
    onDiscoverClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.5.dp, BorderRunic, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bluetooth,
                    contentDescription = null,
                    tint = ArcanePurple,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mesa Local sem Fio",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Conecte os dispositivos sentados à mesa usando rádio direto (Bluetooth & Wi-Fi Direct). Sem custos, sem servidores e 100% privado.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onHostClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = DarkBackground
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("👑 Abrir Mesa (Mestre)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onDiscoverClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("🔍 Buscar Mesas", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun HostActiveCard(
    sessionName: String,
    connectedPeers: List<com.grimorio.rpg.domain.model.P2PEndpoint>,
    onCloseTable: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, GoldPrimary, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "👑 MESTRE DA MESA",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = GoldPrimary
                    )
                }

                OutlinedButton(
                    onClick = onCloseTable,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Fechar Mesa", color = CriticalFailure, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = sessionName,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Sua mesa está transmitindo via Bluetooth. Peça aos jogadores para clicarem em 'Buscar Mesas'.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Jogadores Conectados (${connectedPeers.size}):",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = GoldPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (connectedPeers.isEmpty()) {
                Text(
                    text = "Aguardando entrada de jogadores...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    connectedPeers.forEach { peer ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(CriticalSuccess)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = peer.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoveringCard(
    discoveredTables: List<com.grimorio.rpg.domain.model.P2PEndpoint>,
    onJoin: (com.grimorio.rpg.domain.model.P2PEndpoint) -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, ArcanePurple, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = ArcanePurple
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Buscando Mesas Próximas...",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ArcanePurple
                    )
                }

                OutlinedButton(onClick = onCancel, shape = RoundedCornerShape(8.dp)) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (discoveredTables.isEmpty()) {
                Text(
                    text = "Nenhuma mesa encontrada ainda. Certifique-se de que o Mestre já abriu a sessão no aplicativo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    discoveredTables.forEach { table ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurfaceVariant)
                                .border(1.dp, BorderRunic, RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = table.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Mesa Local P2P",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldPrimary
                                )
                            }

                            Button(
                                onClick = { onJoin(table) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = DarkBackground
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Entrar")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectedClientCard(
    onDisconnect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, CriticalSuccess, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = CriticalSuccess,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Conectado na Mesa!",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = CriticalSuccess
                    )
                    Text(
                        text = "Suas rolagens de dados são sincronizadas ao vivo.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedButton(onClick = onDisconnect, shape = RoundedCornerShape(8.dp)) {
                Text("Sair", color = CriticalFailure)
            }
        }
    }
}

@Composable
private fun FeedMessageCard(message: P2PMessage) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = GoldPrimary
                    )

                    if (message.type == P2PMessageType.DICE_ROLL && message.rollResult != null) {
                        val roll = message.rollResult
                        Text(
                            text = "${message.macroName ?: "Rolagem"}: ${roll.formula}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else if (message.text != null) {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (message.type == P2PMessageType.DICE_ROLL && message.rollResult != null) {
                val roll = message.rollResult
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = roll.total.toString(),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = when {
                            roll.isCriticalHit -> CriticalSuccess
                            roll.isCriticalFail -> CriticalFailure
                            else -> GoldPrimary
                        }
                    )
                    if (roll.isCriticalHit) {
                        Text(
                            text = "CRÍTICO!",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = CriticalSuccess
                        )
                    } else if (roll.isCriticalFail) {
                        Text(
                            text = "FALHA!",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = CriticalFailure
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HostTableDialog(
    defaultName: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String) -> Unit
) {
    var tableName by remember { mutableStateOf(defaultName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "Abrir Sessão de Mesa P2P",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = GoldPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Defina o nome da mesa para que os jogadores na mesma sala encontrem seu dispositivo via Bluetooth.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = tableName,
                    onValueChange = { tableName = it },
                    label = { Text("Nome da Mesa") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = BorderRunic
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (tableName.isNotBlank()) onConfirm(tableName.trim())
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = DarkBackground
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Iniciar Sessão")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
