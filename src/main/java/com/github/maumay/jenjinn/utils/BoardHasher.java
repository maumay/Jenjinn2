/**
 *
 */
package com.github.maumay.jenjinn.utils;

import java.util.Random;

import com.github.maumay.jenjinn.base.CastleZone;
import com.github.maumay.jenjinn.base.Side;
import com.github.maumay.jenjinn.base.Square;
import com.github.maumay.jenjinn.boardstate.CastlingStatus;
import com.github.maumay.jenjinn.boardstate.LocationTracker;
import com.github.maumay.jenjinn.pieces.ChessPieces;
import com.github.maumay.jenjinn.pieces.Piece;
import com.github.maumay.jflow.iterators.Iter;
import com.github.maumay.jflow.vec.Vec;

/**
 * @author ThomasB
 *
 */
public enum BoardHasher
{
	INSTANCE(0x110894L);

	private final Vec<long[]> boardSquareFeatures;
	private final long[] castleRightsFeatures;
	private final long[] enpassantFileFeatures;
	private final long blackToMoveFeature;

	private BoardHasher(long seed)
	{
		if (!seedIsValid(seed)) {
			throw new IllegalArgumentException();
		}
		final Random numberGenerator = new Random(seed);
		boardSquareFeatures = Square.ALL.map(x -> randomArray(12, numberGenerator));
		castleRightsFeatures = randomArray(4, numberGenerator);
		enpassantFileFeatures = randomArray(8, numberGenerator);
		blackToMoveFeature = numberGenerator.nextLong();
	}

	private boolean seedIsValid(long seed)
	{
		Random r = new Random(seed);
		return Iter.until(800).mapToObject(i -> r.nextLong()).toSet().size() == 800;
	}

	private long[] randomArray(int length, Random numberGenerator)
	{
		return Iter.until(length).mapToLong(i -> numberGenerator.nextLong()).toArray();
	}

	public long getSquarePieceFeature(Square square, Piece piece)
	{
		return boardSquareFeatures.get(square.ordinal())[piece.ordinal()];
	}

	public long getCastleRightsFeature(CastleZone zone)
	{
		return castleRightsFeatures[zone.ordinal()];
	}

	public long getEnpassantFileFeature(Square enPassantSquare)
	{
		return enpassantFileFeatures[enPassantSquare.ordinal() % 8];
	}

	public long getBlackToMoveFeature()
	{
		return blackToMoveFeature;
	}

	public long hashPieceLocations(Vec<LocationTracker> pieceLocations)
	{
		if (pieceLocations.size() != 12) {
			throw new IllegalArgumentException();
		}
		long hash = 0L;
		for (Piece piece : ChessPieces.ALL) {
			hash ^= pieceLocations.get(piece.ordinal()).iterator()
					.mapToLong(loc -> getSquarePieceFeature(loc, piece))
					.fold(0L, (a, b) -> a ^ b);
		}
		return hash;
	}

	public long hashNonPieceFeatures(Side activeSide, Square enpassantSquare,
			CastlingStatus castlingStatus)
	{
		long hash = activeSide.isWhite() ? 0L : getBlackToMoveFeature();
		hash ^= enpassantSquare == null ? 0L : getEnpassantFileFeature(enpassantSquare);
		hash ^= Iter.over(castlingStatus.getCastlingRights())
				.mapToLong(this::getCastleRightsFeature).fold(0L, (a, b) -> a ^ b);
		return hash;
	}
}
