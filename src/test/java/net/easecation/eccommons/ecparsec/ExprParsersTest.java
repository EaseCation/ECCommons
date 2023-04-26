package net.easecation.eccommons.ecparsec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.easecation.eccommons.adt.Maybe;
import net.easecation.eccommons.adt.Tuple;
import net.easecation.eccommons.adt.Unit;
import net.easecation.eccommons.ecparsec.expr.ExprDefinition;
import net.easecation.eccommons.ecparsec.expr.ExprLevel;
import net.easecation.eccommons.ecparsec.expr.ExprOperand;
import net.easecation.eccommons.ecparsec.expr.ExprParsers;
import net.easecation.eccommons.ecparsec.text.Text;
import net.easecation.eccommons.ecparsec.text.TextParsers;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class ExprParsersTest {
	@Test
	public void testBasic() {
		assertEquals(Maybe.ofNothing(), Parser.runParser(ExprParsers.exprP(ExprDefinition.of(
			Parser.none(),
			Parser.none(),
			Parser.none(),
			Parser.none()
		)), Text.text("")).ofMaybe());
		assertEquals(Maybe.ofNothing(), Parser.runParser(ExprParsers.exprP(ExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			ExprLevel.of(
				ExprOperand.infixNR(TextParsers.<Unit>string("+").replace((t1, t2) ->
					"(" + t1 + " + " + t2 + ")"
				))
			)
		)), Text.text("")).ofMaybe());
		assertEquals(Maybe.ofNothing(), Parser.runParser(ExprParsers.exprP(ExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			ExprLevel.of(
				ExprOperand.infixNR(TextParsers.<Unit>string("+").replace((t1, t2) ->
					"(" + t1 + " + " + t2 + ")"
				))
			)
		)), Text.text("+")).ofMaybe());
		assertEquals(Maybe.ofNothing(), Parser.runParser(ExprParsers.exprP(ExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			ExprLevel.of(
				ExprOperand.infixNR(TextParsers.<Unit>string("+").replace((t1, t2) ->
					"(" + t1 + " + " + t2 + ")"
				))
			)
		)), Text.text("a +")).ofMaybe());
		assertEquals(Maybe.ofNothing(), Parser.runParser(ExprParsers.exprP(ExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			ExprLevel.of(
				ExprOperand.infixNR(TextParsers.<Unit>string("+").replace((t1, t2) ->
					"(" + t1 + " + " + t2 + ")"
				))
			)
		)), Text.text("+ a")).ofMaybe());
		assertEquals(Maybe.ofJust("a"), Parser.runParser(ExprParsers.exprP(ExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			ExprLevel.of(
				ExprOperand.infixNR(TextParsers.<Unit>string("+").replace((t1, t2) ->
					"(" + t1 + " + " + t2 + ")"
				))
			)
		)), Text.text("a")).ofMaybe());
		assertEquals(Maybe.ofJust("(a + b)"), Parser.runParser(ExprParsers.exprP(ExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			ExprLevel.of(
				ExprOperand.infixNR(TextParsers.<Unit>string("+").replace((t1, t2) ->
					"(" + t1 + " + " + t2 + ")"
				))
			)
		)), Text.text("a + b")).ofMaybe());
	}

	@Test
	public void testTernary() {
		assertEquals(Maybe.ofJust("(a ? b : c)"), Parser.runParser(ExprParsers.exprP(ExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			ExprLevel.of(
				ExprOperand.infixNL(TextParsers.<Unit>string("?").replace(t1 ->
				ExprOperand.infixNR(TextParsers.<Unit>string(":").replace((t2, t3) ->
					"(" + t1 + " ? " + t2 + " : " + t3 + ")"
				))))
			)
		)), Text.text("a ? b : c")).ofMaybe());
	}

	@Test
	public void testBracket() {
		assertEquals(Maybe.ofJust("(a + ((b + c) + d))"), Parser.runParser(ExprParsers.exprP(ExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			ExprLevel.of(
				ExprOperand.infixNR(TextParsers.<Unit>string("+").replace((t1, t2) ->
					"(" + t1 + " + " + t2 + ")"
				))
			)
		)), Text.text("(a + ((b + c) + d))")).ofMaybe());
	}

	@Test
	public void testLevel() {
		assertEquals(Maybe.ofJust("(a + (b * c))"), Parser.runParser(ExprParsers.exprP(ExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			ExprLevel.of(
				ExprOperand.infixNR(TextParsers.<Unit>string("*").replace((t1, t2) ->
					"(" + t1 + " * " + t2 + ")"
				))
			),
			ExprLevel.of(
				ExprOperand.infixNR(TextParsers.<Unit>string("+").replace((t1, t2) ->
					"(" + t1 + " + " + t2 + ")"
				))
			)
		)), Text.text("a + b * c")).ofMaybe());
		assertEquals(Maybe.ofJust("((a * b) + c)"), Parser.runParser(ExprParsers.exprP(ExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			ExprLevel.of(
				ExprOperand.infixNR(TextParsers.<Unit>string("*").replace((t1, t2) ->
					"(" + t1 + " * " + t2 + ")"
				))
			),
			ExprLevel.of(
				ExprOperand.infixNR(TextParsers.<Unit>string("+").replace((t1, t2) ->
					"(" + t1 + " + " + t2 + ")"
				))
			)
		)), Text.text("a * b + c")).ofMaybe());
	}

	@Test
	public void testAssociativity() {
		assertEquals(Maybe.ofJust("((a + b) + c)"), Parser.runParser(ExprParsers.exprP(ExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			ExprLevel.of(
				ExprOperand.infixLR(TextParsers.<Unit>string("+").replace((t1, t2) ->
					"(" + t1 + " + " + t2 + ")"
				))
			))
		), Text.text("a + b + c")).ofMaybe());
		assertEquals(Maybe.ofJust("(a + (b + c))"), Parser.runParser(ExprParsers.exprP(ExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			ExprLevel.of(
				ExprOperand.infixRR(TextParsers.<Unit>string("+").replace((t1, t2) ->
					"(" + t1 + " + " + t2 + ")"
				))
			)
		)), Text.text("a + b + c")).ofMaybe());
		assertEquals(Maybe.ofJust("((a ? b : c) ? d : e)"), Parser.runParser(ExprParsers.exprP(ExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			ExprLevel.of(
				ExprOperand.infixLL(TextParsers.<Unit>string("?").replace(t1 ->
				ExprOperand.infixLR(TextParsers.<Unit>string(":").replace((t2, t3) ->
					"(" + t1 + " ? " + t2 + " : " + t3 + ")"
				))))
			)
		)), Text.text("a ? b : c ? d : e")).ofMaybe());
		assertEquals(Maybe.ofJust("(a ? b : (c ? d : e))"), Parser.runParser(ExprParsers.exprP(ExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			ExprLevel.of(
				ExprOperand.infixRL(TextParsers.<Unit>string("?").replace(t1 ->
				ExprOperand.infixRR(TextParsers.<Unit>string(":").replace((t2, t3) ->
					"(" + t1 + " ? " + t2 + " : " + t3 + ")"
				))))
			)
		)), Text.text("a ? b : c ? d : e")).ofMaybe());
	}

	@Test
	public void testOperands() {
		assertEquals(Maybe.ofJust("(a ~ b @ c)"), Parser.runParser(ExprParsers.exprP(ExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			ExprLevel.of(
				ExprOperand.infixNL(TextParsers.<Unit>string("~").replace(t1 ->
				ExprOperand.infixNR(Combinator.choice(
					TextParsers.<Unit>string("@").replace((t2, t3) ->
						"(" + t1 + " ~ " + t2 + " @ " + t3 + ")"
					),
					TextParsers.<Unit>string("#").replace((t2, t3) ->
						"(" + t1 + " ~ " + t2 + " # " + t3 + ")"
					)
				))))
			)
		)), Text.text("a ~ b @ c")).ofMaybe());
		assertEquals(Maybe.ofJust("(a ~ b # c)"), Parser.runParser(ExprParsers.exprP(ExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			ExprLevel.of(
				ExprOperand.infixNL(TextParsers.<Unit>string("~").replace(t1 ->
				ExprOperand.infixNR(Combinator.choice(
					TextParsers.<Unit>string("@").replace((t2, t3) ->
						"(" + t1 + " ~ " + t2 + " @ " + t3 + ")"
					),
					TextParsers.<Unit>string("#").replace((t2, t3) ->
						"(" + t1 + " ~ " + t2 + " # " + t3 + ")"
					)
				))))
			)
		)), Text.text("a ~ b # c")).ofMaybe());
		assertEquals(Maybe.ofJust("(((a + b) - c) + d)"), Parser.runParser(ExprParsers.exprP(ExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			ExprLevel.of(
				ExprOperand.infixLR(TextParsers.<Unit>string("+").replace((t1, t2) ->
					"(" + t1 + " + " + t2 + ")"
				)),
				ExprOperand.infixLR(TextParsers.<Unit>string("-").replace((t1, t2) ->
					"(" + t1 + " - " + t2 + ")"
				))
			)
		)), Text.text("a + b - c + d")).ofMaybe());
	}

	@Test
	public void testComplexExpr() {
		assertEquals(Maybe.ofJust("(((a + (b * c)) - ((d / e) * f)) - g)"), Parser.runParser(ExprParsers.exprP(ExprDefinition.of(
			TextParsers.spaces(),
			TextParsers.character('('),
			TextParsers.character(')'),
			Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
			ExprLevel.of(
				ExprOperand.infixLR(TextParsers.<Unit>string("*").replace((t1, t2) ->
					"(" + t1 + " * " + t2 + ")"
				)),
				ExprOperand.infixLR(TextParsers.<Unit>string("/").replace((t1, t2) ->
					"(" + t1 + " / " + t2 + ")"
				))
			),
			ExprLevel.of(
				ExprOperand.infixLR(TextParsers.<Unit>string("+").replace((t1, t2) ->
					"(" + t1 + " + " + t2 + ")"
				)),
				ExprOperand.infixLR(TextParsers.<Unit>string("-").replace((t1, t2) ->
					"(" + t1 + " - " + t2 + ")"
				))
			)
		)), Text.text("a + b * c - d / e * f - g")).ofMaybe());
	}

	@Test
	public void testRandomExpr() {
		List<Tuple<String, String>> testsuiteList = Arrays.asList(
			Tuple.of(
				"a + b * c > d / e ? f - g : h + i",
				"(((a + (b * c)) > (d / e)) ? (f - g) : (h + i))"
			),
			Tuple.of(
				"!a && b || c == d + e * f - g / h",
				"((!a && b) || (c == ((d + (e * f)) - (g / h))))"
			),
			Tuple.of(
				"a * b + c / d - e < f * g - h + i",
				"((((a * b) + (c / d)) - e) < (((f * g) - h) + i))"
			),
			Tuple.of(
				"a / b - c * d != e + f / g * h - i",
				"(((a / b) - (c * d)) != ((e + ((f / g) * h)) - i))"
			),
			Tuple.of(
				"a + b > c - d ? e * f : g / h + i",
				"(((a + b) > (c - d)) ? (e * f) : ((g / h) + i))"
			),
			Tuple.of(
				"a - b * c < d + e / f ? g * h : i - j",
				"(((a - (b * c)) < (d + (e / f))) ? (g * h) : (i - j))"
			),
			Tuple.of(
				"a * b / c + d >= e - f * g + h",
				"((((a * b) / c) + d) >= ((e - (f * g)) + h))"
			),
			Tuple.of(
				"a / b + c - d <= e * f / g - h",
				"((((a / b) + c) - d) <= (((e * f) / g) - h))"
			),
			Tuple.of(
				"a && b || c > d + e - f * g",
				"((a && b) || (c > ((d + e) - (f * g))))"
			),
			Tuple.of(
				"a || b && c < d - e + f / g",
				"(a || (b && (c < ((d - e) + (f / g)))))"
			),
			Tuple.of(
				"a - b / c != d * e + f ? g - h : i + j",
				"(((a - (b / c)) != ((d * e) + f)) ? (g - h) : (i + j))"
			),
			Tuple.of(
				"a + b == c / d ? e - f : g * h + i",
				"(((a + b) == (c / d)) ? (e - f) : ((g * h) + i))"
			),
			Tuple.of(
				"a * b - c >= d / e + f ? g - h : i * j",
				"((((a * b) - c) >= ((d / e) + f)) ? (g - h) : (i * j))"
			),
			Tuple.of(
				"a / b + c <= d * e - f ? g + h : i / j",
				"((((a / b) + c) <= ((d * e) - f)) ? (g + h) : (i / j))"
			),
			Tuple.of(
				"a && b == c || d > e + f * g",
				"((a && (b == c)) || (d > (e + (f * g))))"
			),
			Tuple.of(
				"a || b != c && d < e - f / g",
				"(a || ((b != c) && (d < (e - (f / g)))))"
			),
			Tuple.of(
				"a + b / c > d - e * f ? g + h : i - j",
				"(((a + (b / c)) > (d - (e * f))) ? (g + h) : (i - j))"
			),
			Tuple.of(
				"a - b == c * d ? e / f : g - h + i",
				"(((a - b) == (c * d)) ? (e / f) : ((g - h) + i))"
			),
			Tuple.of(
				"a * b <= c / d ? e + f : g * h - i",
				"(((a * b) <= (c / d)) ? (e + f) : ((g * h) - i))"
			),
			Tuple.of(
				"a / b >= c - d ? e * f : g / h + i",
				"(((a / b) >= (c - d)) ? (e * f) : ((g / h) + i))"
			)
		);
		List<Executable> executables = new ArrayList<>();
		for (Tuple<String, String> testsuite : testsuiteList) {
			String input = testsuite.getFirst();
			String expected = testsuite.getSecond();
			Executable executable = () -> assertEquals(Maybe.ofJust(expected), Parser.runParser(ExprParsers.exprP(ExprDefinition.of(
				TextParsers.spaces(),
				TextParsers.character('('),
				TextParsers.character(')'),
				Parser.advancing(TextParsers.stringSatisfy(c -> c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')),
				ExprLevel.of(
					ExprOperand.prefix(TextParsers.<Unit>string("+").replace(t1 ->
						"+" + t1
					)),
					ExprOperand.prefix(TextParsers.<Unit>string("-").replace(t1 ->
						"-" + t1
					)),
					ExprOperand.prefix(TextParsers.<Unit>string("!").replace(t1 ->
						"!" + t1
					))
				),
				ExprLevel.of(
					ExprOperand.infixLR(TextParsers.<Unit>string("*").replace((t1, t2) ->
						"(" + t1 + " * " + t2 + ")"
					)),
					ExprOperand.infixLR(TextParsers.<Unit>string("/").replace((t1, t2) ->
						"(" + t1 + " / " + t2 + ")"
					))
				),
				ExprLevel.of(
					ExprOperand.infixLR(TextParsers.<Unit>string("+").replace((t1, t2) ->
						"(" + t1 + " + " + t2 + ")"
					)),
					ExprOperand.infixLR(TextParsers.<Unit>string("-").replace((t1, t2) ->
						"(" + t1 + " - " + t2 + ")"
					))
				),
				ExprLevel.of(
					ExprOperand.infixLR(TextParsers.<Unit>string("<=").replace((t1, t2) ->
						"(" + t1 + " <= " + t2 + ")"
					)),
					ExprOperand.infixLR(TextParsers.<Unit>string(">=").replace((t1, t2) ->
						"(" + t1 + " >= " + t2 + ")"
					)),
					ExprOperand.infixLR(TextParsers.<Unit>string("<").replace((t1, t2) ->
						"(" + t1 + " < " + t2 + ")"
					)),
					ExprOperand.infixLR(TextParsers.<Unit>string(">").replace((t1, t2) ->
						"(" + t1 + " > " + t2 + ")"
					))
				),
				ExprLevel.of(
					ExprOperand.infixLR(TextParsers.<Unit>string("==").replace((t1, t2) ->
						"(" + t1 + " == " + t2 + ")"
					)),
					ExprOperand.infixLR(TextParsers.<Unit>string("!=").replace((t1, t2) ->
						"(" + t1 + " != " + t2 + ")"
					))
				),
				ExprLevel.of(
					ExprOperand.infixLR(TextParsers.<Unit>string("&&").replace((t1, t2) ->
						"(" + t1 + " && " + t2 + ")"
					))
				),
				ExprLevel.of(
					ExprOperand.infixLR(TextParsers.<Unit>string("||").replace((t1, t2) ->
						"(" + t1 + " || " + t2 + ")"
					))
				),
				ExprLevel.<Text, Unit, String>of(
					ExprOperand.infixRL(TextParsers.<Unit>string("?").replace(t1 ->
					ExprOperand.infixRR(TextParsers.<Unit>string(":").replace((t2, t3) ->
						"(" + t1 + " ? " + t2 + " : " + t3 + ")"
					))))
				)
			)), Text.text(input)).ofMaybe(), "Input: " + input);
			executables.add(executable);
		}
		assertAll(executables);
	}
}
