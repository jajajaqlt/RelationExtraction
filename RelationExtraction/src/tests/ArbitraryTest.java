package tests;

import info.olteanu.interfaces.StringFilter;
import info.olteanu.utils.TextNormalizer;

import java.util.regex.Pattern;

import com.google.common.base.CharMatcher;

public class ArbitraryTest {
	public static void main(String[] args) throws Exception {
		// ArrayList<Integer> ints = new ArrayList<Integer>();
		// ints.add(1);
		// ints.add(2);
		// System.out.println(ints.contains(1));
		// System.out.println(ints.contains(0));

		String line = "süme string";
		boolean isAscii = CharMatcher.ASCII.matchesAllOf(line);
		System.out.println("Is the sentence in ascii?");
		System.out.println(isAscii);
		System.out.println("Converted:");
		String normalizedText = normalizeString(line);
		System.out.println(normalizedText);
		System.out.println("Question mark replaced:");
		String asciiText2 = filterNonAscii(line);
		System.out.println(asciiText2);
		// String asciiText = filterNonAscii(normalizedText);
	}

	/**
	 *
	 * @param text
	 * @return ASCII encoding of text with non ASCII characters replaced by ?
	 * @throws UnsupportedEncodingException
	 */
	public static String filterNonAscii(String text) throws Exception {
		String aText;
		byte[] b = text.getBytes("US-ASCII");
		aText = new String(b, "US-ASCII");
		return aText;
	}

	/**
	 *
	 * @param str
	 * @return Normalized version of str with accented characters replaced by
	 *         unaccented version and with diacritics removed. E.g. Ö -> O
	 */
	public static String normalizeString(String str)
			throws ClassNotFoundException {
		// TextNormalizer code from phramer.org
		// Allows compilation under both Java 5 and Java 6
		StringFilter stringFilter = TextNormalizer
				.getNormalizationStringFilter();
		String nfdNormalizedString = stringFilter.filter(str);

		// Normalizer is Java 6 only
		// String nfdNormalizedString = java.text.Normalizer.normalize(str,
		// Normalizer.Form.NFD);
		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		return pattern.matcher(nfdNormalizedString).replaceAll("");
	}
}
