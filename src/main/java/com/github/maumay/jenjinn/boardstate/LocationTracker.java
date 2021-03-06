/**
 *
 */
package com.github.maumay.jenjinn.boardstate;

import static com.github.maumay.jenjinn.bitboards.Bitboard.intersects;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import com.github.maumay.jenjinn.base.Square;
import com.github.maumay.jenjinn.bitboards.BitboardIterator;
import com.github.maumay.jflow.iterables.RichIterable;
import com.github.maumay.jflow.iterators.Iter;
import com.github.maumay.jflow.iterators.RichIterator;

/**
 * @author t
 */
public final class LocationTracker implements RichIterable<Square>
{
	private final Set<Square> locs = EnumSet.noneOf(Square.class);
	private long allLocs;

	public LocationTracker(Set<Square> locations)
	{
		locs.addAll(locations);
		allLocs = iterator().mapToLong(sq -> sq.bitboard).fold(0L, (a, b) -> a | b);
	}

	public LocationTracker(long locations)
	{
		this(BitboardIterator.from(locations).toSet());
	}

	public long allLocs()
	{
		return allLocs;
	}

	public boolean contains(Square location)
	{
		return intersects(allLocs, location.bitboard);
	}

	public int pieceCount()
	{
		return locs.size();
	}

	void addLoc(Square location)
	{
		assert !intersects(allLocs, location.bitboard);
		allLocs ^= location.bitboard;
		locs.add(location);
	}

	void removeLoc(Square location)
	{
		assert intersects(allLocs, location.bitboard);
		allLocs ^= location.bitboard;
		locs.remove(location);
	}

	/**
	 * Note that this iterator makes no guarantee about the order in which squares
	 * appear in the iteration.
	 */
	@Override
	public RichIterator<Square> iter()
	{
		return Iter.over(locs);
	}

	public LocationTracker copy()
	{
		return new LocationTracker(new HashSet<>(locs));
	}

	@Override
	public int hashCode()
	{
		int prime = 31;
		int result = 1;
		result = prime * result + (int) (allLocs ^ (allLocs >>> 32));
		result = prime * result + ((locs == null) ? 0 : locs.hashCode());
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
		LocationTracker other = (LocationTracker) obj;
		if (allLocs != other.allLocs)
			return false;
		if (locs == null) {
			if (other.locs != null)
				return false;
		} else if (!locs.equals(other.locs))
			return false;
		return true;
	}
}
