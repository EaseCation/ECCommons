package net.easecation.eccommons.ecparsec;

import net.easecation.eccommons.adt.Maybe;
import net.easecation.eccommons.ecparsec.nexpr.NExprAssociativity;
import net.easecation.eccommons.ecparsec.nexpr.NExprDefinition;
import net.easecation.eccommons.ecparsec.nexpr.NExprLevel;
import net.easecation.eccommons.ecparsec.nexpr.NExprOperand;
import net.easecation.eccommons.ecparsec.nexpr.NExprParsers;
import net.easecation.eccommons.ecparsec.text.Text;
import net.easecation.eccommons.ecparsec.text.TextParsers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class NExprParsersTest {
	@Test
	public void testBasic() {
		assertEquals(Maybe.ofNothing(), Parser.runParser(NExprParsers.exprP(NExprDefinition.of(
			Parser.none(),
			Parser.none(),
			Parser.none(),
			n -> Parser.none(),
			Parser.none()
		)), Text.text("")).ofMaybe());
		assertEquals(Maybe.ofNothing(), Parser.runParser(NExprParsers.exprP(NExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			TextParsers::string,
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			NExprLevel.of(NExprAssociativity.NONE,
				NExprOperand.node("+", t1 -> NExprOperand.tail(t2 ->
					"(" + t1 + " + " + t2 + ")"
				))
			)
		)), Text.text("")).ofMaybe());
		assertEquals(Maybe.ofNothing(), Parser.runParser(NExprParsers.exprP(NExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			TextParsers::string,
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			NExprLevel.of(NExprAssociativity.NONE,
				NExprOperand.node("+", t1 -> NExprOperand.tail(t2 ->
					"(" + t1 + " + " + t2 + ")"
				))
			)
		)), Text.text("+")).ofMaybe());
		assertEquals(Maybe.ofNothing(), Parser.runParser(NExprParsers.exprP(NExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			TextParsers::string,
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			NExprLevel.of(NExprAssociativity.NONE,
				NExprOperand.node("+", t1 -> NExprOperand.tail(t2 ->
					"(" + t1 + " + " + t2 + ")"
				))
			)
		)), Text.text("a +")).ofMaybe());
		assertEquals(Maybe.ofNothing(), Parser.runParser(NExprParsers.exprP(NExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			TextParsers::string,
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			NExprLevel.of(NExprAssociativity.NONE,
				NExprOperand.node("+", t1 -> NExprOperand.tail(t2 ->
					"(" + t1 + " + " + t2 + ")"
				))
			)
		)), Text.text("+ a")).ofMaybe());
		assertEquals(Maybe.ofJust("a"), Parser.runParser(NExprParsers.exprP(NExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			TextParsers::string,
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			NExprLevel.of(NExprAssociativity.NONE,
				NExprOperand.node("+", t1 -> NExprOperand.tail(t2 ->
					"(" + t1 + " + " + t2 + ")"
				))
			)
		)), Text.text("a")).ofMaybe());
		assertEquals(Maybe.ofJust("(a + b)"), Parser.runParser(NExprParsers.exprP(NExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			TextParsers::string,
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			NExprLevel.of(NExprAssociativity.NONE,
				NExprOperand.node("+", t1 -> NExprOperand.tail(t2 ->
					"(" + t1 + " + " + t2 + ")"
				))
			)
		)), Text.text("a + b")).ofMaybe());
	}

	@Test
	public void testTernary() {
		assertEquals(Maybe.ofJust("(a ? b : c)"), Parser.runParser(NExprParsers.exprP(NExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			TextParsers::string,
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			NExprLevel.of(NExprAssociativity.NONE,
				NExprOperand.node("?", t1 -> NExprOperand.node(":", t2 -> NExprOperand.tail(t3 ->
					"(" + t1 + " ? " + t2 + " : " + t3 + ")"
				)))
			)
		)), Text.text("a ? b : c")).ofMaybe());
	}

	@Test
	public void testBracket() {
		assertEquals(Maybe.ofJust("(a + ((b + c) + d))"), Parser.runParser(NExprParsers.exprP(NExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			TextParsers::string,
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			NExprLevel.of(NExprAssociativity.NONE,
				NExprOperand.node("+", t1 -> NExprOperand.tail(t2 ->
					"(" + t1 + " + " + t2 + ")"
				))
			)
		)), Text.text("(a + ((b + c) + d))")).ofMaybe());
	}

	@Test
	public void testLevel() {
		assertEquals(Maybe.ofJust("(a + (b * c))"), Parser.runParser(NExprParsers.exprP(NExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			TextParsers::string,
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			NExprLevel.of(NExprAssociativity.NONE,
				NExprOperand.node("+", t1 -> NExprOperand.tail(t2 ->
					"(" + t1 + " + " + t2 + ")"
				))
			),
			NExprLevel.of(NExprAssociativity.NONE,
				NExprOperand.node("*", t1 -> NExprOperand.tail(t2 ->
					"(" + t1 + " * " + t2 + ")"
				))
			)
		)), Text.text("a + b * c")).ofMaybe());
		assertEquals(Maybe.ofJust("((a * b) + c)"), Parser.runParser(NExprParsers.exprP(NExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			TextParsers::string,
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			NExprLevel.of(NExprAssociativity.NONE,
				NExprOperand.node("+", t1 -> NExprOperand.tail(t2 ->
					"(" + t1 + " + " + t2 + ")"
				))
			),
			NExprLevel.of(NExprAssociativity.NONE,
				NExprOperand.node("*", t1 -> NExprOperand.tail(t2 ->
					"(" + t1 + " * " + t2 + ")"
				))
			)
		)), Text.text("a * b + c")).ofMaybe());
	}

	@Test
	public void testAssociativity() {
		assertEquals(Maybe.ofJust("((a + b) + c)"), Parser.runParser(NExprParsers.exprP(NExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			TextParsers::string,
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			NExprLevel.of(NExprAssociativity.LEFT,
				NExprOperand.node("+", t1 -> NExprOperand.tail(t2 ->
					"(" + t1 + " + " + t2 + ")"
				))
			))
		), Text.text("a + b + c")).ofMaybe());
		assertEquals(Maybe.ofJust("(a + (b + c))"), Parser.runParser(NExprParsers.exprP(NExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			TextParsers::string,
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			NExprLevel.of(NExprAssociativity.RIGHT,
				NExprOperand.node("+", t1 -> NExprOperand.tail(t2 ->
					"(" + t1 + " + " + t2 + ")"
				))
			)
		)), Text.text("a + b + c")).ofMaybe());
		assertEquals(Maybe.ofJust("((a ? b : c) ? d : e)"), Parser.runParser(NExprParsers.exprP(NExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			TextParsers::string,
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			NExprLevel.of(NExprAssociativity.LEFT,
				NExprOperand.node("?", t1 -> NExprOperand.node(":", t2 -> NExprOperand.tail(t3 ->
					"(" + t1 + " ? " + t2 + " : " + t3 + ")"
				)))
			)
		)), Text.text("a ? b : c ? d : e")).ofMaybe());
		assertEquals(Maybe.ofJust("(a ? b : (c ? d : e))"), Parser.runParser(NExprParsers.exprP(NExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			TextParsers::string,
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			NExprLevel.of(NExprAssociativity.RIGHT,
				NExprOperand.node("?", t1 -> NExprOperand.node(":", t2 -> NExprOperand.tail(t3 ->
					"(" + t1 + " ? " + t2 + " : " + t3 + ")"
				)))
			)
		)), Text.text("a ? b : c ? d : e")).ofMaybe());
	}

	@Test
	public void testOperands() {
		assertEquals(Maybe.ofJust("(a ~ b @ c)"), Parser.runParser(NExprParsers.exprP(NExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			TextParsers::string,
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			NExprLevel.of(NExprAssociativity.NONE,
				NExprOperand.node("~", t1 -> NExprOperand.node("@", t2 -> NExprOperand.tail(t3 ->
					"(" + t1 + " ~ " + t2 + " @ " + t3 + ")"
				))),
				NExprOperand.node("~", t1 -> NExprOperand.node("#", t2 -> NExprOperand.tail(t3 ->
					"(" + t1 + " ~ " + t2 + " # " + t3 + ")"
				)))
			)
		)), Text.text("a ~ b @ c")).ofMaybe());
		assertEquals(Maybe.ofJust("(a ~ b # c)"), Parser.runParser(NExprParsers.exprP(NExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			TextParsers::string,
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			NExprLevel.of(NExprAssociativity.NONE,
				NExprOperand.node("~", t1 -> NExprOperand.node("@", t2 -> NExprOperand.tail(t3 ->
					"(" + t1 + " ~ " + t2 + " @ " + t3 + ")"
				))),
				NExprOperand.node("~", t1 -> NExprOperand.node("#", t2 -> NExprOperand.tail(t3 ->
					"(" + t1 + " ~ " + t2 + " # " + t3 + ")"
				)))
			)
		)), Text.text("a ~ b # c")).ofMaybe());
		assertEquals(Maybe.ofJust("(((a + b) - c) + d)"), Parser.runParser(NExprParsers.exprP(NExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			TextParsers::string,
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			NExprLevel.of(NExprAssociativity.LEFT,
				NExprOperand.node("+", t1 -> NExprOperand.tail(t2 ->
					"(" + t1 + " + " + t2 + ")"
				)),
				NExprOperand.node("-", t1 -> NExprOperand.tail(t2 ->
					"(" + t1 + " - " + t2 + ")"
				))
			)
		)), Text.text("a + b - c + d")).ofMaybe());
	}

	@Test
	public void testComplexExpr() {
		assertEquals(Maybe.ofJust("(((a + (b * c)) - ((d / e) * f)) - g)"), Parser.runParser(NExprParsers.exprP(NExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			TextParsers::string,
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			NExprLevel.of(NExprAssociativity.LEFT,
				NExprOperand.node("+", t1 -> NExprOperand.tail(t2 ->
					"(" + t1 + " + " + t2 + ")"
				)),
				NExprOperand.node("-", t1 -> NExprOperand.tail(t2 ->
					"(" + t1 + " - " + t2 + ")"
				))
			),
			NExprLevel.of(NExprAssociativity.LEFT,
				NExprOperand.node("*", t1 -> NExprOperand.tail(t2 ->
					"(" + t1 + " * " + t2 + ")"
				)),
				NExprOperand.node("/", t1 -> NExprOperand.tail(t2 ->
					"(" + t1 + " / " + t2 + ")"
				))
			)
		)), Text.text("a + b * c - d / e * f - g")).ofMaybe());
	}
}
