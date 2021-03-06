/**
 *
 */
package com.github.maumay.jenjinn.eval.piecesquaretables;

import com.github.maumay.jenjinn.base.Square;
import com.github.maumay.jenjinn.boardstate.LocationTracker;
import com.github.maumay.jenjinn.pieces.ChessPieces;
import com.github.maumay.jenjinn.pieces.Piece;
import com.github.maumay.jflow.iterators.Iter;
import com.github.maumay.jflow.vec.Vec;

/**
 * @author ThomasB
 *
 */
public final class PieceSquareTables
{
	private final Vec<PieceSquareTable> tables;

	public PieceSquareTables(Vec<PieceSquareTable> whiteTables)
	{
		if (whiteTables.size() != 6 || Iter.until(6)
				.anyMatch(i -> whiteTables.get(i).getAssociatedPiece().ordinal() != i)) {
			throw new IllegalArgumentException();
		}
		this.tables = whiteTables.append(whiteTables.map(PieceSquareTable::invertValues));
	}

	public int getLocationValue(Piece piece, Square location)
	{
		return tables.get(piece.ordinal()).getValueAt(location);
	}

	public int evaluateLocations(Vec<LocationTracker> pieceLocations)
	{
		if (pieceLocations.size() != 12) {
			throw new IllegalArgumentException();
		}
		int eval = 0;
		for (int i = 0; i < pieceLocations.size(); i++) {
			PieceSquareTable pieceTable = tables.get(i);
			eval += pieceLocations.get(i).iterator()
					.mapToInt(loc -> pieceTable.getValueAt(loc)).fold(0, (a, b) -> a + b);
		}
		return eval;
	}

	@Override
	public int hashCode()
	{
		int prime = 31;
		int result = 1;
		result = prime * result + ((tables == null) ? 0 : tables.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PieceSquareTables other = (PieceSquareTables) obj;
		if (tables == null) {
			if (other.tables != null)
				return false;
		} else if (!tables.equals(other.tables))
			return false;
		return true;
	}

	public static PieceSquareTables endgame()
	{
		return new PieceSquareTables(ChessPieces.WHITE.map(p -> TableParser.parseFile(p,
				p.name().substring(6).toLowerCase() + "-endgame")));
	}

	public static PieceSquareTables midgame()
	{
		return new PieceSquareTables(ChessPieces.WHITE.map(p -> TableParser.parseFile(p,
				p.name().substring(6).toLowerCase() + "-midgame")));
	}
}
