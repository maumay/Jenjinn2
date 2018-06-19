/**
 *
 */
package jenjinn.engine.boardstate.terminationstate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import jenjinn.engine.boardstate.BoardState;
import jenjinn.engine.boardstate.calculators.TerminationState;
import jenjinn.engine.enums.GameTermination;
import xawd.jflow.iterators.Flow;

/**
 * @author ThomasB
 */
class TerminationStateTest
{
	@ParameterizedTest
	@MethodSource
	void test(final BoardState state, final Boolean hasLegalMoves, final GameTermination expectedTerminationState)
	{
		assertEquals(expectedTerminationState, TerminationState.of(state, hasLegalMoves));
	}

	static Flow<Arguments> test()
	{
		throw new RuntimeException();
	}
}