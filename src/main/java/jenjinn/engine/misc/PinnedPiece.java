/**
 *
 */
package jenjinn.engine.misc;

import jenjinn.engine.enums.BoardSquare;

/**
 * Represents a piece at the given location whose movement area is constrained
 * to lie on the given bitboard.
 *
 * @author ThomasB
 */
public final class PinnedPiece {

	private final BoardSquare location;
	private final long constrainedArea;

	public PinnedPiece(final BoardSquare location, final long constrainedArea)
	{
		this.location = location;
		this.constrainedArea = constrainedArea;
	}

	public BoardSquare getLocation()
	{
		return location;
	}

	public long getConstrainedArea()
	{
		return constrainedArea;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (constrainedArea ^ (constrainedArea >>> 32));
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final PinnedPiece other = (PinnedPiece) obj;
		if (constrainedArea != other.constrainedArea)
			return false;
		if (location != other.location)
			return false;
		return true;
	}
}
