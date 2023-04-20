package net.easecation.eccommons.ecparsec.text;

import java.math.BigDecimal;
import java.math.BigInteger;
import net.easecation.eccommons.adt.Tuple;
import net.easecation.eccommons.ecparsec.Combinator;
import net.easecation.eccommons.ecparsec.Parser;

public final class TextReadParsers {
	public static <U> Parser<Text, U, Boolean> readBoolean() {
		return Parser.label(Combinator.choice(
			TextParsers.<U>string("false").replace(false),
			TextParsers.<U>string("true").replace(true)
		), "expected: boolean");
	}

	public static <U> Parser<Text, U, Byte> readByte() {
		return Parser.label(TextReadParsers.<U>integer(), "expected: byte")
			.flatMap(number -> Parser.<Text, U>ensure(
				number.compareTo(BigInteger.valueOf(Byte.MIN_VALUE)) >= 0 &&
				number.compareTo(BigInteger.valueOf(Byte.MAX_VALUE)) <= 0,
				"Literal out of range")
			.replace(number::byteValueExact));
	}

	public static <U> Parser<Text, U, Short> readShort() {
		return Parser.label(TextReadParsers.<U>integer(), "expected: short")
			.flatMap(number -> Parser.<Text, U>ensure(
				number.compareTo(BigInteger.valueOf(Short.MIN_VALUE)) >= 0 &&
				number.compareTo(BigInteger.valueOf(Short.MAX_VALUE)) <= 0,
				"Literal out of range")
			.replace(number::shortValueExact));
	}

	public static <U> Parser<Text, U, Integer> readInteger() {
		return Parser.label(TextReadParsers.<U>integer(), "expected: integer")
			.flatMap(number -> Parser.<Text, U>ensure(
				number.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) >= 0 &&
				number.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0,
				"Literal out of range")
			.replace(number::intValueExact));
	}

	public static <U> Parser<Text, U, Long> readLong() {
		return Parser.label(TextReadParsers.<U>integer(), "expected: long")
			.flatMap(number -> Parser.<Text, U>ensure(
				number.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) >= 0 &&
				number.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0,
				"Literal out of range")
			.replace(number::longValueExact));
	}

	public static <U> Parser<Text, U, Float> readFloat() {
		return Parser.label(Combinator.choice(
			TextParsers.<U>string("NaN").replace(Float.NaN),
			TextParsers.<U>string("Infinity").replace(Float.POSITIVE_INFINITY),
			TextParsers.<U>string("-Infinity").replace(Float.NEGATIVE_INFINITY),
			TextReadParsers.<U>sign()
				.flatMap(sign -> TextReadParsers.<U>fraction()
				.map(fraction -> sign ? -fraction.floatValue() : fraction.floatValue()))
		), "expected: float");
	}

	public static <U> Parser<Text, U, Double> readDouble() {
		return Parser.label(Combinator.choice(
			TextParsers.<U>string("NaN").replace(Double.NaN),
			TextParsers.<U>string("Infinity").replace(Double.POSITIVE_INFINITY),
			TextParsers.<U>string("-Infinity").replace(Double.NEGATIVE_INFINITY),
			TextReadParsers.<U>sign()
				.flatMap(sign -> TextReadParsers.<U>fraction()
				.map(fraction -> sign ? -fraction.doubleValue() : fraction.doubleValue()))
		), "expected: double");
	}

	public static <U> Parser<Text, U, Character> readCharacter() {
		return Parser.label(Combinator.between(
			TextParsers.character('\''),
			TextParsers.character('\''),
			Combinator.choice(TextParsers.satisfy(c -> c != '\'' && c != '\\'), escape())
		), "expected: character");
	}

	public static <U> Parser<Text, U, String> readString() {
		return Parser.label(Combinator.between(
			TextParsers.character('\"'),
			TextParsers.character('\"'),
			Combinator.some(Combinator.choice(
				TextParsers.<U>satisfy(c -> c != '\"' && c != '\\'), escape()
			)).map(characters -> characters.foldl(StringBuilder::append, new StringBuilder()).toString())
		), "expected: string");
	}

	static <U> Parser<Text, U, Boolean> sign() {
		return Combinator.choice(
			TextParsers.<U>character('-').replace(true),
			TextParsers.<U>character('+').replace(false),
			Parser.of(false)
		);
	}

	static <U> Parser<Text, U, Tuple<Integer, BigInteger>> digits(BigInteger base, Parser<Text, U, BigInteger> digitP) {
		return Combinator.many(digitP).map(digits -> digits.foldl(
			(t, d) -> Tuple.of(t.getFirst() + 1, t.getSecond().multiply(base).add(d)),
			Tuple.of(0, BigInteger.ZERO)
		));
	}

	static <U> Parser<Text, U, BigInteger> integer() {
		return TextReadParsers.<U>sign()
			.flatMap(sign -> TextReadParsers.digits(BigInteger.TEN,
				TextParsers.<U>satisfy(c -> c >= '0' && c <= '9')
				.map(c -> BigInteger.valueOf(c - '0')))
			.map(integer -> sign ? integer.getSecond().negate() : integer.getSecond()));
	}

	static <U> Parser<Text, U, BigDecimal> fraction() {
		return digits(BigInteger.TEN,
				TextParsers.<U>satisfy(c -> c >= '0' && c <= '9')
				.map(c -> BigInteger.valueOf(c - '0')))
			.flatMap(integer -> Combinator.choice(
				TextParsers.<U>character('.').andThen(TextReadParsers.digits(BigInteger.TEN,
					TextParsers.<U>satisfy(c -> c >= '0' && c <= '9')
					.map(c -> BigInteger.valueOf(c - '0')))),
				Parser.of(Tuple.of(0, BigInteger.ZERO)))
			.map(fraction -> new BigDecimal(integer.getSecond()
				.multiply(BigInteger.TEN.pow(fraction.getFirst()))
				.add(fraction.getSecond()), fraction.getFirst())));
	}

	static <U> Parser<Text, U, Character> escape() {
		return TextParsers.<U>character('\\').andThen(Combinator.choice(
			TextParsers.<U>character('b').replace('\b'),
			TextParsers.<U>character('t').replace('\t'),
			TextParsers.<U>character('n').replace('\n'),
			TextParsers.<U>character('f').replace('\f'),
			TextParsers.<U>character('r').replace('\r'),
			TextParsers.<U>character('\"').replace('\"'),
			TextParsers.<U>character('\'').replace('\''),
			TextParsers.<U>character('\\').replace('\\')
		));
	}
}
