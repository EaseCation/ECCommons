package net.easecation.eccommons.ecparsec.expr;

import java.util.function.BiFunction;
import java.util.function.Function;
import net.easecation.eccommons.adt.ConsList;
import net.easecation.eccommons.adt.Either;
import net.easecation.eccommons.control.Functional;
import net.easecation.eccommons.ecparsec.Combinator;
import net.easecation.eccommons.ecparsec.Parser;

public final class ExprParsers {
	public static <S, U, A> Parser<S, U, A> exprP(ExprDefinition<S, U, A> definition) {
		return exprP(definition.getSpaces(), termP(definition), definition.getLevels());
	}

	public static <S, U, A> Parser<S, U, A> exprP(
		Parser<S, U, ?> spacesP, Parser<S, U, A> termP,
		ConsList<ExprLevel<S, U, A>> levels
	) {
		return levels.foldl(
			(nextTermP, nextLevels) -> Parser.recur(() -> ExprParsers.levelP(spacesP, nextTermP, nextLevels)),
			Parser.recur(() -> termP)
		);
	}

	public static <S, U, A> Parser<S, U, A> levelP(
		Parser<S, U, ?> spacesP, Parser<S, U, A> termP,
		ExprLevel<S, U, A> level
	) {
		return levelP(spacesP, termP, level.getOperands());
	}

	public static <S, U, A> Parser<S, U, A> levelP(
		Parser<S, U, ?> spacesP, Parser<S, U, A> termP,
		ConsList<ExprOperand<S, U, A>> operands
	) {
		return levelP(spacesP, termP, ExprGroupedOperands.of(operands));
	}

	public static <S, U, A> Parser<S, U, A> levelP(
		Parser<S, U, ?> spacesP, Parser<S, U, A> termP,
		ExprGroupedOperands<S, U, A> groupedOperands
	) {
		Parser<S, U, A> basicTermP = basicTermP(groupedOperands.getPrefixes(), groupedOperands.getSuffixes(), spacesP, termP);
		return basicTermP.flatMap(term -> {
			Parser<S, U, A> infixNP = infixNP(groupedOperands.getInfixNs(), spacesP, basicTermP, term);
			Parser<S, U, A> infixLP = infixLP(groupedOperands.getInfixLs(), spacesP, basicTermP, term);
			Parser<S, U, A> infixRP = infixRP(groupedOperands.getInfixRs(), spacesP, basicTermP, term);
			return Combinator.choice(infixRP, infixLP, infixNP, Parser.of(term));
		});
	}

	public static <S, U, A> Parser<S, U, A> basicTermP(
		ConsList<ExprOperand.Prefix<S, U, A>> prefixes, ConsList<ExprOperand.Suffix<S, U, A>> suffixes,
		Parser<S, U, ?> spacesP, Parser<S, U, A> termP
	) {
		return basicTermP(
			Combinator.choice(prefixes.stream().map(ExprOperand.Prefix::getParser)),
			Combinator.choice(suffixes.stream().map(ExprOperand.Suffix::getParser)),
			spacesP, termP
		);
	}

	public static <S, U, A> Parser<S, U, A> basicTermP(
		Parser<S, U, Function<A, A>> prefixP, Parser<S, U, Function<A, A>> suffixP,
		Parser<S, U, ?> spacesP, Parser<S, U, A> termP
	) {
		return Parser.attempt(prefixP.replaceThen(spacesP)).orElse(Function.identity())
			.flatMap(prefix -> termP
			.flatMap(term -> Parser.attempt(spacesP.andThen(suffixP)).orElse(Function.identity())
			.map(suffix -> suffix.apply(prefix.apply(term)))));
	}

	public static <S, U, A> Parser<S, U, A> infixNP(
		ConsList<ExprOperand.InfixN<S, U, A>> infixNs,
		Parser<S, U, ?> spacesP, Parser<S, U, A> termP, A headTerm
	) {
		return infixNP(
			Combinator.choice(infixNs.stream().map(ExprOperand.InfixN::getParser)),
			spacesP, termP, headTerm
		);
	}

	public static <S, U, A> Parser<S, U, A> infixNP(
		Parser<S, U, Either<Function<A, ExprOperand.InfixN<S, U, A>>, BiFunction<A, A, A>>> infixNP,
		Parser<S, U, ?> spacesP, Parser<S, U, A> termP, A headTerm
	) {
		return Parser.attempt(spacesP.andThen(infixNP.replaceThen(spacesP))).flatMap(next -> next.caseOf(
			nextInfixN -> termP.flatMap(nextTerm -> Parser.recur(() ->
				infixNP(nextInfixN.apply(headTerm).getParser(), spacesP, termP, nextTerm)
			)),
			lastInfixN -> termP.map(nextTerm -> lastInfixN.apply(headTerm, nextTerm))
		));
	}

	public static <S, U, A> Parser<S, U, A> infixLP(
		ConsList<ExprOperand.InfixL<S, U, A>> infixLs,
		Parser<S, U, ?> spacesP, Parser<S, U, A> termP, A headTerm
	) {
		return infixLP(
			Combinator.choice(infixLs.stream().map(ExprOperand.InfixL::getParser)),
			spacesP, termP, headTerm
		);
	}

	public static <S, U, A> Parser<S, U, A> infixLP(
		Parser<S, U, Either<Function<A, ExprOperand.InfixL<S, U, A>>, BiFunction<A, A, A>>> infixLP,
		Parser<S, U, ?> spacesP, Parser<S, U, A> termP, A headTerm
	) {
		return infixLP(
			resultTerm -> Parser.recur(() -> infixLP(infixLP, spacesP, termP, resultTerm)),
			infixLP, spacesP, termP, headTerm
		);
	}

	public static <S, U, A> Parser<S, U, A> infixLP(
		Function<A, Parser<S, U, A>> nextOperatorP,
		Parser<S, U, Either<Function<A, ExprOperand.InfixL<S, U, A>>, BiFunction<A, A, A>>> infixLP,
		Parser<S, U, ?> spacesP, Parser<S, U, A> termP, A headTerm
	) {
		return Parser.attempt(spacesP.andThen(infixLP.replaceThen(spacesP))).flatMap(next -> next.caseOf(
			nextInfixL -> termP.flatMap(nextTerm -> Parser.recur(() ->
				infixLP(nextOperatorP, nextInfixL.apply(headTerm).getParser(), spacesP, termP, nextTerm)
			)),
			lastInfixL -> termP.flatMap(nextTerm -> Functional.apply(
				resultTerm -> nextOperatorP.apply(resultTerm).orElse(resultTerm),
				lastInfixL.apply(headTerm, nextTerm)
			))
		));
	}

	public static <S, U, A> Parser<S, U, A> infixRP(
		ConsList<ExprOperand.InfixR<S, U, A>> infixRs,
		Parser<S, U, ?> spacesP, Parser<S, U, A> termP, A headTerm
	) {
		return infixRP(
			Combinator.choice(infixRs.stream().map(ExprOperand.InfixR::getParser)),
			spacesP, termP, headTerm
		);
	}

	public static <S, U, A> Parser<S, U, A> infixRP(
		Parser<S, U, Either<Function<A, ExprOperand.InfixR<S, U, A>>, BiFunction<A, A, A>>> infixRP,
		Parser<S, U, ?> spacesP, Parser<S, U, A> termP, A headTerm
	) {
		return infixRP(
			resultTerm -> Parser.recur(() -> infixRP(infixRP, spacesP, termP, resultTerm)),
			infixRP, spacesP, termP, headTerm
		);
	}

	public static <S, U, A> Parser<S, U, A> infixRP(
		Function<A, Parser<S, U, A>> nextOperatorP,
		Parser<S, U, Either<Function<A, ExprOperand.InfixR<S, U, A>>, BiFunction<A, A, A>>> infixRP,
		Parser<S, U, ?> spacesP, Parser<S, U, A> termP, A headTerm
	) {
		return Parser.attempt(spacesP.andThen(infixRP.replaceThen(spacesP))).flatMap(next -> next.caseOf(
			nextInfixR -> termP.flatMap(nextTerm -> Parser.recur(() ->
				infixRP(nextOperatorP, nextInfixR.apply(headTerm).getParser(), spacesP, termP, nextTerm)
			)),
			lastInfixR -> termP
				.flatMap(nextTerm -> nextOperatorP.apply(nextTerm).orElse(nextTerm))
				.map(resultTerm -> lastInfixR.apply(headTerm, resultTerm))
		));
	}

	public static <S, U, A> Parser<S, U, A> termP(ExprDefinition<S, U, A> definition) {
		return definition.getBracketBegin()
			.andThen(definition.getSpaces()
			.andThen(Parser.recur(() -> exprP(definition))
			.flatMap(expr -> definition.getSpaces()
			.andThen(definition.getBracketEnd()
			.replace(expr))))
		).or(definition.getTerm());
	}
}
