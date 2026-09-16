package com.grimorio.rpg.domain.usecase

import com.grimorio.rpg.domain.model.RollResult
import com.grimorio.rpg.domain.parser.DiceExpressionParser

/**
 * Caso de uso para processar rolagens de dados a partir de fórmulas ou seleções da UI.
 */
class RollDiceUseCase(
    private val parser: DiceExpressionParser = DiceExpressionParser()
) {
    operator fun invoke(formula: String): Result<RollResult> {
        return runCatching {
            parser.parseAndRoll(formula)
        }
    }
}
