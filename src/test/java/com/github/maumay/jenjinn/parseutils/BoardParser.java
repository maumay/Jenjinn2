/**
 *
 */
package com.github.maumay.jenjinn.parseutils;

import static com.github.maumay.jenjinn.eval.piecesquaretables.TestingPieceSquareTables.getEndgameTables;
import static com.github.maumay.jenjinn.eval.piecesquaretables.TestingPieceSquareTables.getMidgameTables;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.github.maumay.jenjinn.base.CastleZone;
import com.github.maumay.jenjinn.base.DevelopmentPiece;
import com.github.maumay.jenjinn.base.Side;
import com.github.maumay.jenjinn.base.Square;
import com.github.maumay.jenjinn.bitboards.Bitboard;
import com.github.maumay.jenjinn.boardstate.BoardState;
import com.github.maumay.jenjinn.boardstate.CastlingStatus;
import com.github.maumay.jenjinn.boardstate.DetailedPieceLocations;
import com.github.maumay.jenjinn.boardstate.HalfMoveCounter;
import com.github.maumay.jenjinn.boardstate.HashCache;
import com.github.maumay.jenjinn.eval.piecesquaretables.PieceSquareTables;
import com.github.maumay.jenjinn.utils.BoardHasher;
import com.github.maumay.jflow.iterators.Iter;
import com.github.maumay.jflow.utils.Exceptions;
import com.github.maumay.jflow.utils.Strings;
import com.github.maumay.jflow.vec.Vec;

/**
 * Lets us construct {@linkplain BoardState} instances in an easy way via
 * external files.
 *
 * @author t
 */
public final class BoardParser
{
	private static final int DEFAULT_MOVE_COUNT = 20;

	private BoardParser()
	{
	}

	public static BoardState parse(Vec<String> attributes)
	{
		return parse(attributes, DEFAULT_MOVE_COUNT);
	}

	public static BoardState parse(Vec<String> attributes, int totalMoveCount)
	{
		if (attributes.size() != 9) {
			throw new IllegalArgumentException();
		}
		Vec<String> atts = attributes.map(String::trim).map(String::toLowerCase);
		DetailedPieceLocations pieceLocations = constructPieceLocations(atts.get(0),
				atts.get(1));
		HalfMoveCounter halfMoveCount = constructHalfMoveCounter(atts.get(2));
		CastlingStatus castlingStatus = constructCastlingStatus(atts.get(3), atts.get(4),
				atts.get(5));
		Set<DevelopmentPiece> developedPieces = constructDevelopedPieces(atts.get(6));
		Side activeSide = constructActiveSide(atts.get(7));
		Square enpassantSquare = constructEnpassantSquare(atts.get(8));
		long hashOfConstructedState = pieceLocations.getSquarePieceFeatureHash()
				^ BoardHasher.INSTANCE.hashNonPieceFeatures(activeSide, enpassantSquare,
						castlingStatus);
		HashCache hashCache = constructDummyHashCache(hashOfConstructedState,
				totalMoveCount);

		return new BoardState(hashCache, pieceLocations, halfMoveCount, castlingStatus,
				developedPieces, activeSide, enpassantSquare);
	}

	private static HashCache constructDummyHashCache(long initializedBoardHash,
			int totalMoveCount)
	{
		int cacheSize = HashCache.CACHE_SIZE;
		long[] cache = Iter.until(cacheSize).mapToLong(i -> i + 1).toArray();
		cache[totalMoveCount % cacheSize] = initializedBoardHash;
		return new HashCache(cache, totalMoveCount);
	}

	private static Square constructEnpassantSquare(String enpassantSquare)
	{
		Exceptions.require(enpassantSquare.trim()
				.matches("^enpassant_square: *((none)|([a-h][1-8]))$"));
		Optional<String> squareMatch = Strings.firstMatch(enpassantSquare, "[a-h][1-8]");
		return squareMatch.isPresent() ? Square.valueOf(squareMatch.get().toUpperCase())
				: null;
	}

	private static Side constructActiveSide(String activeSide)
	{
		Exceptions.require(activeSide.trim().matches("^active_side: *(white|black)$"));
		Optional<String> whiteMatch = Strings.firstMatch(activeSide, "white");
		return whiteMatch.isPresent() ? Side.WHITE : Side.BLACK;
	}

	private static Set<DevelopmentPiece> constructDevelopedPieces(String developedPieces)
	{
		Exceptions.require(developedPieces.trim().matches(
				"^developed_pieces:(( *none)|(( *[a-h][1-8])( +[a-h][1-8]){0,11}))$"),
				developedPieces);
		Vec<String> squaresMatched = Strings.allMatches(developedPieces, "[a-h][1-8]")
				.toVec();
		Set<Square> uniqueSquares = squaresMatched.map(String::toUpperCase)
				.map(Square::valueOf).toSet();
		if (uniqueSquares.size() != squaresMatched.size()) {
			throw new IllegalArgumentException(developedPieces);
		}
		// Will error here if invalid square was passed, cannot add null to enumset.
		return Iter.over(uniqueSquares).map(DevelopmentPiece::fromStartSquare)
				.toCollection(() -> EnumSet.noneOf(DevelopmentPiece.class));
	}

	private static CastlingStatus constructCastlingStatus(String rights,
			String whiteStatus, String blackStatus)
	{
		Exceptions.require(
				rights.trim().matches("^castling_rights:( *wk)?( +wq)?( +bk)?( +bq)?$"));
		Exceptions.require(
				whiteStatus.trim().matches("^white_castle_status: *(none|wk|wq|bk|bq)$"));
		Exceptions.require(
				blackStatus.trim().matches("^black_castle_status: *(none|wk|wq|bk|bq)$"));

		Map<String, CastleZone> regexMatchers = CastleZone.ALL
				.toMap(CastleZone::getSimpleIdentifier, Function.identity());

		Set<CastleZone> rightSet = Iter.over(regexMatchers.keySet())
				.filter(rx -> Strings.matchesAnywhere(rights, rx)).map(regexMatchers::get)
				.toCollection(() -> EnumSet.noneOf(CastleZone.class));

		CastleZone whiteCastleStatus = Iter.over(regexMatchers.keySet())
				.filter(rx -> Strings.matchesAnywhere(whiteStatus, rx))
				.map(regexMatchers::get).nextOp().orElse(null);

		CastleZone blackCastleStatus = Iter.over(regexMatchers.keySet())
				.filter(rx -> Strings.matchesAnywhere(blackStatus, rx))
				.map(regexMatchers::get).nextOp().orElse(null);

		return new CastlingStatus(rightSet, whiteCastleStatus, blackCastleStatus);
	}

	private static HalfMoveCounter constructHalfMoveCounter(String clockString)
	{
		Exceptions.require(clockString.trim().matches("^half_move_clock: *[0-9]+$"));
		return new HalfMoveCounter(
				Integer.parseInt(Strings.firstMatch(clockString, "[0-9]+").get()));
	}

	private static DetailedPieceLocations constructPieceLocations(String whiteLocs,
			String blackLocs)
	{
		/* Arbitrary number of boardsquare separated by whitespace in parentheses */
		String groupedSquares = "\\( *([a-h][1-8] *)?( +[a-h][1-8])* *\\)";
		String sixGroupedSquareSets = Iter.until(6).mapToObject(i -> groupedSquares)
				.fold(" *", (a, b) -> a + " +" + b);

		Exceptions.require(
				whiteLocs.trim().matches("^white_pieces:" + sixGroupedSquareSets + "$"));
		Exceptions.require(
				blackLocs.trim().matches("^black_pieces:" + sixGroupedSquareSets + "$"));

		PieceSquareTables midTables = getMidgameTables(), endTables = getEndgameTables();
		long[] locations = Strings.allMatches(whiteLocs + blackLocs, groupedSquares)
				.map(x -> Strings.allMatches(x, "[a-h][1-8]"))
				.map(xs -> xs.map(String::toUpperCase))
				.map(xs -> xs.map(Square::valueOf).toVec()).mapToLong(Bitboard::fold)
				.toArray();
		return new DetailedPieceLocations(locations, midTables, endTables);
	}
}
