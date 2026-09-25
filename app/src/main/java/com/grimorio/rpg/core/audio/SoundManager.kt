package com.grimorio.rpg.core.audio

/**
 * Interface para reprodução de efeitos sonoros de baixa latência da mesa de RPG.
 */
interface SoundManager {
    fun playRollSound()
    fun playCriticalHitSound()
    fun playCriticalFailSound()
    fun playClickSound()
    fun release()
}

class NoOpSoundManager : SoundManager {
    override fun playRollSound() = Unit
    override fun playCriticalHitSound() = Unit
    override fun playCriticalFailSound() = Unit
    override fun playClickSound() = Unit
    override fun release() = Unit
}
