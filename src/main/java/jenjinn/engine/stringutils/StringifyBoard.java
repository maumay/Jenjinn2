/**
 *
 */
package jenjinn.engine.stringutils;

import static jenjinn.engine.bitboards.BitboardUtils.getSetBitIndices;
import static jenjinn.engine.bitboards.Bitboards.rankBitboard;

import java.util.List;
import java.util.Map;

import jenjinn.engine.enums.BoardSquare;
import jenjinn.engine.enums.ChessPiece;
import jenjinn.engine.misc.PieceLocations;
import xawd.jflow.iterators.construction.IterRange;
import xawd.jflow.iterators.construction.Iterate;
import xawd.jflow.iterators.construction.ReverseIterate;

/**
 * @author t
 *
 */
public final class StringifyBoard {

	private static final String BOARD_INDENTER = ">>    ";

	private StringifyBoard() {
		//		+--+
		//	    |BN|
		//	    +--+
		//	    |WQ|
		//		+--+
	}

	public static String fromBitboard(final long bitboard)
	{
		return fromSquareToCharMapping(
				Iterate.over(getSetBitIndices(bitboard))
				.mapToObject(BoardSquare::fromIndex)
				.toMap(x -> x, x -> new char[] {'X', 'X'})
				);
	}

	public static String fromPieceLocations(final PieceLocations locations)
	{
		final Map<BoardSquare, char[]> locs = Iterate.over(getSetBitIndices(locations.getWhite())).toMap(BoardSquare::fromIndex, i -> new char[] {'X', 'W'});
		locs.putAll(Iterate.over(getSetBitIndices(locations.getBlack())).toMap(BoardSquare::fromIndex, i -> new char[] {'X', 'B'}));
		return fromSquareToCharMapping(locs);
	}

	public static String fromSquareToPieceMapping(final Map<BoardSquare, ChessPiece> pieceLocations)
	{
		return fromSquareToCharMapping(Iterate.over(pieceLocations.keySet()).toMap(x -> x, x -> CharGrid.createPieceChars(pieceLocations.get(x))));
	}

	public static String fromSquareToCharMapping(final Map<BoardSquare, char[]> squareEntries)
	{
		final char[] grid = CharGrid.getNewGrid();

		BoardSquare.iterateAll().forEach(square ->
		{
			final int gridIndex = CharGrid.mapToGridIndex(square);
			final char[] entry = squareEntries.containsKey(square)? squareEntries.get(square) : new char[2];
			System.arraycopy(entry, 0, grid, gridIndex, 2);
		});

		return formatGrid(grid);
	}

	private static String formatGrid(final char[] grid)
	{
		final int w = CharGrid.BOARD_CHAR_WIDTH, h = CharGrid.BOARD_LINE_HEIGHT;

		final List<String> lines = IterRange.to(h).mapToObject(i ->
		{
			final StringBuilder sb = new StringBuilder();
			final int lineStart = i*w;
			IterRange.to(w).forEach(j -> sb.append(grid[lineStart + j]));
			return sb.reverse().toString();
		}).toList();

		final StringBuilder sb = new StringBuilder(BOARD_INDENTER + System.lineSeparator());

		ReverseIterate.over(lines).forEach(line ->
		{
			sb.append(BOARD_INDENTER);
			sb.append(line);
			sb.append(System.lineSeparator());
		});

		sb.append(BOARD_INDENTER + System.lineSeparator());
		return sb.toString();
	}

	public static void main(final String[] args) {
		System.out.println(fromBitboard(rankBitboard(0) | rankBitboard(5)));
	}
}
