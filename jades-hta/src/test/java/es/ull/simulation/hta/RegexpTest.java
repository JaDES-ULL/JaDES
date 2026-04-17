package es.ull.simulation.hta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class RegexpTest {
	private static final String DIST_NAME = "DISTNAME";
	private static final String DIST_SCALE = "DISTSCALE";
	private static final String DIST_SCALE_SIGN = "DISTSCALESIGN";
	private static final String DIST_OFFSET = "DISTOFFSET";
	private static final String DIST_PARAM1 = "DISTPARAM1";
	private static final String DIST_PARAM2 = "DISTPARAM2";
	private static final String DIST_PARAM3 = "DISTPARAM3";
	private static final String DIST_PARAM4 = "DISTPARAM4";
	private static final String REG_EXP = "^((?<" + 
					DIST_OFFSET + ">[+-]?[0-9]+\\.?[0-9]*)?(?<" +
					DIST_SCALE_SIGN + ">[+-])?((?<" + 
					DIST_SCALE + ">[0-9]+\\.?[0-9]*)\\*)?(?<" + 
					DIST_NAME +">[A-Za-z]+)\\((?<" + 
					DIST_PARAM1 + ">[+-]?[0-9]+\\.?[0-9]*)(,(?<" + 
					DIST_PARAM2 + ">[+-]?[0-9]+\\.?[0-9]*))?(,(?<" + 
					DIST_PARAM3 + ">[+-]?[0-9]+\\.?[0-9]*))?(,(?<" + 
					DIST_PARAM4 + ">[+-]?[0-9]+\\.?[0-9]*))?\\))$";

	private static final Pattern PATTERN = Pattern.compile(REG_EXP);
	
	@ParameterizedTest
	@MethodSource("provideStringsForTest")
	public void testExpressions(String testText, String expectedDistName, String expectedParam1, String expectedParam2, String expectedParam3, String expectedParam4, String expectedScale, String expectedScaleSign, String expectedOffset) {
		String valueNormalized = testText.replace(" ", "");
		Matcher matcher = PATTERN.matcher(valueNormalized);
		assertTrue(matcher.find());
		assertEquals(matcher.group(DIST_NAME), expectedDistName);
		assertEquals(matcher.group(DIST_PARAM1), expectedParam1);
		assertEquals(matcher.group(DIST_PARAM2), expectedParam2);
		assertEquals(matcher.group(DIST_PARAM3), expectedParam3);
		assertEquals(matcher.group(DIST_PARAM4), expectedParam4);
		assertEquals(matcher.group(DIST_SCALE), expectedScale);
		assertEquals(matcher.group(DIST_SCALE_SIGN), expectedScaleSign);
		assertEquals(matcher.group(DIST_OFFSET), expectedOffset);
	}

	static Stream<Arguments> provideStringsForTest() {
		return Stream.of(
				Arguments.of("Normal(0.5, 1.23)", "Normal", "0.5", "1.23", null, null, null, null, null),
				Arguments.of("3-2*Beta(2, 56)", "Beta", "2", "56", null, null, "2", "-", "3"),
				Arguments.of("-2*Beta(2, 56)", "Beta", "2", "56", null, null, "2", "-", null),
				Arguments.of("3+Beta(2, 56)", "Beta", "2", "56", null, null, null, "+", "3"),
				Arguments.of("-Beta(2, 56)", "Beta", "2", "56", null, null, null, "-", null),
				Arguments.of("InventParam(2, 56, 3, 48.0)", "InventParam", "2", "56", "3", "48.0", null, null, null)
		);
	}
}


