package com.grimorio.rpg.presentation

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grimorio.rpg.core.audio.SoundPoolManager
import com.grimorio.rpg.core.designsystem.theme.AppTheme
import com.grimorio.rpg.core.designsystem.theme.GrimorioTheme
import com.grimorio.rpg.core.haptic.AndroidHapticManager
import com.grimorio.rpg.data.local.database.AppDatabase
import com.grimorio.rpg.data.nearby.NearbyConnectionsClient
import com.grimorio.rpg.data.repository.CharacterRepositoryImpl
import com.grimorio.rpg.data.repository.CombatRepositoryImpl
import com.grimorio.rpg.data.repository.DiceRepositoryImpl
import com.grimorio.rpg.data.repository.MacroRepositoryImpl
import com.grimorio.rpg.data.repository.P2PPartyRepositoryImpl
import com.grimorio.rpg.domain.model.SharePayloadType
import com.grimorio.rpg.presentation.combat.CombatScreen
import com.grimorio.rpg.presentation.combat.CombatViewModel
import com.grimorio.rpg.presentation.components.ThemeSelectorDialog
import com.grimorio.rpg.presentation.dice.DiceScreen
import com.grimorio.rpg.presentation.dice.DiceViewModel
import com.grimorio.rpg.presentation.party.PartyScreen
import com.grimorio.rpg.presentation.party.PartyViewModel
import com.grimorio.rpg.presentation.resources.ResourcesScreen
import com.grimorio.rpg.presentation.resources.ResourcesViewModel
import com.grimorio.rpg.presentation.share.components.ScanQrDialog
import kotlinx.coroutines.launch

enum class MainTab(val title: String, val icon: ImageVector) {
    DICE("Dados", Icons.Default.Casino),
    COMBAT("Combate", Icons.Default.SportsKabaddi),
    RESOURCES("Recursos", Icons.Default.Favorite),
    PARTY("Mesa P2P", Icons.Default.Groups)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val diceRepository = DiceRepositoryImpl(database.rollHistoryDao())
        val macroRepository = MacroRepositoryImpl(database.macroDao())
        val characterRepository = CharacterRepositoryImpl(database.characterDao())
        val combatRepository = CombatRepositoryImpl(database.combatDao())

        val nearbyClient = NearbyConnectionsClient(applicationContext)
        val partyRepository = P2PPartyRepositoryImpl(nearbyClient)

        val hapticManager = AndroidHapticManager(this)
        val soundManager = SoundPoolManager(this)

        val diceFactory = DiceViewModel.provideFactory(
            repository = diceRepository,
            macroRepository = macroRepository,
            partyRepository = partyRepository,
            hapticManager = hapticManager,
            soundManager = soundManager
        )

        val combatFactory = CombatViewModel.provideFactory(
            repository = combatRepository,
            hapticManager = hapticManager,
            soundManager = soundManager
        )

        val resourcesFactory = ResourcesViewModel.provideFactory(
            repository = characterRepository,
            hapticManager = hapticManager,
            soundManager = soundManager
        )

        val partyFactory = PartyViewModel.provideFactory(
            repository = partyRepository,
            hapticManager = hapticManager,
            soundManager = soundManager
        )

        setContent {
            var currentTheme by remember { mutableStateOf(AppTheme.MEDIEVAL) }
            var isThemeDialogOpen by remember { mutableStateOf(false) }

            GrimorioTheme(appTheme = currentTheme) {
                val diceViewModel: DiceViewModel = viewModel(factory = diceFactory)
                val combatViewModel: CombatViewModel = viewModel(factory = combatFactory)
                val resourcesViewModel: ResourcesViewModel = viewModel(factory = resourcesFactory)
                val partyViewModel: PartyViewModel = viewModel(factory = partyFactory)

                var currentTab by remember { mutableStateOf(MainTab.DICE) }
                var isScanQrDialogOpen by remember { mutableStateOf(false) }

                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    floatingActionButton = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            SmallFloatingActionButton(
                                onClick = { isThemeDialogOpen = true },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                shape = CircleShape
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "Trocar Tema Visual"
                                )
                            }

                            FloatingActionButton(
                                onClick = { isScanQrDialogOpen = true },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                shape = CircleShape
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "Escanear QR Code Offline"
                                )
                            }
                        }
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            MainTab.entries.forEach { tab ->
                                val selected = currentTab == tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentTab = tab },
                                    icon = { Icon(tab.icon, contentDescription = tab.title) },
                                    label = { Text(tab.title) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = innerPadding.calculateBottomPadding())
                    ) {
                        when (currentTab) {
                            MainTab.DICE -> DiceScreen(viewModel = diceViewModel)
                            MainTab.COMBAT -> CombatScreen(viewModel = combatViewModel)
                            MainTab.RESOURCES -> ResourcesScreen(viewModel = resourcesViewModel)
                            MainTab.PARTY -> PartyScreen(viewModel = partyViewModel)
                        }
                    }
                }

                if (isThemeDialogOpen) {
                    ThemeSelectorDialog(
                        currentTheme = currentTheme,
                        onThemeSelected = { newTheme ->
                            currentTheme = newTheme
                            soundManager.playClickSound()
                            hapticManager.vibrateClick()
                        },
                        onDismiss = { isThemeDialogOpen = false }
                    )
                }

                if (isScanQrDialogOpen) {
                    ScanQrDialog(
                        onDismiss = { isScanQrDialogOpen = false },
                        onPayloadImported = { payload ->
                            lifecycleScope.launch {
                                when (payload.type) {
                                    SharePayloadType.CHARACTER -> {
                                        payload.character?.let {
                                            characterRepository.saveCharacter(it)
                                        }
                                    }
                                    SharePayloadType.COMBAT_ENCOUNTER -> {
                                        payload.combatants?.let { list ->
                                            combatRepository.clearCombatants()
                                            for (c in list) {
                                                combatRepository.addCombatant(c)
                                            }
                                        }
                                    }
                                    SharePayloadType.MACRO -> {
                                        payload.macro?.let {
                                            macroRepository.saveMacro(it)
                                        }
                                    }
                                }
                                soundManager.playCriticalHitSound()
                                hapticManager.vibrateCriticalHit()
                                Toast.makeText(
                                    applicationContext,
                                    "Importado com sucesso: ${payload.title}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                }
            }
        }
    }
}
