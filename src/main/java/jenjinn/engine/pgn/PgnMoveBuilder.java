/**
 *
 */
package jenjinn.engine.pgn;

import static xawd.jflow.utilities.CollectionUtil.head;
import static xawd.jflow.utilities.CollectionUtil.tail;
import static xawd.jflow.utilities.Strings.findFirstMatch;
import static xawd.jflow.utilities.Strings.getAllMatches;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import jenjinn.engine.boardstate.BoardState;
import jenjinn.engine.boardstate.DetailedPieceLocations;
import jenjinn.engine.boardstate.calculators.LegalMoves;
import jenjinn.engine.enums.BoardSquare;
import jenjinn.engine.enums.CastleZone;
import jenjinn.engine.moves.CastleMove;
import jenjinn.engine.moves.ChessMove;
import jenjinn.engine.moves.PromotionMove;
import jenjinn.engine.pieces.ChessPieces;
import xawd.jflow.iterators.factories.Iterate;
import xawd.jflow.utilities.Strings;

/**
 * @author ThomasB
 *
 */
public final class PgnMoveBuilder
{
	/**
	 * Maps PGN identifiers to piece ordinals mod 6.
	 */
	private static final Map<Character, Integer> CHARACTER_PIECE_MAP = Iterate.overInts('N', 'B', 'R', 'Q', 'K')
			.zipWith(Iterate.over(ChessPieces.white()).drop(1))
			.toMap(p -> Character.valueOf((char) p.getInt()), p -> p.getElement().ordinal());


	private static final String FILE = "([a-h])", RANK = "([1-8])";
	private static final String SQUARE = "(" + FILE + RANK +")";
	private static final String PIECE = "(N|B|R|Q|K)";
	private static final String CHECK = "(\\+|#)";

	public static final String CASTLE_MOVE = "(O-O(-O)?)";
	public static final String PROMOTION_MOVE = "(([a-h]x)?" + SQUARE + "=Q" + ")";
	public static final String STANDARD_MOVE = "(" + PIECE + "?([a-h]|[1-8]|([a-h][1-8]))?x?" + SQUARE + ")";
	public static final String MOVE = "(" + STANDARD_MOVE + "|" + PROMOTION_MOVE + "|" + CASTLE_MOVE + ")";

	public static final String EXCLUDED_PROMOTION_MOVE = "(([a-h]x)?" + SQUARE + "=(N|B|R)" + CHECK + "?)";

	private PgnMoveBuilder()
	{
	}

	public static void main(String[] args)
	{
		final String pgn = "1.e4 e6 2.d4 d5 3.e5 c5 4.c3 Nc6 5.Nf3 Qb6 6.a3 c4 7.Nbd2 Na5 8.Rb1 "
				+ "Bd7 9.g3 Ne7 10.h4 Nb3 11.Nxb3 Ba4 12.Nfd2 Nc6 13.Bh3 Na5 14.O-O Nxb3 "
				+ "15.Nf3 O-O-O 1-0";

		System.out.println(Strings.getAllMatches(pgn, MOVE));
	}

	public static ChessMove convertPgnCommand(final BoardState currentState, final String moveCommand) throws BadPgnException
	{
		final Set<ChessMove> legalMoves = LegalMoves.getMoves(currentState).toSet();

		if (moveCommand.matches(CASTLE_MOVE)) {
			return decodeCastleMove(currentState, moveCommand, legalMoves);
		}
		else if (moveCommand.matches(PROMOTION_MOVE)) {
			return decodePromotionMove(currentState, moveCommand, legalMoves);
		}
		else if (moveCommand.matches(STANDARD_MOVE)) {
			return decodeStandardMove(currentState, moveCommand, legalMoves);
		}
		else {
			throw new BadPgnException(moveCommand);
		}
	}

	private static ChessMove decodeStandardMove(BoardState state, String moveCommand, Set<ChessMove> legalMoves) throws BadPgnException
	{
		final String mc = moveCommand;
		final Supplier<BadPgnException> exSupplier = () -> new BadPgnException(moveCommand);

		final List<BoardSquare> encodedSquares = Iterate.over(getAllMatches(mc, SQUARE))
				.map(String::toUpperCase)
				.map(BoardSquare::valueOf)
				.toList();

		if (encodedSquares.size() == 1) {
			final BoardSquare target = tail(encodedSquares);
			final List<String> files = getAllMatches(mc, FILE), ranks = getAllMatches(mc, RANK);
			final char pieceIdentifier = findFirstMatch(mc, PIECE).orElse("P").charAt(0);
			final int pieceOrdinalMod6 = CHARACTER_PIECE_MAP.getOrDefault(pieceIdentifier, 0);
			final DetailedPieceLocations plocs = state.getPieceLocations();
			final List<ChessMove> candidates = Iterate.over(legalMoves)
					.filter(mv -> mv.getTarget() == target && (plocs.getPieceAt(mv.getSource()).ordinal() % 6) == pieceOrdinalMod6)
					.toList();

			if (candidates.size() == 1) {
				return head(candidates);
			}
			else if (files.size() == 2) {
				final char sourceFile = head(files).toUpperCase().charAt(0);
				return Iterate.over(candidates)
						.filter(mv -> mv.getSource().name().charAt(0) == sourceFile)
						.safeNext().orElseThrow(() -> new BadPgnException(mc + ", " + sourceFile + ", " + candidates));
			}
			else if (ranks.size() == 2) {
				final char sourceRank = head(ranks).charAt(0);
				return Iterate.over(candidates)
						.filter(mv -> mv.getSource().name().charAt(1) == sourceRank)
						.safeNext().orElseThrow(() -> new BadPgnException(mc + ", " + sourceRank + ", " + candidates));
			}
			else {
				throw exSupplier.get();
			}
		}
		else if (encodedSquares.size() == 2) {
			final BoardSquare source = head(encodedSquares), target = tail(encodedSquares);
			return Iterate.over(legalMoves)
					.filter(mv -> mv.getSource() == source && mv.getTarget() == target)
					.safeNext().orElseThrow(exSupplier);
		}
		else {
			throw exSupplier.get();
		}
	}

	private static ChessMove decodePromotionMove(BoardState state, String moveCommand, Set<ChessMove> legalMoves) throws BadPgnException
	{
		final String mc = moveCommand;
		final Supplier<BadPgnException> exSupplier = () -> new BadPgnException(moveCommand);

		final String encodedTarget = findFirstMatch(mc, SQUARE).orElseThrow(exSupplier);
		final BoardSquare target = BoardSquare.valueOf(encodedTarget.toUpperCase());
		final List<PromotionMove> candidates = Iterate.over(legalMoves)
				.filterAndCastTo(PromotionMove.class)
				.filter(mv -> mv.getTarget() == target)
				.toList();

		if (candidates.size() == 1) {
			return head(candidates);
		}
		else if (candidates.size() == 2) {
			final char file = mc.charAt(0);
			return Iterate.over(candidates)
					.filter(mv -> mv.getSource().name().charAt(0) == file)
					.safeNext().orElseThrow(exSupplier);
		}
		else {
			throw exSupplier.get();
		}
	}

	private static ChessMove decodeCastleMove(BoardState state, String moveCommand, Set<ChessMove> legalMoves) throws BadPgnException
	{
		final String mc = moveCommand;
		final CastleZone kingSide = state.getActiveSide().isWhite()? CastleZone.WHITE_KINGSIDE : CastleZone.BLACK_KINGSIDE;
		final CastleZone queenSide = state.getActiveSide().isWhite()? CastleZone.WHITE_QUEENSIDE : CastleZone.BLACK_QUEENSIDE;
		final ChessMove mv = mc.matches("O-O")? new CastleMove(kingSide) : new CastleMove(queenSide);

		if (legalMoves.contains(mv)) {
			return mv;
		}
		else {
			throw new BadPgnException(moveCommand); //
		}
	}
}
