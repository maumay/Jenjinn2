/**
 *
 */
package jenjinn.engine.boardstate;

import java.util.Arrays;

/**
 * @author ThomasB
 */
public final class StateHashCache
{
	private static final int CACHE_SIZE = 12;

	private final long[] hashCache;
	private int totalHalfMoveCount, cacheIndexer;

	public StateHashCache(long[] hashCache, int halfMoveCount)
	{
		if (hashCache.length != CACHE_SIZE) {
			throw new IllegalArgumentException();
		}
		this.hashCache = Arrays.copyOf(hashCache, CACHE_SIZE);
		this.totalHalfMoveCount = halfMoveCount;
		updateCacheIndexer();
	}

	public StateHashCache()
	{
		this(new long[CACHE_SIZE], 0);
	}

	public long incrementHalfMoveCount()
	{
		final long currentHash = hashCache[cacheIndexer];
		totalHalfMoveCount++;
		updateCacheIndexer();
		final long discardedHash = hashCache[cacheIndexer];
		hashCache[cacheIndexer] = currentHash;
		return discardedHash;
	}

	public void decrementHalfMoveCount(final long replacementHash)
	{
		hashCache[cacheIndexer] = replacementHash;
		totalHalfMoveCount--;
		updateCacheIndexer();
	}

	private void updateCacheIndexer()
	{
		cacheIndexer = totalHalfMoveCount % CACHE_SIZE;
	}

	public void xorFeatureWithCurrentHash(final long feature)
	{
		hashCache[cacheIndexer] ^= feature;
	}

	/*
	 * Generated by Eclipse.
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + cacheIndexer;
		result = prime * result + Arrays.hashCode(hashCache);
		result = prime * result + totalHalfMoveCount;
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final StateHashCache other = (StateHashCache) obj;
		if (cacheIndexer != other.cacheIndexer)
			return false;
		if (!Arrays.equals(hashCache, other.hashCache))
			return false;
		if (totalHalfMoveCount != other.totalHalfMoveCount)
			return false;
		return true;
	}
}