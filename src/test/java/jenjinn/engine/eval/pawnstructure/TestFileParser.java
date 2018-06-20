/**
 *
 */
package jenjinn.engine.eval.pawnstructure;

import static java.util.stream.Collectors.toList;
import static xawd.jflow.utilities.CollectionUtil.head;
import static xawd.jflow.utilities.CollectionUtil.tail;

import java.util.List;

import org.junit.jupiter.params.provider.Arguments;

import jenjinn.engine.enums.BoardSquare;
import jenjinn.engine.parseutils.CommonRegex;
import jenjinn.engine.utils.FileUtils;
import xawd.jflow.collections.FlowList;
import xawd.jflow.iterators.misc.IntPair;
import xawd.jflow.utilities.StringUtils;

/**
 * @author ThomasB
 */
public final class TestFileParser
{
	private TestFileParser()
	{
	}

	public static Arguments parse(String filename)
	{
		final Class<?> cls = TestFileParser.class;
		final List<String> lines = FileUtils.loadResourceFromPackageOf(cls, filename)
		.map(String::trim)
		.filter(s -> !(s.isEmpty() || s.startsWith("//")))
		.collect(toList());

		if (lines.size() == 8) {
			final String encodedWhiteLocs = head(lines), encodedBlackLocs = lines.get(1);
			final Long whiteLocs = decodeLocations(encodedWhiteLocs);
			final Long blackLocs = decodeLocations(encodedBlackLocs);

			final ExpectedValues expected = new ExpectedValues(
					decodeIntegerPair(lines.get(2)),
					decodeIntegerPair(lines.get(3)),
					decodeIntegerPair(lines.get(4)),
					decodeIntegerPair(lines.get(5)),
					decodeIntegerSequence(lines.get(6)),
					decodeIntegerSequence(lines.get(7)));

			return Arguments.of(whiteLocs, blackLocs, expected);
		}
		else {
			throw new IllegalArgumentException(filename + " is formatted incorrectly.");
		}
	}

	private static FlowList<Integer> decodeIntegerSequence(String encodedSequence)
	{
		final String num = "([0-9]+)";
		if (!encodedSequence.matches("^" + num + "( " + num + ")+$")) {
			throw new IllegalArgumentException(encodedSequence);
		}
		return StringUtils.getAllMatches(encodedSequence, num)
				.map(Integer::parseInt)
				.toList();
	}

	private static IntPair decodeIntegerPair(String encodedPair)
	{
		final String num = "([0-9]+)";
		if (!encodedPair.matches("^" + num + " +" + num + "$")) {
			throw new IllegalArgumentException(encodedPair);
		}
		final FlowList<Integer> decoded = StringUtils.getAllMatches(encodedPair, num)
				.map(Integer::parseInt)
				.toList();

		return IntPair.of(head(decoded), tail(decoded));
	}

	private static Long decodeLocations(String encodedLocs)
	{
		final String sq = CommonRegex.SINGLE_SQUARE;
		if (!encodedLocs.matches("^" + sq + "( " + sq + ")*$")) {
			throw new IllegalArgumentException(encodedLocs);
		}
		return StringUtils.getAllMatches(encodedLocs, sq)
				.map(String::toUpperCase)
				.map(BoardSquare::valueOf)
				.mapToLong(BoardSquare::asBitboard)
				.reduce(0L, (a, b) -> a | b);
	}
}