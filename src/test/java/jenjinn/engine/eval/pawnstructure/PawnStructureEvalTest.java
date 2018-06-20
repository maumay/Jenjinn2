/**
 *
 */
package jenjinn.engine.eval.pawnstructure;

import static jenjinn.engine.eval.PawnStructureEvaluator.CHAIN_BONUS;
import static jenjinn.engine.eval.PawnStructureEvaluator.DOUBLED_PENALTY;
import static jenjinn.engine.eval.PawnStructureEvaluator.ISOLATED_PENALTY;
import static jenjinn.engine.eval.PawnStructureEvaluator.PASSED_BONUS;
import static jenjinn.engine.eval.PawnStructureEvaluator.SEMIOPEN_FILE_BONUS;
import static jenjinn.engine.eval.PawnStructureEvaluator.evaluateDoubledPawns;
import static jenjinn.engine.eval.PawnStructureEvaluator.evaluateIsolatedPawns;
import static jenjinn.engine.eval.PawnStructureEvaluator.evaluatePassedPawns;
import static jenjinn.engine.eval.PawnStructureEvaluator.evaluatePawnChains;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import xawd.jflow.iterators.misc.IntPair;

/**
 * @author ThomasB
 */
class PawnStructureEvalTest
{
	@ParameterizedTest
	@MethodSource
	void test(Long whitePawnLocs, Long blackPawnLocs, ExpectedValues expectedValues)
	{
		final int doubledDifference = expectedValues.getDoubledPawnCountDifference();
		assertEquals(-doubledDifference*DOUBLED_PENALTY, evaluateDoubledPawns(whitePawnLocs, blackPawnLocs));

		final int passedDifference = expectedValues.getPassedPawnCountDifference();
		assertEquals(passedDifference*PASSED_BONUS, evaluatePassedPawns(whitePawnLocs, blackPawnLocs));

		final int chainLinkDifference = expectedValues.getChainLinkCountDifference();
		assertEquals(chainLinkDifference*CHAIN_BONUS, evaluatePawnChains(whitePawnLocs, blackPawnLocs));

		final IntPair isolatedDifferences = expectedValues.getIsolatedPawnCountDifferences();
		final int expectedEval = -isolatedDifferences.getFirst() * ISOLATED_PENALTY
				- isolatedDifferences.getSecond() * (ISOLATED_PENALTY + SEMIOPEN_FILE_BONUS);
		assertEquals(expectedEval, evaluateIsolatedPawns(whitePawnLocs, blackPawnLocs));


	}

}
