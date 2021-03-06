/**
 *
 */
package com.github.maumay.jenjinn.boardstate;

/**
 * @author ThomasB
 *
 */
public final class HalfMoveCounter
{
	private int halfMoveClock;

	public HalfMoveCounter(final int initialValue)
	{
		this.halfMoveClock = initialValue;
	}

	public HalfMoveCounter()
	{
		this(0);
	}

	public int getValue()
	{
		return halfMoveClock;
	}

	public void setValue(final int value)
	{
		halfMoveClock = value;
	}

	public void incrementValue()
	{
		halfMoveClock++;
	}

	public void resetValue()
	{
		halfMoveClock = 0;
	}

	@Override
	public String toString()
	{
		return new StringBuilder("HalfMoveclock[")
				.append(halfMoveClock)
				.append("]")
				.toString();
	}

	public HalfMoveCounter copy()
	{
		return new HalfMoveCounter(halfMoveClock);
	}

	/*
	 * Generated by Eclipse.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + halfMoveClock;
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
		final HalfMoveCounter other = (HalfMoveCounter) obj;
		if (halfMoveClock != other.halfMoveClock)
			return false;
		return true;
	}
}
