package net.easecation.eccommons.ecparsec;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.With;
import net.easecation.eccommons.adt.Equality;
import net.easecation.eccommons.adt.Maybe;
import net.easecation.eccommons.adt.Unit;
import net.easecation.eccommons.control.*;
import net.easecation.eccommons.hkt.TC;
import net.easecation.eccommons.hkt.TypeConstructor3;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Parser<S, U, A> implements TypeConstructor3<Parser.HKTWitness, S, U, A> {
	@Value
	@With
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class Position implements Comparable<Position> {
		String sourceName;
		int line;
		int column;

		public static Position of(String sourceName, int line, int column) { return new Position(sourceName, line, column); }
		public static Position of(String sourceName) { return of(sourceName, 1, 1); }
		public static Position empty() { return of("<unknown>", 1, 1); }

		public Position updateChar(char c) {
			switch (c) {
			case '\n': return of(sourceName, line + 1, 1);
			case '\t': return of(sourceName, line, column - (column - 1 % 4) + 4);
			default: return of(sourceName, line, column + 1);
			}
		}
		public Position updateString(String s) {
			Position result = this;
			for (int i = 0; i < s.length(); i++)
				result = result.updateChar(s.charAt(i));
			return result;
		}

		@Override
		public int compareTo(Position that) {
			int sourceNameOrd = this.sourceName.compareTo(that.sourceName);
			if (sourceNameOrd != 0) return sourceNameOrd < 0 ? -1 : 1;
			if (this.line != that.line) return (this.line < that.line ? -1 : 1);
			if (this.column != that.column) return (this.column < that.column ? -1 : 1);
			return 0;
		}

		public String prettyPrint() {
			return Escaper.escapeString(sourceName) + " (line " + line + ", column " + column + ")";
		}
	}

	@Value
	@With
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class State<S, U> {
		S stream;
		U userState;
		Position position;

		public static <S, U> State<S, U> of(S stream, U userState, Position position) { return new State<>(stream, userState, position); }
	}

	@Value
	@With
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class ParseError {
		Position position;
		List<String> messages;

		public static ParseError of(Position position, List<String> messages) { return new ParseError(position, messages); }
		public static ParseError of(Position position, String... messages) { return of(position, Arrays.asList(messages)); }
		public static ParseError of(Position position, String message) { return of(position, Collections.singletonList(message)); }
		public static ParseError of(Position position) { return of(position, Collections.emptyList()); }
		public static <S, U> ParseError of(State<S, U> state, List<String> messages) { return of(state.getPosition(), messages); }
		public static <S, U> ParseError of(State<S, U> state, String... messages) { return of(state.getPosition(), messages); }
		public static <S, U> ParseError of(State<S, U> state, String message) { return of(state.getPosition(), message); }
		public static <S, U> ParseError of(State<S, U> state) { return of(state.getPosition()); }

		public boolean isEmpty() { return messages.isEmpty(); }
		public ParseError addMessages(List<String> messages) { return of(position, Stream.concat(this.messages.stream(), messages.stream()).collect(Collectors.toList())); }
		public ParseError addMessages(String... messages) { return addMessages(Arrays.asList(messages)); }

		public ParseError merge(ParseError other) {
			if (messages.isEmpty() && !other.messages.isEmpty()) return other;
			if (!messages.isEmpty() && other.messages.isEmpty()) return this;
			int ord = position.compareTo(other.position);
			if (ord < 0) return other;
			else if (ord > 0) return this;
			else return addMessages(other.messages);
		}

		public String prettyPrint() {
			String pos = position.prettyPrint();
			String msg = messages.stream().distinct().sorted().collect(Collectors.joining("\n"));
			return pos + ":\n" + msg;
		}
	}

	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static abstract class Result<S, U, A> {
		@Value
		@With
		@EqualsAndHashCode(callSuper = false)
		@AllArgsConstructor(access = AccessLevel.PRIVATE)
		public static class Success<S, U, A> extends Result<S, U, A> {
			boolean consumed;
			ParseError error;
			State<S, U> state;
			A result;

			// region Pattern matching
			public interface Case<S, U, A, R> { R caseSuccess(Success<S, U, A> success); }
			@Override public <R> R caseOf(Case<S, U, A, R> caseSuccess, Fail.Case<S, U, A, R> caseFail) { return caseSuccess.caseSuccess(this); }

			@Override public boolean isSuccess() { return true; }
			@Override public boolean isConsumed() { return consumed; }
			@Override public Maybe<A> ofMaybe() { return Maybe.ofJust(result); }
			@Override public Success<S, U, A> coerceSuccess() throws NoSuchElementException { return this; }
			@Override public Fail<S, U, A> coerceFail() throws NoSuchElementException { throw new NoSuchElementException(); }
			// endregion

			// region Monadic Interface
			@Override public <B> Result<S, U, B> map(Function<A, B> f) { return success(consumed, error, state, f.apply(result)); }
			@Override public <B> Result<S, U, B> applyMap(Result<S, U, Function<A, B>> f) { return f.flatMap(this::map); }
			@Override public <B> Result<S, U, B> flatMap(Function<A, Result<S, U, B>> f) { return f.apply(result).orConsumed(consumed); }
			// endregion
		}

		@Value
		@With
		@EqualsAndHashCode(callSuper = false)
		@AllArgsConstructor(access = AccessLevel.PRIVATE)
		public static class Fail<S, U, A> extends Result<S, U, A> {
			boolean consumed;
			ParseError error;

			// region Pattern matching
			public interface Case<S, U, A, R> { R caseFail(Fail<S, U, A> fail); }
			@Override public <R> R caseOf(Success.Case<S, U, A, R> caseSuccess, Fail.Case<S, U, A, R> caseFail) { return caseFail.caseFail(this); }

			@Override public boolean isSuccess() { return false; }
			@Override public boolean isConsumed() { return consumed; }
			@Override public Maybe<A> ofMaybe() { return Maybe.ofNothing(); }
			@Override public Success<S, U, A> coerceSuccess() throws NoSuchElementException { throw new NoSuchElementException(); }
			@Override public Fail<S, U, A> coerceFail() throws NoSuchElementException { return this; }
			// endregion

			// region Monadic Interface
			@Override public <B> Result<S, U, B> map(Function<A, B> f) { return fail(consumed, error); }
			@Override public <B> Result<S, U, B> applyMap(Result<S, U, Function<A, B>> f) { return f.flatMap(this::map); }
			@Override public <B> Result<S, U, B> flatMap(Function<A, Result<S, U, B>> f) { return fail(consumed, error); }
			// endregion
		}

		public static <S, U, A> Result<S, U, A> success(boolean consumed, ParseError error, State<S, U> state, A result) { return new Success<>(consumed, error, state, result); }
		public static <S, U, A> Result<S, U, A> fail(boolean consumed, ParseError error) { return new Fail<>(consumed, error); }

		// region Boilerplate
		public Result<S, U, A> orConsumed(boolean consumed) { return withConsumed(isConsumed() || consumed); }
		public Result<S, U, A> mergeError(ParseError error) { return withError(getError().merge(error)); }
		public Result<S, U, A> addError(String... messages) { return withError(getError().addMessages(messages)); }
		public Result<S, U, A> replaceError(String... messages) { return withError(getError().withMessages(Arrays.asList(messages))); }
		// endregion

		// region Pattern matching
		public interface Match<S, U, A, R> extends Success.Case<S, U, A, R>, Fail.Case<S, U, A, R> {}
		public final <R> R match(Match<S, U, A, R> match) { return caseOf(match, match); }
		public abstract <R> R caseOf(Success.Case<S, U, A, R> caseSuccess, Fail.Case<S, U, A, R> caseFail);

		public abstract boolean isSuccess();
		public abstract boolean isConsumed();
		public abstract Result<S, U, A> withConsumed(boolean consumed);
		public abstract ParseError getError();
		public abstract Result<S, U, A> withError(ParseError error);
		public abstract Maybe<A> ofMaybe();
		public abstract Success<S, U, A> coerceSuccess() throws NoSuchElementException;
		public abstract Fail<S, U, A> coerceFail() throws NoSuchElementException;
		// endregion

	    // region Monadic Interface
		public abstract <B> Result<S, U, B> map(Function<A, B> f);
		public abstract <B> Result<S, U, B> applyMap(Result<S, U, Function<A, B>> f);
		public abstract <B> Result<S, U, B> flatMap(Function<A, Result<S, U, B>> f);
		// endregion
	}

	@FunctionalInterface
	public interface ParseStep<S, U, A> {
		Trampoline<Result<S, U, A>> parse(State<S, U> state);
	}

	ParseStep<S, U, A> step;

	public Trampoline<Result<S, U, A>> parse(State<S, U> state) { return getStep().parse(state); }

	public static <S, U, A> Parser<S, U, A> of(ParseStep<S, U, A> step) { return new Parser<>(step); }
	public static <S, U, A> Parser<S, U, A> of(A a) { return of(s -> Trampoline.done(Result.success(false, ParseError.of(s), s, a))); }
	public static <S, U, A> Parser<S, U, A> none() { return of(s -> Trampoline.done(Result.fail(false, ParseError.of(s)))); }
	public static <S, U> Parser<S, U, Unit> unit() { return of(Unit.UNIT); }
	public static <S, U, A> Parser<S, U, A> panic(String... messages) { return of(s -> Trampoline.done(Result.fail(false, ParseError.of(s, Arrays.asList(messages))))); }
	public static <S, U, A> Parser<S, U, A> recur(Supplier<Parser<S, U, A>> p) { return of(s -> Trampoline.more(() -> p.get().parse(s))); }

	// region Evaluation
	public static <S, U, A> Result<S, U, A> runParser(Parser<S, U, A> p, State<S, U> state) { return p.parse(state).run(); }
	public static <S, U, A> Result<S, U, A> runParser(Parser<S, U, A> p, S stream, U userState, Position position) { return runParser(p, State.of(stream, userState, position)); }
	public static <S, U, A> Result<S, U, A> runParser(Parser<S, U, A> p, S stream, U userState, String sourceName) { return runParser(p, stream, userState, Position.of(sourceName)); }
	public static <S, U, A> Result<S, U, A> runParser(Parser<S, U, A> p, S stream, U userState) { return runParser(p, stream, userState, Position.empty()); }
	public static <S, A> Result<S, Unit, A> runParser(Parser<S, Unit, A> p, S stream, String sourceName) { return runParser(p, stream, Unit.UNIT, Position.of(sourceName)); }
	public static <S, A> Result<S, Unit, A> runParser(Parser<S, Unit, A> p, S stream) { return runParser(p, stream, Unit.UNIT, Position.empty()); }
	// endregion

	// region Alternative
	public Parser<S, U, A> or(Parser<S, U, A> p) {
		return of(s -> parse(s).flatMap(r1 -> r1.caseOf(
			Trampoline::done,
			fail1 -> fail1.isConsumed() ? Trampoline.done(fail1) : Trampoline.more(() -> p.parse(s).map(
				r2 -> r2.isConsumed() ? r2 : r2.mergeError(fail1.getError()))
			)
		)));
	}
	public Parser<S, U, A> orElse(A a) { return or(of(a)); }
	// endregion

	// region Monadic Interface
	public <B> Parser<S, U, B> map(Function<A, B> f) {
		return of(s -> parse(s).flatMap(r -> Trampoline.done(r.map(f))));
	}
	public <B> Parser<S, U, B> applyMap(Parser<S, U, Function<A, B>> f) { return f.flatMap(this::map); }
	public <B> Parser<S, U, B> flatMap(Function<A, Parser<S, U, B>> f) {
		return of(s -> parse(s).flatMap(r1 -> r1.caseOf(
			success -> Trampoline.more(() -> f.apply(success.getResult())
				.parse(success.getState())
				.map(r2 -> r2.orConsumed(success.isConsumed()).mergeError(success.getError()))
			),
			fail -> Trampoline.done(Result.fail(fail.isConsumed(), fail.getError()))
		)));
	}
	public <B> Parser<S, U, B> andThen(Parser<S, U, B> p) { return flatMap(a -> p); }
	public <B> Parser<S, U, B> andThen(Supplier<Parser<S, U, B>> f) { return flatMap(a -> f.get()); }
	public <B> Parser<S, U, A> replaceThen(Parser<S, U, B> p) { return flatMap(p::replace); }
	public <B> Parser<S, U, A> replaceThen(Supplier<Parser<S, U, B>> f) { return flatMap(a -> f.get().replace(a)); }
	public <B> Parser<S, U, B> replace(B b) { return map(a -> b); }
	public <B> Parser<S, U, B> replace(Supplier<B> f) { return map(a -> f.get()); }
	public Parser<S, U, Unit> discard() { return map(a -> Unit.UNIT); }
	// endregion

	// region State manipulation
	public static <S, U> Parser<S, U, State<S, U>> getState() { return of(s -> Trampoline.done(Result.success(false, ParseError.of(s), s, s))); }
	public static <S, U> Parser<S, U, S> getStream() { return of(s -> Trampoline.done(Result.success(false, ParseError.of(s), s, s.getStream()))); }
	public static <S, U> Parser<S, U, U> getUserState() { return of(s -> Trampoline.done(Result.success(false, ParseError.of(s), s, s.getUserState()))); }
	public static <S, U> Parser<S, U, Unit> putUserState(U userState) { return of(s -> Trampoline.done(Result.success(false, ParseError.of(s), s.withUserState(userState), Unit.UNIT))); }
	public static <S, U> Parser<S, U, Position> getPosition() { return of(s -> Trampoline.done(Result.success(false, ParseError.of(s), s, s.getPosition()))); }
	// endregion

	// region Error messages
	public static <S, U, A> Parser<S, U, A> label(Parser<S, U, A> p, String... messages) {
		return of(s -> p.parse(s).map(r -> r.caseOf(
			success -> success.isConsumed() || success.getError().isEmpty() ? success : success.replaceError(messages),
			fail -> fail.isConsumed() ? fail : fail.replaceError(messages)
		)));
	}
	public static <S, U, A> Parser<S, U, A> remark(Parser<S, U, A> p, String... messages) {
		return of(s -> p.parse(s).map(r -> r.caseOf(
			success -> success.isConsumed() || success.getError().isEmpty() ? success : success.replaceError(messages),
			fail -> fail.isConsumed() ? fail : fail.replaceError(messages)
		)));
	}
	public static <S, U, A> Parser<S, U, A> suppress(Parser<S, U, A> p) {
		return of(s -> p.parse(s).map(r -> r.isConsumed() ? r : r.withError(ParseError.of(s))));
	}
	public static <S, U> Parser<S, U, Unit> ensure(boolean condition, String... messages) {
		return condition ? unit() : panic(messages);
	}
	// endregion

	// region Control
	public static <S, U, A> Parser<S, U, A> lookahead(Parser<S, U, A> p) {
		return of(s -> p.parse(s).flatMap(r -> r.caseOf(
			success -> Trampoline.done(success.withConsumed(false).withError(ParseError.of(s))),
			Trampoline::done
		)));
	}
	public static <S, U, A> Parser<S, U, A> attempt(Parser<S, U, A> p) {
		return of(s -> p.parse(s).flatMap(r -> r.caseOf(
			Trampoline::done,
			fail -> Trampoline.done(fail.withConsumed(false))
		)));
	}
	public static <S, U, A> Parser<S, U, A> advancing(Parser<S, U, A> p) {
		return of(s -> p.parse(s).flatMap(r -> r.caseOf(
			success -> Trampoline.done(success.isConsumed() ? success : Result.fail(false, success.getError())),
			Trampoline::done
		)));
	}
	// endregion

	// region Token
	public static <S, T, U> Parser<S, U, T> token(TokenStream<S, T> tokenStream) { return token(tokenStream, Maybe::ofJust); }
	public static <S, T, U, A> Parser<S, U, A> token(TokenStream<S, T> tokenStream, Function<T, Maybe<A>> matching) {
		return of(s -> Trampoline.done(tokenStream.unconsToken(s.getStream()).caseOf(
			() -> Result.fail(false, ParseError.of(s, "unexpected: <eof>")),
			uncons -> matching.apply(uncons.getFirst()).caseOf(
				() -> Result.fail(false, ParseError.of(s, "unexpected: " + tokenStream.printToken(uncons.getFirst()))),
				a -> Functional.apply(
					p -> Result.success(true, ParseError.of(p), State.of(uncons.getSecond(), s.getUserState(), p), a),
					tokenStream.advanceToken(s.getPosition(), uncons.getFirst(), uncons.getSecond())
				)
			)
		)));
	}
	@SafeVarargs
	public static <S, T, U> Parser<S, U, S> tokens(TokenStream<S, T> tokenStream, T... tokens) { return tokens(tokenStream, Arrays.asList(tokens)); }
	public static <S, T, U> Parser<S, U, S> tokens(TokenStream<S, T> tokenStream, List<T> tokens) { return tokens(tokenStream, tokenStream.fromTokens(tokens)); }
	public static <S, T, U> Parser<S, U, S> tokens(TokenStream<S, T> tokenStream, S stream) {
		return of(s -> Trampoline.done(tokenStream.prefixStream(s.getStream(), stream).caseOf(
			headStream -> Result.fail(false, ParseError.of(s, "unexpected: " + tokenStream.printStream(headStream), "expected: " + tokenStream.printStream(stream))),
			tailStream -> Functional.apply(
				p -> Result.success(true, ParseError.of(p), State.of(tailStream, s.getUserState(), p), stream),
				tokenStream.advanceStream(s.getPosition(), stream, tailStream)
			)
		)));
	}
	public static <S, T, U> Parser<S, U, S> tokens(TokenStream<S, T> tokenStream, Predicate<T> f) {
		return of(s -> Trampoline.done(tokenStream.spanStream(s.getStream(), f).caseOf(
			(headStream, tailStream) -> Functional.apply(
				p -> Result.success(!tokenStream.isEmpty(headStream), ParseError.of(p), State.of(tailStream, s.getUserState(), p), headStream),
				tokenStream.advanceStream(s.getPosition(), headStream, tailStream)
			)
		)));
	}

	public static <S, T, U> Parser<S, U, Unit> eof(TokenStream<S, T> tokenStream) {
		return attempt(Parser.<S, T, U>token(tokenStream))
			.<Unit>flatMap(t -> panic("unexpected: " + tokenStream.printToken(t), "expected: <eof>"))
			.or(unit());
	}
	// endregion

    // region Higher Kinded Type
    public enum HKTWitness {}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <S, U, A> Equality<TypeConstructor3<HKTWitness, S, U, A>, Parser<S, U, A>> reflHKT() {
		return (Equality) Equality.ofRefl();
	}

	public static <S, U, A> Parser<S, U, A> coerceHKT(TC<TC<TC<HKTWitness, S>, U>, A> hkt) {
		return (Parser<S, U, A>) hkt;
	}
    // endregion

    // region Type Classes
    public static <S, U> Functor<TC<TC<HKTWitness, S>, U>> functor() { return ParserMonad.getInstance(); }
	public static <S, U> Applicative<TC<TC<HKTWitness, S>, U>> applicative() { return ParserMonad.getInstance(); }
	public static <S, U> Monad<TC<TC<HKTWitness, S>, U>> monad() { return ParserMonad.getInstance(); }

	static class ParserMonad<S, U> implements Monad<TC<TC<HKTWitness, S>, U>> {
        @SuppressWarnings({"unchecked", "rawtypes"})
		static final Monad<TC<TC<HKTWitness, ?>, ?>> INSTANCE = new ParserMonad();
        @SuppressWarnings({"unchecked", "rawtypes"})
		static <S, U> Monad<TC<TC<HKTWitness, S>, U>> getInstance() { return (Monad) INSTANCE; }

		@Override public <A> TC<TC<TC<HKTWitness, S>, U>, A> pure(A a) { return of(a); }
		@Override public <A, B> TC<TC<TC<HKTWitness, S>, U>, B> flatMap(Function<A, TC<TC<TC<HKTWitness, S>, U>, B>> f, TC<TC<TC<HKTWitness, S>, U>, A> a) { return coerceHKT(a).flatMap(x -> coerceHKT(f.apply(x))); }
	}
    // endregion
}
