/**
 *
 */
package jenjinn.engine.bitboards;

import static java.util.Arrays.asList;
import static jenjinn.engine.bitboards.Bitboards.antiDiagonalBitboard;
import static jenjinn.engine.bitboards.Bitboards.diagonalBitboard;
import static jenjinn.engine.bitboards.Bitboards.fileBitboard;
import static jenjinn.engine.bitboards.Bitboards.rankBitboard;
import static jenjinn.engine.bitboards.Bitboards.singleOccupancyBitboard;
import static jenjinn.engine.enums.BoardSquare.A1;
import static jenjinn.engine.enums.BoardSquare.A2;
import static jenjinn.engine.enums.BoardSquare.A3;
import static jenjinn.engine.enums.BoardSquare.A4;
import static jenjinn.engine.enums.BoardSquare.A5;
import static jenjinn.engine.enums.BoardSquare.A6;
import static jenjinn.engine.enums.BoardSquare.A7;
import static jenjinn.engine.enums.BoardSquare.A8;
import static jenjinn.engine.enums.BoardSquare.B1;
import static jenjinn.engine.enums.BoardSquare.B3;
import static jenjinn.engine.enums.BoardSquare.B4;
import static jenjinn.engine.enums.BoardSquare.B5;
import static jenjinn.engine.enums.BoardSquare.B6;
import static jenjinn.engine.enums.BoardSquare.B7;
import static jenjinn.engine.enums.BoardSquare.C1;
import static jenjinn.engine.enums.BoardSquare.C4;
import static jenjinn.engine.enums.BoardSquare.C5;
import static jenjinn.engine.enums.BoardSquare.C6;
import static jenjinn.engine.enums.BoardSquare.D1;
import static jenjinn.engine.enums.BoardSquare.E1;
import static jenjinn.engine.enums.BoardSquare.E2;
import static jenjinn.engine.enums.BoardSquare.F1;
import static jenjinn.engine.enums.BoardSquare.F3;
import static jenjinn.engine.enums.BoardSquare.G1;
import static jenjinn.engine.enums.BoardSquare.G3;
import static jenjinn.engine.enums.BoardSquare.G6;
import static jenjinn.engine.enums.BoardSquare.H1;
import static jenjinn.engine.enums.BoardSquare.H2;
import static jenjinn.engine.enums.BoardSquare.H3;
import static jenjinn.engine.enums.BoardSquare.H4;
import static jenjinn.engine.enums.BoardSquare.H5;
import static jenjinn.engine.enums.BoardSquare.H6;
import static jenjinn.engine.enums.BoardSquare.H7;
import static jenjinn.engine.enums.BoardSquare.H8;
import static jenjinn.engine.enums.ChessPiece.BLACK_BISHOP;
import static jenjinn.engine.enums.ChessPiece.BLACK_KING;
import static jenjinn.engine.enums.ChessPiece.BLACK_KNIGHT;
import static jenjinn.engine.enums.ChessPiece.BLACK_PAWN;
import static jenjinn.engine.enums.ChessPiece.BLACK_QUEEN;
import static jenjinn.engine.enums.ChessPiece.BLACK_ROOK;
import static jenjinn.engine.enums.ChessPiece.WHITE_BISHOP;
import static jenjinn.engine.enums.ChessPiece.WHITE_KING;
import static jenjinn.engine.enums.ChessPiece.WHITE_KNIGHT;
import static jenjinn.engine.enums.ChessPiece.WHITE_PAWN;
import static jenjinn.engine.enums.ChessPiece.WHITE_QUEEN;
import static jenjinn.engine.enums.ChessPiece.WHITE_ROOK;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import jenjinn.engine.enums.BoardSquare;
import jenjinn.engine.enums.ChessPiece;
import jenjinn.engine.enums.Direction;
import jenjinn.engine.misc.PieceMovementDirections;
import xawd.jflow.iterators.construction.IterRange;
import xawd.jflow.iterators.construction.Iterate;

/**
 * @author ThomasB
 *
 */
class BitboardsInitialisationSection1Test
{
	@Test
	void testSingleOccupancyBitboard()
	{
		IterRange.to(64).forEach(i -> assertEquals(1L << i, singleOccupancyBitboard(i)));
	}

	@Test
	void testRankBitboard()
	{
		final long[] expectedRanks = Iterate.over(A1, A2, A3, A4, A5, A6, A7, A8)
				.map(square -> Iterate.over(square.getAllSquaresInDirections(Direction.E, 8)).insert(square))
				.mapToLong(BitboardUtils::bitwiseOr)
				.toArray();

		IterRange.to(8).forEach(i -> assertEquals(expectedRanks[i], rankBitboard(i)));
	}

	@Test
	void testFileBitboard()
	{
		final long[] expectedFiles = Iterate.over(H1, G1, F1, E1, D1, C1, B1, A1)
				.map(square -> Iterate.over(square.getAllSquaresInDirections(Direction.N, 8)).insert(square))
				.mapToLong(BitboardUtils::bitwiseOr)
				.toArray();

		IterRange.to(8).forEach(i -> assertEquals(expectedFiles[i], fileBitboard(i)));
	}

	@Test
	void testDiagonalBitboard()
	{
		final long[] expectedDiagonals = Iterate.over(asList(H1, G1, F1, E1, D1, C1, B1, A1, A2, A3, A4, A5, A6, A7, A8))
				.map(square -> Iterate.over(square.getAllSquaresInDirections(Direction.NE, 8)).insert(square))
				.mapToLong(BitboardUtils::bitwiseOr)
				.toArray();

		IterRange.to(15).forEach(i -> assertEquals(expectedDiagonals[i], diagonalBitboard(i)));
	}

	@Test
	void testAntiDiagonalBitboard()
	{
		final long[] expectedDiagonals = Iterate.over(asList(A1, B1, C1, D1, E1, F1, G1, H1, H2, H3, H4, H5, H6, H7, H8))
				.map(square -> Iterate.over(square.getAllSquaresInDirections(Direction.NW, 8)).insert(square))
				.mapToLong(BitboardUtils::bitwiseOr)
				.toArray();

		IterRange.to(15).forEach(i -> assertEquals(expectedDiagonals[i], antiDiagonalBitboard(i)));
	}

	@ParameterizedTest
	@MethodSource
	void testEmptyBoardMovesetBitboard(final ChessPiece piece, final BoardSquare location, final List<BoardSquare> expectedMoveLocations)
	{
		assertEquals(BitboardUtils.bitwiseOr(expectedMoveLocations), Bitboards.emptyBoardMoveset(piece, location));
	}

	static Stream<Arguments> testEmptyBoardMovesetBitboard()
	{
		return Stream.of(
				Arguments.of(WHITE_PAWN, A2, asList(A3, A4)),
				Arguments.of(WHITE_PAWN, B3, asList(B4)),
				Arguments.of(WHITE_KNIGHT, C5, C5.getAllSquaresInDirections(PieceMovementDirections.KNIGHT, 1)),
				Arguments.of(WHITE_BISHOP, F3, F3.getAllSquaresInDirections(PieceMovementDirections.BISHOP, 8)),
				Arguments.of(WHITE_ROOK, B3, B3.getAllSquaresInDirections(PieceMovementDirections.ROOK, 8)),
				Arguments.of(WHITE_QUEEN, H2, H2.getAllSquaresInDirections(PieceMovementDirections.QUEEN, 8)),
				Arguments.of(WHITE_KING, E2, E2.getAllSquaresInDirections(PieceMovementDirections.KING, 1)),

				Arguments.of(BLACK_PAWN, A2, asList(A1)),
				Arguments.of(BLACK_PAWN, B7, asList(B6, B5)),
				Arguments.of(BLACK_KNIGHT, C5, C5.getAllSquaresInDirections(PieceMovementDirections.KNIGHT, 1)),
				Arguments.of(BLACK_BISHOP, F3, F3.getAllSquaresInDirections(PieceMovementDirections.BISHOP, 8)),
				Arguments.of(BLACK_ROOK, B3, B3.getAllSquaresInDirections(PieceMovementDirections.ROOK, 8)),
				Arguments.of(BLACK_QUEEN, H2, H2.getAllSquaresInDirections(PieceMovementDirections.QUEEN, 8)),
				Arguments.of(BLACK_KING, E2, E2.getAllSquaresInDirections(PieceMovementDirections.KING, 1))
				);
	}

	@ParameterizedTest
	@MethodSource
	void testEmptyBoardAttacksetBitboard(final ChessPiece piece, final BoardSquare location, final List<BoardSquare> expectedMoveLocations)
	{
		assertEquals(BitboardUtils.bitwiseOr(expectedMoveLocations), Bitboards.emptyBoardAttackset(piece, location));
	}

	static Stream<Arguments> testEmptyBoardAttacksetBitboard()
	{
		return Stream.of(
				Arguments.of(WHITE_PAWN, A2, asList(B3)),
				Arguments.of(WHITE_PAWN, B3, asList(C4, A4)),
				Arguments.of(WHITE_PAWN, H5, asList(G6)),
				Arguments.of(WHITE_KNIGHT, C5, C5.getAllSquaresInDirections(PieceMovementDirections.KNIGHT, 1)),
				Arguments.of(WHITE_BISHOP, F3, F3.getAllSquaresInDirections(PieceMovementDirections.BISHOP, 8)),
				Arguments.of(WHITE_ROOK, B3, B3.getAllSquaresInDirections(PieceMovementDirections.ROOK, 8)),
				Arguments.of(WHITE_QUEEN, H2, H2.getAllSquaresInDirections(PieceMovementDirections.QUEEN, 8)),
				Arguments.of(WHITE_KING, E2, E2.getAllSquaresInDirections(PieceMovementDirections.KING, 1)),

				Arguments.of(BLACK_PAWN, A2, asList(B1)),
				Arguments.of(BLACK_PAWN, B7, asList(C6, A6)),
				Arguments.of(BLACK_PAWN, H4, asList(G3)),
				Arguments.of(BLACK_KNIGHT, C5, C5.getAllSquaresInDirections(PieceMovementDirections.KNIGHT, 1)),
				Arguments.of(BLACK_BISHOP, F3, F3.getAllSquaresInDirections(PieceMovementDirections.BISHOP, 8)),
				Arguments.of(BLACK_ROOK, B3, B3.getAllSquaresInDirections(PieceMovementDirections.ROOK, 8)),
				Arguments.of(BLACK_QUEEN, H2, H2.getAllSquaresInDirections(PieceMovementDirections.QUEEN, 8)),
				Arguments.of(BLACK_KING, E2, E2.getAllSquaresInDirections(PieceMovementDirections.KING, 1))
				);
	}
}