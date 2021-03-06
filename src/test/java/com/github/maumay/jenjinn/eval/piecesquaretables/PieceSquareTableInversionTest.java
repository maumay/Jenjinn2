/**
 *
 */
package com.github.maumay.jenjinn.eval.piecesquaretables;

import static com.github.maumay.jenjinn.pieces.Piece.BLACK_KNIGHT;
import static com.github.maumay.jenjinn.pieces.Piece.WHITE_KNIGHT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.maumay.jflow.iterators.Iter;

/**
 * @author t
 *
 */
class PieceSquareTableInversionTest
{
	@Test
	void test()
	{
		final PieceSquareTable startTable = new PieceSquareTable(WHITE_KNIGHT, 500,
				Iter.between(0, -64, -1).toArray());

		final int[] expectedInvertedLocs = Iter.between(7, -1, -1).map(i -> 8 * i)
				.mapToObject(i -> Iter.between(i, i + 8).toArray())
				.flatMap(array -> Iter.ints(array).boxed()).mapToInt(x -> x).toArray();

		final PieceSquareTable expectedInversion = new PieceSquareTable(BLACK_KNIGHT,
				-500, expectedInvertedLocs);

		assertEquals(expectedInversion, startTable.invertValues());
		assertEquals(startTable, startTable.invertValues().invertValues());
	}
}
