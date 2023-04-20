package net.easecation.eccommons.ecparsec.text;

import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Predicate;
import net.easecation.eccommons.adt.Maybe;
import net.easecation.eccommons.adt.Unit;
import net.easecation.eccommons.ecparsec.Combinator;
import net.easecation.eccommons.ecparsec.Parser;

public final class TextParsers {
	public static <U> Parser<Text, U, Character> any() { return Parser.token(Text.tokenStream()); }
	public static <U> Parser<Text, U, Character> character(char c) { return Parser.token(Text.tokenStream(), t -> t == c ? Maybe.ofJust(t) : Maybe.ofNothing()); }
	public static <U> Parser<Text, U, String> string(String s) { return Parser.<Text, Character, U> tokens(Text.tokenStream(), Text.text(s)).map(Text::toString); }

	public static <U> Parser<Text, U, Character> satisfy(Predicate<Character> p) { return Parser.token(Text.tokenStream(), c -> p.test(c) ? Maybe.ofJust(c) : Maybe.ofNothing()); }
	public static <U> Parser<Text, U, Character> dissatisfy(Predicate<Character> p) { return satisfy(p.negate()); }
	public static <U> Parser<Text, U, String> stringSatisfy(Predicate<Character> p) { return Parser.<Text, Character, U>tokens(Text.tokenStream(), p).map(Text::toString); }
	public static <U> Parser<Text, U, String> stringDissatisfy(Predicate<Character> p) { return stringSatisfy(p.negate()); }

	public static <U> Parser<Text, U, Character> oneOf(Character... cs) { return satisfy(new HashSet<>(Arrays.asList(cs))::contains); }
	public static <U> Parser<Text, U, Character> noneOf(Character... cs) { return dissatisfy(new HashSet<>(Arrays.asList(cs))::contains); }

	public static <U> Parser<Text, U, Character> space() { return Parser.label(satisfy(Character::isWhitespace), "expected: space"); }
	public static <U> Parser<Text, U, Unit> spaces() { return Combinator.skipSome(space()); }
	public static <U> Parser<Text, U, String> newline() { return Parser.label(Combinator.choice(string("\r\n"), string("\n")), "expected: newline"); }
	public static <U> Parser<Text, U, Character> upper() { return Parser.label(satisfy(Character::isUpperCase), "expected: upper"); }
	public static <U> Parser<Text, U, Character> lower() { return Parser.label(satisfy(Character::isLowerCase), "expected: lower"); }
	public static <U> Parser<Text, U, Character> letter() { return Parser.label(satisfy(Character::isLetter), "expected: letter"); }
	public static <U> Parser<Text, U, Character> digit() { return Parser.label(satisfy(Character::isDigit), "expected: digit"); }
}
