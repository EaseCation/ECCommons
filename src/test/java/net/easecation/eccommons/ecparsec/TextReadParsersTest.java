package net.easecation.eccommons.ecparsec;

import net.easecation.eccommons.adt.Maybe;
import net.easecation.eccommons.ecparsec.text.Text;
import net.easecation.eccommons.ecparsec.text.TextReadParsers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class TextReadParsersTest {
	@Test
	public void testReadBoolean() {
		assertEquals(Maybe.ofNothing(), Parser.runParser(TextReadParsers.readBoolean(), Text.text("")).ofMaybe());
		assertEquals(Maybe.ofNothing(), Parser.runParser(TextReadParsers.readBoolean(), Text.text("bar")).ofMaybe());
		assertEquals(Maybe.ofJust(false), Parser.runParser(TextReadParsers.readBoolean(), Text.text("false")).ofMaybe());
		assertEquals(Maybe.ofJust(true), Parser.runParser(TextReadParsers.readBoolean(), Text.text("true")).ofMaybe());
	}

	@Test
	public void testReadInteger() {
		assertEquals(Maybe.ofNothing(), Parser.runParser(TextReadParsers.readInteger(), Text.text("")).ofMaybe());
		assertEquals(Maybe.ofNothing(), Parser.runParser(TextReadParsers.readInteger(), Text.text("bar")).ofMaybe());
		assertEquals(Maybe.ofNothing(), Parser.runParser(TextReadParsers.readInteger(), Text.text("9999999999999999999")).ofMaybe());
		assertEquals(Maybe.ofJust(1024), Parser.runParser(TextReadParsers.readInteger(), Text.text("1024")).ofMaybe());
		assertEquals(Maybe.ofJust(-1024), Parser.runParser(TextReadParsers.readInteger(), Text.text("-1024")).ofMaybe());
	}

	@Test
	public void testReadDouble() {
		assertEquals(Maybe.ofNothing(), Parser.runParser(TextReadParsers.readDouble(), Text.text("")).ofMaybe());
		assertEquals(Maybe.ofNothing(), Parser.runParser(TextReadParsers.readDouble(), Text.text("bar")).ofMaybe());
		assertEquals(Maybe.ofNothing(), Parser.runParser(TextReadParsers.readDouble(), Text.text("-NaN")).ofMaybe());
		assertEquals(Maybe.ofJust(Double.NaN), Parser.runParser(TextReadParsers.readDouble(), Text.text("NaN")).ofMaybe());
		assertEquals(Maybe.ofJust(Double.POSITIVE_INFINITY), Parser.runParser(TextReadParsers.readDouble(), Text.text("Infinity")).ofMaybe());
		assertEquals(Maybe.ofJust(Double.NEGATIVE_INFINITY), Parser.runParser(TextReadParsers.readDouble(), Text.text("-Infinity")).ofMaybe());
		assertEquals(Maybe.ofJust(0.0D), Parser.runParser(TextReadParsers.readDouble(), Text.text("0.0")).ofMaybe());
		assertEquals(Maybe.ofJust(-0.0D), Parser.runParser(TextReadParsers.readDouble(), Text.text("-0.0")).ofMaybe());
		assertEquals(Maybe.ofJust(1024.0D), Parser.runParser(TextReadParsers.readDouble(), Text.text("1024")).ofMaybe());
		assertEquals(Maybe.ofJust(-1024.0D), Parser.runParser(TextReadParsers.readDouble(), Text.text("-1024")).ofMaybe());
		assertEquals(Maybe.ofJust(-12345.6789D), Parser.runParser(TextReadParsers.readDouble(), Text.text("-12345.6789")).ofMaybe());
	}

	@Test
	public void testReadCharacter() {
		assertEquals(Maybe.ofNothing(), Parser.runParser(TextReadParsers.readCharacter(), Text.text("")).ofMaybe());
		assertEquals(Maybe.ofNothing(), Parser.runParser(TextReadParsers.readCharacter(), Text.text("a")).ofMaybe());
		assertEquals(Maybe.ofNothing(), Parser.runParser(TextReadParsers.readCharacter(), Text.text("\'a")).ofMaybe());
		assertEquals(Maybe.ofNothing(), Parser.runParser(TextReadParsers.readCharacter(), Text.text("a\'")).ofMaybe());
		assertEquals(Maybe.ofNothing(), Parser.runParser(TextReadParsers.readCharacter(), Text.text("\'bar\'")).ofMaybe());
		assertEquals(Maybe.ofJust('a'), Parser.runParser(TextReadParsers.readCharacter(), Text.text("\'a\'")).ofMaybe());
	}

	@Test
	public void testReadString() {
		assertEquals(Maybe.ofNothing(), Parser.runParser(TextReadParsers.readString(), Text.text("")).ofMaybe());
		assertEquals(Maybe.ofNothing(), Parser.runParser(TextReadParsers.readString(), Text.text("bar")).ofMaybe());
		assertEquals(Maybe.ofNothing(), Parser.runParser(TextReadParsers.readString(), Text.text("bar\"")).ofMaybe());
		assertEquals(Maybe.ofNothing(), Parser.runParser(TextReadParsers.readString(), Text.text("\"bar")).ofMaybe());
		assertEquals(Maybe.ofJust(""), Parser.runParser(TextReadParsers.readString(), Text.text("\"\"")).ofMaybe());
		assertEquals(Maybe.ofJust("bar"), Parser.runParser(TextReadParsers.readString(), Text.text("\"bar\"")).ofMaybe());
	}
}
