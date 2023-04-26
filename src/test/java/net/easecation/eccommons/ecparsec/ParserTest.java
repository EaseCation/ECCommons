package net.easecation.eccommons.ecparsec;

import net.easecation.eccommons.adt.Maybe;
import net.easecation.eccommons.adt.Unit;
import net.easecation.eccommons.ecparsec.text.Text;
import net.easecation.eccommons.ecparsec.text.TextParsers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class ParserTest {
	@Test
	public void testBasic() {
		assertEquals(Maybe.ofJust("foo"), Parser.runParser(Parser.of("foo"), Unit.UNIT).ofMaybe());
		assertEquals(Maybe.ofJust(Unit.UNIT), Parser.runParser(Parser.unit(), Unit.UNIT).ofMaybe());
		assertEquals(Maybe.ofNothing(), Parser.runParser(Parser.none(), Unit.UNIT).ofMaybe());
		assertEquals(Maybe.ofNothing(), Parser.runParser(Parser.panic(), Unit.UNIT).ofMaybe());
	}

	@Test
	public void testEOF() {
		assertEquals(Maybe.ofJust(Unit.UNIT), Parser.runParser(Parser.eof(Text.tokenStream()), Text.emptyText()).ofMaybe());
		assertEquals(Maybe.ofNothing(), Parser.runParser(Parser.eof(Text.tokenStream()), Text.text("foo")).ofMaybe());
	}

	@Test
	public void testText() {
		assertEquals(Maybe.ofJust('a'), Parser.runParser(TextParsers.any(), Text.text("a")).ofMaybe());
		assertEquals(Maybe.ofNothing(), Parser.runParser(TextParsers.any(), Text.emptyText()).ofMaybe());
		assertEquals(Maybe.ofJust('a'), Parser.runParser(TextParsers.character('a'), Text.text("a")).ofMaybe());
		assertEquals(Maybe.ofNothing(), Parser.runParser(TextParsers.character('a'), Text.emptyText()).ofMaybe());
		assertEquals(Maybe.ofNothing(), Parser.runParser(TextParsers.character('a'), Text.text("b")).ofMaybe());
		assertEquals(Maybe.ofJust("foo"), Parser.runParser(TextParsers.string("foo"), Text.text("foo")).ofMaybe());
		assertEquals(Maybe.ofNothing(), Parser.runParser(TextParsers.string("foo"), Text.emptyText()).ofMaybe());
		assertEquals(Maybe.ofNothing(), Parser.runParser(TextParsers.string("foo"), Text.text("bar")).ofMaybe());
		assertEquals(Maybe.ofJust('x'), Parser.runParser(TextParsers.satisfy(c -> c >= 'a' && c <= 'z'), Text.text("xyz")).ofMaybe());
		assertEquals(Maybe.ofNothing(), Parser.runParser(TextParsers.satisfy(c -> c >= 'a' && c <= 'z'), Text.text("XYZ")).ofMaybe());
		assertEquals(Maybe.ofJust("xyz"), Parser.runParser(TextParsers.stringSatisfy(c -> c >= 'a' && c <= 'z'), Text.text("xyz")).ofMaybe());
		assertEquals(Maybe.ofJust(""), Parser.runParser(TextParsers.stringSatisfy(c -> c >= 'a' && c <= 'z'), Text.text("XYZ")).ofMaybe());
	}

	@Test
	public void testComposition() {
		assertEquals(Maybe.ofJust('z'), Parser.runParser(
			TextParsers.<Unit>character('x').andThen(
			TextParsers.character('y')).andThen(
			TextParsers.character('z')), Text.text("xyz")
		).ofMaybe());
		assertEquals(Maybe.ofJust(Unit.UNIT), Parser.runParser(Combinator.sequence(
			TextParsers.character('x'),
			TextParsers.character('y'),
			TextParsers.character('z')
		), Text.text("xyz")).ofMaybe());
		assertEquals(Maybe.ofJust('x'), Parser.runParser(Combinator.choice(
			TextParsers.character('x'),
			TextParsers.character('y'),
			TextParsers.character('z')
		), Text.text("xyz")).ofMaybe());
	}
}
