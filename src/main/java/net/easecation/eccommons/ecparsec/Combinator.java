package net.easecation.eccommons.ecparsec;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import net.easecation.eccommons.adt.ConsList;
import net.easecation.eccommons.adt.Either;
import net.easecation.eccommons.adt.Unit;

public final class Combinator {
	@SafeVarargs
	public static <S, U> Parser<S, U, Unit> sequence(Parser<S, U, ?>... ps) {
		return sequence(Arrays.stream(ps));
	}
	public static <S, U> Parser<S, U, Unit> sequence(ConsList<Parser<S, U, ?>> ps) {
		return sequence(ps.stream());
	}
	public static <S, U> Parser<S, U, Unit> sequence(Stream<Parser<S, U, ?>> ps) {
		return ps.reduce(Parser.unit(), Parser::andThen).discard();
	}
	@SafeVarargs
	public static <S, U, A> Parser<S, U, A> choice(Parser<S, U, A>... ps) {
		return choice(Arrays.stream(ps));
	}
	public static <S, U, A> Parser<S, U, A> choice(ConsList<Parser<S, U, A>> ps) {
		return choice(ps.stream());
	}
	public static <S, U, A> Parser<S, U, A> choice(Stream<Parser<S, U, A>> ps) {
		return ps.reduce(Parser.none(), Parser::or);
	}

	public static <S, U, A> Parser<S, U, ConsList<A>> replicate(int i, Parser<S, U, A> p) {
		return i <= 0 ? Parser.of(ConsList.nil()) : p
			.flatMap(a -> Parser.recur(() -> replicate(i - 1, p))
			.map(as -> ConsList.cons(a, as)));
	}
	public static <S, U, A, B> Parser<S, U, B> loop(A a, Function<A, Parser<S, U, Either<A, B>>> f) {
		return f.apply(a).flatMap(e -> e.caseOf(
			x -> Parser.recur(() -> loop(x, f)),
			Parser::of
		));
	}

	public static <S, U, A> Parser<S, U, A> between(Parser<S, U, ?> begin, Parser<S, U, ?> end, Parser<S, U, A> p) {
		return begin.andThen(p.flatMap(end::replace));
	}

	public static <S, U, A> Parser<S, U, ConsList<A>> some(Parser<S, U, A> p) {
		return Parser.recur(() -> many(p)).or(Parser.of(ConsList.nil()));
	}
	public static <S, U, A> Parser<S, U, ConsList<A>> many(Parser<S, U, A> p) {
		return p.flatMap(a -> Parser.recur(() -> some(p)).map(as -> ConsList.cons(a, as)));
	}

	public static <S, U, A> Parser<S, U, A> iterateSome(A a, Function<A, Parser<S, U, A>> f) {
		return Parser.recur(() -> iterateMany(a, f)).or(Parser.of(a));
	}
	public static <S, U, A> Parser<S, U, A> iterateMany(A a, Function<A, Parser<S, U, A>> f) {
		return f.apply(a).flatMap(x -> Parser.recur(() -> iterateSome(x, f)));
	}

	public static <S, U, A, B> Parser<S, U, B> foldSome(BiFunction<B, A, Parser<S, U, B>> f, B b, Parser<S, U, A> p) {
		return Parser.recur(() -> foldMany(f, b, p)).or(Parser.of(b));
	}
	public static <S, U, A, B> Parser<S, U, B> foldMany(BiFunction<B, A, Parser<S, U, B>> f, B b, Parser<S, U, A> p) {
		return p.flatMap(a -> f.apply(b, a).flatMap(x -> Parser.recur(() -> foldSome(f, x, p))));
	}

	public static <S, U, A> Parser<S, U, Unit> skipSome(Parser<S, U, A> p) {
		return Parser.recur(() -> skipMany(p).or(Parser.unit()));
	}
	public static <S, U, A> Parser<S, U, Unit> skipMany(Parser<S, U, A> p) {
		return p.andThen(Parser.recur(() -> skipSome(p)));
	}

	public static <S, U, A> Parser<S, U, ConsList<A>> someSep(Parser<S, U, ?> sep, Parser<S, U, A> p) {
		return Parser.recur(() -> manySep(sep, p)).or(Parser.of(ConsList.nil()));
	}
	public static <S, U, A> Parser<S, U, ConsList<A>> manySep(Parser<S, U, ?> sep, Parser<S, U, A> p) {
		return p.flatMap(a -> Parser.recur(() -> some(sep.andThen(p))).map(as -> ConsList.cons(a, as)));
	}


	public static <S, U, A> Parser<S, U, A> iterateSomeSep(Parser<S, U, ?> sep, A a, Function<A, Parser<S, U, A>> f) {
		return Parser.recur(() -> iterateManySep(sep, a, f)).or(Parser.of(a));
	}
	public static <S, U, A> Parser<S, U, A> iterateManySep(Parser<S, U, ?> sep, A a, Function<A, Parser<S, U, A>> f) {
		return f.apply(a).flatMap(x -> Parser.recur(() -> iterateSome(x, y -> sep.andThen(f.apply(y)))));
	}

	public static <S, U, A> Parser<S, U, Unit> skipSomeSep(Parser<S, U, ?> sep, Parser<S, U, A> p) {
		return Parser.recur(() -> skipManySep(sep, p).or(Parser.unit()));
	}
	public static <S, U, A> Parser<S, U, Unit> skipManySep(Parser<S, U, ?> sep, Parser<S, U, A> p) {
		return p.andThen(Parser.recur(() -> skipSome(sep.andThen(p))));
	}

	public static <S, U, A, B> Parser<S, U, B> foldSomeSep(Parser<S, U, ?> sep, BiFunction<B, A, Parser<S, U, B>> f, B b, Parser<S, U, A> p) {
		return Parser.recur(() -> foldManySep(sep, f, b, p)).or(Parser.of(b));
	}
	public static <S, U, A, B> Parser<S, U, B> foldManySep(Parser<S, U, ?> sep, BiFunction<B, A, Parser<S, U, B>> f, B b, Parser<S, U, A> p) {
		return p.flatMap(a -> f.apply(b, a).flatMap(x -> Parser.recur(() -> foldSome(f, x, sep.andThen(p)))));
	}
}
