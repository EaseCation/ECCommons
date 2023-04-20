package net.easecation.eccommons.ecparsec;

import java.util.List;
import java.util.function.Predicate;
import net.easecation.eccommons.adt.Either;
import net.easecation.eccommons.adt.Maybe;
import net.easecation.eccommons.adt.Tuple;

public interface TokenStream<S, T> {
	String printToken(T token);

	String printStream(S stream);

	Parser.Position advanceToken(Parser.Position position, T firstToken, S restStream);

	Parser.Position advanceStream(Parser.Position position, S headStream, S tailStream);

	Maybe<Tuple<T, S>> unconsToken(S stream);

	Either<S, S> prefixStream(S stream, S headStream);

	Tuple<S, S> spanStream(S stream, Predicate<T> f);

	boolean isEmpty(S stream);

	S fromTokens(List<T> tokens);

	List<T> toTokens(S stream);
}
