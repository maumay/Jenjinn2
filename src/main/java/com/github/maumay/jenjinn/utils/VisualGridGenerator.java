/**
 *
 */
package com.github.maumay.jenjinn.utils;

import static java.util.Arrays.asList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.maumay.jenjinn.base.Square;
import com.github.maumay.jenjinn.bitboards.BitboardIterator;
import com.github.maumay.jenjinn.boardstate.DetailedPieceLocations;
import com.github.maumay.jenjinn.pieces.ChessPieces;
import com.github.maumay.jenjinn.pieces.Piece;
import com.github.maumay.jflow.iterators.Iter;

/**
 * @author ThomasB
 *
 */
public final class VisualGridGenerator
{
	private VisualGridGenerator()
	{
	}

	public static String from(String title, Map<Square, Piece> locations)
	{
		return StringifyBoard
				.formatGrid(new TitledVisualGrid(title, Iter.over(locations.keySet())
						.toMap(x -> x, x -> CharPair.from(locations.get(x)))));
	}

	public static String from(DetailedPieceLocations locations)
	{
		Map<Square, CharPair> pieceMapping = new HashMap<>();
		ChessPieces.ALL.forEach(piece -> {
			BitboardIterator.from(locations.locationsOf(piece))
					.forEach(square -> pieceMapping.put(square, CharPair.from(piece)));
		});

		List<TitledVisualGrid> grids = asList(
				new TitledVisualGrid("Pieces", pieceMapping),
				TitledVisualGrid.from("White pieces", locations.getWhiteLocations()),
				TitledVisualGrid.from("Black pieces", locations.getBlackLocations()));

		return StringifyBoard.formatGrids(grids);
	}

	public static String from(String title, long bitboard)
	{
		return StringifyBoard.formatGrid(TitledVisualGrid.from(title, bitboard));
	}

	public static String from(long... bitboards)
	{
		return StringifyBoard.formatGrids(
				Iter.longs(bitboards).mapToObject(TitledVisualGrid::from).toList());
	}

	public static TitledVisualGrid from(String title, BasicPieceLocations locations)
	{
		Map<Square, CharPair> locs = BitboardIterator.from(locations.getWhite())
				.toMap(x -> x, i -> new CharPair('X', 'W'));
		locs.putAll(BitboardIterator.from(locations.getBlack()).toMap(x -> x,
				i -> new CharPair('X', 'B')));
		return new TitledVisualGrid(title, locs);
	}

	public static TitledVisualGrid fromPieceLocations(BasicPieceLocations locations)
	{
		return from("", locations);
	}
}
