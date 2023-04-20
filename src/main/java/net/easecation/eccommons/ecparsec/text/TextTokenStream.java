package net.easecation.eccommons.ecparsec.text;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.easecation.eccommons.adt.Either;
import net.easecation.eccommons.adt.Maybe;
import net.easecation.eccommons.adt.Tuple;
import net.easecation.eccommons.ecparsec.Escaper;
import net.easecation.eccommons.ecparsec.Parser;
import net.easecation.eccommons.ecparsec.TokenStream;

public final class TextTokenStream implements TokenStream<Text, Character> {
	public static final TokenStream<Text, Character> INSTANCE = new TextTokenStream();

	@Override
	public String printToken(Character token) {
		return Escaper.escapeCharacter(token);
	}

	@Override
	public String printStream(Text stream) {
		return Escaper.escapeString(stream.toString());
	}

	@Override
	public Parser.Position advanceToken(Parser.Position position, Character firstToken, Text restStream) {
		return position.updateChar(firstToken);
	}

	@Override
	public Parser.Position advanceStream(Parser.Position position, Text headStream, Text restStream) {
		return position.updateString(headStream.toString());
	}

	@Override
	public Maybe<Tuple<Character, Text>> unconsToken(Text stream) {
		return stream.uncons();
	}

	@Override
	public Either<Text, Text> prefixStream(Text stream, Text headStream) {
		return headStream.isPrefixOf(stream) ? Either.ofRight(stream.drop(headStream.length())) : Either.ofLeft(stream.take(headStream.length()));
	}

	@Override
	public Tuple<Text, Text> spanStream(Text stream, Predicate<Character> f) {
		return stream.span(f);
	}

	@Override
	public boolean isEmpty(Text stream) {
		return stream.isEmpty();
	}

	@Override
	public Text fromTokens(List<Character> tokens) {
		StringBuilder builder = new StringBuilder(tokens.size());
		for (Character token : tokens) {
			builder.append(token);
		}
		return Text.text(builder.toString());
	}

	@Override
	public List<Character> toTokens(Text stream) {
		return stream.toString().chars().mapToObj(c -> (char) c).collect(Collectors.toList());
	}
}
