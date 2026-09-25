package com.grimorio.rpg.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.grimorio.rpg.R

/**
 * Implementação de SoundManager baseada em SoundPool para latência próxima de zero.
 */
class SoundPoolManager(context: Context) : SoundManager {

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(audioAttributes)
        .build()

    private var rollSoundId: Int = 0
    private var criticalHitSoundId: Int = 0
    private var criticalFailSoundId: Int = 0
    private var clickSoundId: Int = 0

    private val loadedSoundIds = mutableSetOf<Int>()

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedSoundIds.add(sampleId)
            }
        }

        try {
            rollSoundId = soundPool.load(context, R.raw.dice_roll, 1)
            criticalHitSoundId = soundPool.load(context, R.raw.critical_hit, 1)
            criticalFailSoundId = soundPool.load(context, R.raw.critical_fail, 1)
            clickSoundId = soundPool.load(context, R.raw.dice_click, 1)
        } catch (_: Exception) {
            // Ignora se recursos não puderem ser resolvidos em ambientes de preview ou teste
        }
    }

    override fun playRollSound() {
        playSound(rollSoundId)
    }

    override fun playCriticalHitSound() {
        playSound(criticalHitSoundId)
    }

    override fun playCriticalFailSound() {
        playSound(criticalFailSoundId)
    }

    override fun playClickSound() {
        playSound(clickSoundId, volume = 0.5f)
    }

    private fun playSound(soundId: Int, volume: Float = 1.0f) {
        if (soundId != 0 && loadedSoundIds.contains(soundId)) {
            soundPool.play(soundId, volume, volume, 1, 0, 1.0f)
        }
    }

    override fun release() {
        soundPool.release()
    }
}
