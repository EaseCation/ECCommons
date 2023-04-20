package net.easecation.eccommons.ecparsec.expr;

import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.With;
import net.easecation.eccommons.adt.Either;
import net.easecation.eccommons.adt.Maybe;
import net.easecation.eccommons.ecparsec.Parser;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class ExprOperand<S, U, A> {
	@Value
	@With
	@EqualsAndHashCode(callSuper = false)
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class InfixN<S, U, A> extends ExprOperand<S, U, A> {
		Parser<S, U, Either<Function<A, InfixN<S, U, A>>, BiFunction<A, A, A>>> parser;

		// region Pattern matching
		public interface Case<S, U, A, R> { R caseInfixN(InfixN<S, U, A> infixN); }
		@Override public <R> R caseOf(
			InfixN.Case<S, U, A, R> caseInfixN,
			InfixL.Case<S, U, A, R> caseInfixL,
			InfixR.Case<S, U, A, R> caseInfixR,
			Prefix.Case<S, U, A, R> casePrefix,
			Suffix.Case<S, U, A, R> caseSuffix
		) { return caseInfixN.caseInfixN(this); }

		@Override public Maybe<InfixN<S, U, A>> getInfixN() { return Maybe.ofJust(this); }
		@Override public Maybe<InfixL<S, U, A>> getInfixL() { return Maybe.ofNothing(); }
		@Override public Maybe<InfixR<S, U, A>> getInfixR() { return Maybe.ofNothing(); }
		@Override public Maybe<Prefix<S, U, A>> getPrefix() { return Maybe.ofNothing(); }
		@Override public Maybe<Suffix<S, U, A>> getSuffix() { return Maybe.ofNothing(); }
		// endregion
	}


	@Value
	@With
	@EqualsAndHashCode(callSuper = false)
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class InfixL<S, U, A> extends ExprOperand<S, U, A> {
		Parser<S, U, Either<Function<A, InfixL<S, U, A>>, BiFunction<A, A, A>>> parser;

		// region Pattern matching
		public interface Case<S, U, A, R> { R caseInfixL(InfixL<S, U, A> infixL); }
		@Override public <R> R caseOf(
			InfixN.Case<S, U, A, R> caseInfixN,
			InfixL.Case<S, U, A, R> caseInfixL,
			InfixR.Case<S, U, A, R> caseInfixR,
			Prefix.Case<S, U, A, R> casePrefix,
			Suffix.Case<S, U, A, R> caseSuffix
		) { return caseInfixL.caseInfixL(this); }

		@Override public Maybe<InfixN<S, U, A>> getInfixN() { return Maybe.ofNothing(); }
		@Override public Maybe<InfixL<S, U, A>> getInfixL() { return Maybe.ofJust(this); }
		@Override public Maybe<InfixR<S, U, A>> getInfixR() { return Maybe.ofNothing(); }
		@Override public Maybe<Prefix<S, U, A>> getPrefix() { return Maybe.ofNothing(); }
		@Override public Maybe<Suffix<S, U, A>> getSuffix() { return Maybe.ofNothing(); }
		// endregion
	}

	@Value
	@With
	@EqualsAndHashCode(callSuper = false)
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class InfixR<S, U, A> extends ExprOperand<S, U, A> {
		Parser<S, U, Either<Function<A, InfixR<S, U, A>>, BiFunction<A, A, A>>> parser;

		// region Pattern matching
		public interface Case<S, U, A, R> { R caseInfixR(InfixR<S, U, A> infixR); }
		@Override public <R> R caseOf(
			InfixN.Case<S, U, A, R> caseInfixN,
			InfixL.Case<S, U, A, R> caseInfixL,
			InfixR.Case<S, U, A, R> caseInfixR,
			Prefix.Case<S, U, A, R> casePrefix,
			Suffix.Case<S, U, A, R> caseSuffix
		) { return caseInfixR.caseInfixR(this); }

		@Override public Maybe<InfixN<S, U, A>> getInfixN() { return Maybe.ofNothing(); }
		@Override public Maybe<InfixL<S, U, A>> getInfixL() { return Maybe.ofNothing(); }
		@Override public Maybe<InfixR<S, U, A>> getInfixR() { return Maybe.ofJust(this); }
		@Override public Maybe<Prefix<S, U, A>> getPrefix() { return Maybe.ofNothing(); }
		@Override public Maybe<Suffix<S, U, A>> getSuffix() { return Maybe.ofNothing(); }
		// endregion.
	}

	@Value
	@With
	@EqualsAndHashCode(callSuper = false)
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class Prefix<S, U, A> extends ExprOperand<S, U, A> {
		Parser<S, U, Function<A, A>> parser;

		// region Pattern matching
		public interface Case<S, U, A, R> { R casePrefix(Prefix<S, U, A> prefix); }
		@Override public <R> R caseOf(
			InfixN.Case<S, U, A, R> caseInfixN,
			InfixL.Case<S, U, A, R> caseInfixL,
			InfixR.Case<S, U, A, R> caseInfixR,
			Prefix.Case<S, U, A, R> casePrefix,
			Suffix.Case<S, U, A, R> caseSuffix
		) { return casePrefix.casePrefix(this); }

		@Override public Maybe<InfixN<S, U, A>> getInfixN() { return Maybe.ofNothing(); }
		@Override public Maybe<InfixL<S, U, A>> getInfixL() { return Maybe.ofNothing(); }
		@Override public Maybe<InfixR<S, U, A>> getInfixR() { return Maybe.ofNothing(); }
		@Override public Maybe<Prefix<S, U, A>> getPrefix() { return Maybe.ofJust(this); }
		@Override public Maybe<Suffix<S, U, A>> getSuffix() { return Maybe.ofNothing(); }
		// endregion
	}

	@Value
	@With
	@EqualsAndHashCode(callSuper = false)
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class Suffix<S, U, A> extends ExprOperand<S, U, A> {
		Parser<S, U, Function<A, A>> parser;

		// region Pattern matching
		public interface Case<S, U, A, R> { R caseSuffix(Suffix<S, U, A> suffix); }
		@Override public <R> R caseOf(
			InfixN.Case<S, U, A, R> caseInfixN,
			InfixL.Case<S, U, A, R> caseInfixL,
			InfixR.Case<S, U, A, R> caseInfixR,
			Prefix.Case<S, U, A, R> casePrefix,
			Suffix.Case<S, U, A, R> caseSuffix
		) { return caseSuffix.caseSuffix(this); }

		@Override public Maybe<InfixN<S, U, A>> getInfixN() { return Maybe.ofNothing(); }
		@Override public Maybe<InfixL<S, U, A>> getInfixL() { return Maybe.ofNothing(); }
		@Override public Maybe<InfixR<S, U, A>> getInfixR() { return Maybe.ofNothing(); }
		@Override public Maybe<Prefix<S, U, A>> getPrefix() { return Maybe.ofNothing(); }
		@Override public Maybe<Suffix<S, U, A>> getSuffix() { return Maybe.ofJust(this); }
		// endregion
	}

	public static <S, U, A> InfixN<S, U, A> infixN(Parser<S, U, Either<Function<A, InfixN<S, U, A>>, BiFunction<A, A, A>>> parser) { return new InfixN<>(parser); }
	public static <S, U, A> InfixN<S, U, A> infixNL(Parser<S, U, Function<A, InfixN<S, U, A>>> parser) { return infixN(parser.map(Either::ofLeft)); }
	public static <S, U, A> InfixN<S, U, A> infixNR(Parser<S, U, BiFunction<A, A, A>> parser) { return infixN(parser.map(Either::ofRight)); }
	public static <S, U, A> InfixL<S, U, A> infixL(Parser<S, U, Either<Function<A, InfixL<S, U, A>>, BiFunction<A, A, A>>> parser) { return new InfixL<>(parser); }
	public static <S, U, A> InfixL<S, U, A> infixLL(Parser<S, U, Function<A, InfixL<S, U, A>>> parser) { return infixL(parser.map(Either::ofLeft)); }
	public static <S, U, A> InfixL<S, U, A> infixLR(Parser<S, U, BiFunction<A, A, A>> parser) { return infixL(parser.map(Either::ofRight)); }
	public static <S, U, A> InfixR<S, U, A> infixR(Parser<S, U, Either<Function<A, InfixR<S, U, A>>, BiFunction<A, A, A>>> parser) { return new InfixR<>(parser); }
	public static <S, U, A> InfixR<S, U, A> infixRL(Parser<S, U, Function<A, InfixR<S, U, A>>> parser) { return infixR(parser.map(Either::ofLeft)); }
	public static <S, U, A> InfixR<S, U, A> infixRR(Parser<S, U, BiFunction<A, A, A>> parser) { return infixR(parser.map(Either::ofRight)); }
	public static <S, U, A> Prefix<S, U, A> prefix(Parser<S, U, Function<A, A>> parser) { return new Prefix<>(parser); }
	public static <S, U, A> Suffix<S, U, A> suffix(Parser<S, U, Function<A, A>> parser) { return new Suffix<>(parser); }

	// region Pattern matching
	public interface Match<S, U, A, R> extends InfixN.Case<S, U, A, R>, InfixL.Case<S, U, A, R>, InfixR.Case<S, U, A, R>, Prefix.Case<S, U, A, R>, Suffix.Case<S, U, A, R> {}
	public final <R> R match(Match<S, U, A, R> match) { return caseOf(match, match, match, match, match); }
	public abstract <R> R caseOf(
		InfixN.Case<S, U, A, R> caseInfixN,
		InfixL.Case<S, U, A, R> caseInfixL,
		InfixR.Case<S, U, A, R> caseInfixR,
		Prefix.Case<S, U, A, R> casePrefix,
		Suffix.Case<S, U, A, R> caseSuffix
	);

	public abstract Maybe<InfixN<S, U, A>> getInfixN();
	public abstract Maybe<InfixL<S, U, A>> getInfixL();
	public abstract Maybe<InfixR<S, U, A>> getInfixR();
	public abstract Maybe<Prefix<S, U, A>> getPrefix();
	public abstract Maybe<Suffix<S, U, A>> getSuffix();
	// endregion
}
