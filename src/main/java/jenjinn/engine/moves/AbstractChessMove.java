/**
 *
 */
package jenjinn.engine.moves;

import java.util.EnumSet;
import java.util.Set;

import jenjinn.engine.boardstate.BoardState;
import jenjinn.engine.boardstate.DataForReversingMove;
import jenjinn.engine.enums.BoardSquare;
import jenjinn.engine.enums.CastleZone;

/**
 * @author ThomasB
 *
 */
public abstract class AbstractChessMove implements ChessMove
{
	private final BoardSquare source, target;

	public AbstractChessMove(final BoardSquare start, final BoardSquare target) {
		this.source = start;
		this.target = target;
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

	@Override
	public void makeMove(final BoardState state, final DataForReversingMove unmakeDataStore)
	{
		assert unmakeDataStore.isConsumed();
		unmakeDataStore.setDiscardedHash(state.getHashCache().incrementHalfMoveCount());
		updateCastlingRights(state, unmakeDataStore);
		updatePieceLocations(state, unmakeDataStore);
		updateDevelopedPieces(state, unmakeDataStore);
		state.switchActiveSide();
		unmakeDataStore.setConsumed(false);
	}

	void updateCastlingRights(final BoardState state, final DataForReversingMove unmakeDataStore)
	{
		if (state.getCastlingStatus().getCastlingRights().size() > 0)
		{
			final Set<CastleZone> rightsRemoved = EnumSet.copyOf(CastleRightsRemoval.getRightsRemovedBy(this));
			rightsRemoved.retainAll(state.getCastlingStatus().getCastlingRights());
			state.getCastlingStatus().getCastlingRights().removeAll(rightsRemoved);
			unmakeDataStore.setDiscardedCastlingRights(rightsRemoved);

			for (final CastleZone rightRemoved : rightsRemoved) {
				state.getHashCache().xorFeatureWithCurrentHash(state.getStateHasher().getCastleRightsFeature(rightRemoved));
			}
		}
		else if (unmakeDataStore.getDiscardedCastlingRights().size() > 0)
		{
			unmakeDataStore.setDiscardedCastlingRights(EnumSet.noneOf(CastleZone.class));
		}
	}

	abstract void updatePieceLocations(final BoardState state, final DataForReversingMove unmakeDataStore);

	abstract void updateDevelopedPieces(final BoardState state, final DataForReversingMove unmakeDataStore);

	@Override
	public void reverseMove(final BoardState state, final DataForReversingMove unmakeDataStore)
	{
		assert !unmakeDataStore.isConsumed();
		state.switchActiveSide();
		state.getDevelopedPieces().remove(unmakeDataStore.getPieceDeveloped());
		state.getCastlingStatus().getCastlingRights().addAll(unmakeDataStore.getDiscardedCastlingRights());
		resetPieceLocations(state, unmakeDataStore);
		state.getHashCache().decrementHalfMoveCount(unmakeDataStore.getDiscardedHash());
		unmakeDataStore.setConsumed(true);
	}

	abstract void resetPieceLocations(final BoardState state, final DataForReversingMove unmakeDataStore);
}
