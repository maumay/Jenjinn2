/**
 *
 */
package com.github.maumay.jenjinn.boardstate;

import static com.github.maumay.jenjinn.bitboards.Bitboard.fold;
import static com.github.maumay.jenjinn.eval.piecesquaretables.TestingPieceSquareTables.getEndgameTables;
import static com.github.maumay.jenjinn.eval.piecesquaretables.TestingPieceSquareTables.getMidgameTables;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.maumay.jenjinn.base.Side;
import com.github.maumay.jenjinn.base.Square;
import com.github.maumay.jenjinn.eval.piecesquaretables.PieceSquareTables;
import com.github.maumay.jenjinn.pieces.ChessPieces;
import com.github.maumay.jenjinn.pieces.Piece;
import com.github.maumay.jenjinn.utils.BoardHasher;
import com.github.maumay.jflow.iterators.Iter;

/**
 * @author t
 */
class DetailedPieceLocationsTest
{
	@Test
	void testAddPieceAt()
	{
		List<Square> locationsToAddPieceAt = getLocationSquares();
		PieceSquareTables midTables = getMidgameTables(), endTables = getEndgameTables();
		DetailedPieceLocations locations = new DetailedPieceLocations(new long[12],
				midTables, endTables);

		int runningMidgameEval = locations.getMidgameEval(),
				runningEndgameEval = locations.getEndgameEval();
		long runningHash = locations.getSquarePieceFeatureHash();

		for (Piece piece : ChessPieces.WHITE) {
			Square loc = locationsToAddPieceAt.get(piece.ordinal());
			locations.addPieceAt(loc, piece);
			assertEquals(loc.bitboard, locations.locationsOf(piece));
			assertEquals(fold(locationsToAddPieceAt.subList(0, piece.ordinal() + 1)),
					locations.getWhiteLocations());
			assertEquals(0L, locations.getBlackLocations());

			runningMidgameEval += midTables.getLocationValue(piece, loc);
			assertEquals(runningMidgameEval, locations.getMidgameEval());
			runningEndgameEval += endTables.getLocationValue(piece, loc);
			assertEquals(runningEndgameEval, locations.getEndgameEval());
			runningHash ^= BoardHasher.INSTANCE.getSquarePieceFeature(loc, piece);
			assertEquals(runningHash, locations.getSquarePieceFeatureHash());
		}

		for (Piece piece : ChessPieces.BLACK) {
			Square loc = locationsToAddPieceAt.get(piece.ordinal());
			locations.addPieceAt(loc, piece);
			assertEquals(loc.bitboard, locations.locationsOf(piece));
			assertEquals(fold(locationsToAddPieceAt.subList(0, 6)),
					locations.getWhiteLocations());
			assertEquals(fold(locationsToAddPieceAt.subList(6, piece.ordinal() + 1)),
					locations.getBlackLocations());

			runningMidgameEval += midTables.getLocationValue(piece, loc);
			assertEquals(runningMidgameEval, locations.getMidgameEval());
			runningEndgameEval += endTables.getLocationValue(piece, loc);
			assertEquals(runningEndgameEval, locations.getEndgameEval());
			runningHash ^= BoardHasher.INSTANCE.getSquarePieceFeature(loc, piece);
			assertEquals(runningHash, locations.getSquarePieceFeatureHash());
		}
	}

	@Test
	void testRemovePieceAt()
	{
		List<Square> locationsToAddPieceAt = getLocationSquares();
		int nlocs = locationsToAddPieceAt.size();
		PieceSquareTables midTables = getMidgameTables(), endTables = getEndgameTables();
		long[] initialLocations = Iter.over(locationsToAddPieceAt)
				.mapToLong(s -> s.bitboard).toArray();
		DetailedPieceLocations locations = new DetailedPieceLocations(initialLocations,
				midTables, endTables);

		int runningMidgameEval = locations.getMidgameEval(),
				runningEndgameEval = locations.getEndgameEval();
		long runningHash = locations.getSquarePieceFeatureHash();

		for (Piece piece : ChessPieces.WHITE) {
			Square loc = locationsToAddPieceAt.get(piece.ordinal());
			locations.removePieceAt(loc, piece);
			assertEquals(0L, locations.locationsOf(piece));
			assertEquals(fold(locationsToAddPieceAt.subList(piece.ordinal() + 1, 6)),
					locations.getWhiteLocations());
			assertEquals(fold(locationsToAddPieceAt.subList(6, nlocs)),
					locations.getBlackLocations());

			runningMidgameEval -= midTables.getLocationValue(piece, loc);
			assertEquals(runningMidgameEval, locations.getMidgameEval());
			runningEndgameEval -= endTables.getLocationValue(piece, loc);
			assertEquals(runningEndgameEval, locations.getEndgameEval());
			runningHash ^= BoardHasher.INSTANCE.getSquarePieceFeature(loc, piece);
			assertEquals(runningHash, locations.getSquarePieceFeatureHash());
		}

		for (Piece piece : ChessPieces.BLACK) {
			Square loc = locationsToAddPieceAt.get(piece.ordinal());
			locations.removePieceAt(loc, piece);
			assertEquals(0L, locations.locationsOf(piece));
			assertEquals(0L, locations.getWhiteLocations());
			assertEquals(fold(locationsToAddPieceAt.subList(piece.ordinal() + 1, 12)),
					locations.getBlackLocations());

			runningMidgameEval -= midTables.getLocationValue(piece, loc);
			assertEquals(runningMidgameEval, locations.getMidgameEval());
			runningEndgameEval -= endTables.getLocationValue(piece, loc);
			assertEquals(runningEndgameEval, locations.getEndgameEval());
			runningHash ^= BoardHasher.INSTANCE.getSquarePieceFeature(loc, piece);
			assertEquals(runningHash, locations.getSquarePieceFeatureHash());
		}
	}

	@Test
	void testGetPieceAt()
	{
		List<Square> locationsToAddPieceAt = getLocationSquares();
		long[] initialLocations = Iter.over(locationsToAddPieceAt)
				.mapToLong(s -> s.bitboard).toArray();
		DetailedPieceLocations locations = new DetailedPieceLocations(initialLocations,
				getMidgameTables(), getEndgameTables());

		for (Piece piece : ChessPieces.ALL) {
			Square loc = locationsToAddPieceAt.get(piece.ordinal());
			assertEquals(piece, locations.getPieceAt(loc));
			assertEquals(piece, locations.getPieceAt(loc, piece.getSide()));
			assertNull(locations.getPieceAt(loc, piece.getSide().otherSide()),
					piece.name());
		}

		Square emptySquare = Square.of(63);
		assertNull(locations.getPieceAt(emptySquare));
		assertNull(locations.getPieceAt(emptySquare, Side.WHITE));
		assertNull(locations.getPieceAt(emptySquare, Side.BLACK));
	}

	private List<Square> getLocationSquares()
	{
		return Iter.between(0, 64, 4).take(12).mapToObject(Square::of).toList();
	}
}
