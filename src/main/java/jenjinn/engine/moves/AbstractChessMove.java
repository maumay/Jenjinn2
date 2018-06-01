package jenjinn.engine.moves;

import java.util.EnumSet;
import java.util.Set;

import jenjinn.engine.boardstate.BoardState;
import jenjinn.engine.boardstate.DataForReversingMove;
import jenjinn.engine.boardstate.DetailedPieceLocations;
import jenjinn.engine.enums.BoardSquare;
import jenjinn.engine.enums.CastleZone;
import jenjinn.engine.enums.DevelopmentPiece;

/**
 * @author ThomasB
 *
 */
public abstract class AbstractChessMove implements ChessMove
{
	private final BoardSquare source, target;

	public AbstractChessMove(final BoardSquare start, final BoardSquare target)
	{
		this.source = start;
		this.target = target;
	}

	@Override
	public void makeMove(final BoardState state, final DataForReversingMove unmakeDataStore)
	{
		assert unmakeDataStore.isConsumed();
		updateCastlingRights(state, unmakeDataStore);
		updatePieceLocations(state, unmakeDataStore);
		updateDevelopedPieces(state, unmakeDataStore);
		unmakeDataStore.setDiscardedHash(state.getHashCache().incrementHalfMoveCount(state.calculateHash()));
		state.switchActiveSide();
		unmakeDataStore.setConsumed(false);
	}

	void updateCastlingRights(final BoardState state, final DataForReversingMove unmakeDataStore)
	{
		if (state.getCastlingStatus().getCastlingRights().size() > 0) {
			final Set<CastleZone> rightsRemoved = EnumSet.copyOf(getAllRightsToBeRemoved());
			rightsRemoved.retainAll(state.getCastlingStatus().getCastlingRights());
			state.getCastlingStatus().getCastlingRights().removeAll(rightsRemoved);
			unmakeDataStore.setDiscardedCastlingRights(rightsRemoved);
		}
		else if (unmakeDataStore.getDiscardedCastlingRights().size() > 0) {
			unmakeDataStore.setDiscardedCastlingRights(EnumSet.noneOf(CastleZone.class));
		}
	}

	/**
	 * Responsible for updating the set of {@linkplain DevelopmentPiece} field in the parameter {@linkplain BoardState}
	 * and updating the pieceDeveloped field in the {@linkplain DataForReversingMove} parameter.
	 */
	void updateDevelopedPieces(final BoardState state, final DataForReversingMove unmakeDataStore)
	{
		final Set<DevelopmentPiece> developedPieces = state.getDevelopedPieces();
		unmakeDataStore.setPieceDeveloped(null);
		if (developedPieces.size() < 12) {
			final DevelopmentPiece potentialDevelopment = getPieceDeveloped();
			if (potentialDevelopment != null && !developedPieces.contains(potentialDevelopment)) {
				developedPieces.add(potentialDevelopment);
				unmakeDataStore.setPieceDeveloped(potentialDevelopment);
			}
		}
	}

	/**
	 * @return a set of {@linkplain CastleZone} which this move would remove if a state had all castling rights
	 * enabled.
	 */
	abstract Set<CastleZone> getAllRightsToBeRemoved();

	/**
	 * @return the piece which would be developed by this move assuming it has not already been developed.
	 */
	abstract DevelopmentPiece getPieceDeveloped();

	/**
	 * Responsible for updating the {@linkplain DetailedPieceLocations} field in the parameter {@linkplain BoardState},
	 * the enpassant square and also the half move clock. This means that it is responsible for updating the discardedPiece,
	 * discardedEnpassantSquare, discardedHalfMoveClock fields in the {@linkplain DataForReversingMove} parameter too.
	 */
	abstract void updatePieceLocations(final BoardState state, final DataForReversingMove unmakeDataStore);

	@Override
	public void reverseMove(final BoardState state, final DataForReversingMove unmakeDataStore)
	{
		assert !unmakeDataStore.isConsumed();
		state.switchActiveSide();
		state.getDevelopedPieces().remove(unmakeDataStore.getPieceDeveloped());
		state.getHalfMoveClock().setValue(unmakeDataStore.getDiscardedHalfMoveClockValue());
		state.setEnPassantSquare(unmakeDataStore.getDiscardedEnpassantSquare());
		state.getCastlingStatus().getCastlingRights().addAll(unmakeDataStore.getDiscardedCastlingRights());
		resetPieceLocations(state, unmakeDataStore);
		state.getHashCache().decrementHalfMoveCount(unmakeDataStore.getDiscardedHash());
		unmakeDataStore.setConsumed(true);
	}

	abstract void resetPieceLocations(final BoardState state, final DataForReversingMove unmakeDataStore);


	@Override
	public String toString()
	{
		return new StringBuilder(getClass().getSimpleName())
				.append("[source=")
				.append(source.name())
				.append("|target=")
				.append(target.name())
				.append("]")
				.toString();
	}

	@Override
	public BoardSquare getSource()
	{
		return source;
	}

	@Override
	public BoardSquare getTarget()
	{
		return target;
	}

	// Eclipse generated
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
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
		final AbstractChessMove other = (AbstractChessMove) obj;
		if (source != other.source)
			return false;
		if (target != other.target)
			return false;
		return true;
	}
}
