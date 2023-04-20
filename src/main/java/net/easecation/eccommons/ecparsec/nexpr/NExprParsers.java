package net.easecation.eccommons.ecparsec.nexpr;

import java.util.Objects;
import net.easecation.eccommons.adt.ConsList;
import net.easecation.eccommons.adt.Either;
import net.easecation.eccommons.ecparsec.Combinator;
import net.easecation.eccommons.ecparsec.Parser;

public final class NExprParsers {
	public static <S, U, N, T> Parser<S, U, T> exprP(NExprDefinition<S, U, N, T> definition) {
		return levelP(definition, definition.getLevels());
	}

	static <S, U, N, T> Parser<S, U, T> levelP(NExprDefinition<S, U, N, T> definition, ConsList<NExprLevel<N, T>> current) {
		return current.caseOf(
			() -> Parser.recur(() -> bracketP(definition)),
			(level, higher) -> {
				switch (level.getAssociativity()) {
				case NONE: return Parser.recur(() -> noneP(definition, level, higher));
				case LEFT: return Parser.recur(() -> leftP(definition, level, higher));
				case RIGHT: return Parser.recur(() -> rightP(definition, level, higher));
				default: throw new IllegalStateException();
				}
			}
		);
	}

	static <S, U, N, T> Parser<S, U, T> noneP(NExprDefinition<S, U, N, T> definition, NExprLevel<N, T> level, ConsList<NExprLevel<N, T>> higher) {
		return Parser.recur(() -> levelP(definition, higher)).flatMap(expr ->
			Parser.recur(() -> operandP(
				definition,
				level.getOperands(),
				Parser.of(expr),
				Parser.recur(() -> levelP(definition, higher))
			)).flatMap(result -> result.caseOf(
				operands -> Combinator.loop(operands, operands1 ->
					Parser.recur(() -> operandP(
						definition,
						operands1,
						Parser.recur(() -> levelP(definition, higher)),
						Parser.recur(() -> levelP(definition, higher))
					))
				),
				Parser::of
			)).orElse(expr)
		);
	}

	static <S, U, N, T> Parser<S, U, T> leftP(NExprDefinition<S, U, N, T> definition, NExprLevel<N, T> level, ConsList<NExprLevel<N, T>> higher) {
		return Parser.recur(() -> levelP(definition, higher)).flatMap(expr ->
			Combinator.iterateSome(expr, expr1 -> Parser.recur(() -> operandP(
				definition,
				level.getOperands(),
				Parser.of(expr1),
				Parser.recur(() -> levelP(definition, higher))
			)).flatMap(result -> result.caseOf(
				operands -> Combinator.loop(operands, operands1 ->
					Parser.recur(() -> operandP(
						definition,
						operands1,
						Parser.recur(() -> levelP(definition, higher)),
						Parser.recur(() -> levelP(definition, higher))
					))
				),
				Parser::of
			)))
		);
	}

	static <S, U, N, T> Parser<S, U, T> rightP(NExprDefinition<S, U, N, T> definition, NExprLevel<N, T> level, ConsList<NExprLevel<N, T>> higher) {
		return Parser.recur(() -> levelP(definition, higher)).flatMap(expr ->
			Parser.recur(() -> operandP(
				definition,
				level.getOperands(),
				Parser.of(expr),
				Parser.recur(() -> levelP(definition, higher))
			)).flatMap(result -> result.caseOf(
				operands -> Combinator.loop(operands, operands1 ->
					Parser.recur(() -> operandP(
						definition,
						operands1,
						Parser.recur(() -> levelP(definition, higher)),
						Parser.recur(() -> levelP(definition, ConsList.cons(level, higher)))
					))
				),
				Parser::of
			)).orElse(expr)
		);
	}

	static <S, U, N, T> Parser<S, U, Either<ConsList<NExprOperand<N, T>>, T>> operandP(NExprDefinition<S, U, N, T> definition, ConsList<NExprOperand<N, T>> operands, Parser<S, U, T> nodeExprP, Parser<S, U, T> tailExprP) {
		return operands.stream().flatMap(operand -> operand.getTail().map(NExprOperand.Tail::getNext).stream()).findFirst()
			.map(next -> tailExprP.map(expr ->
				Either.<ConsList<NExprOperand<N, T>>, T>ofRight(next.apply(expr)))
			).orElseGet(() -> nodeExprP.flatMap(expr ->
				Parser.attempt(definition.getSpacesP()
					.andThen(Parser.recur(() -> notationP(definition, operands))
					.flatMap(operands1 -> definition.getSpacesP()
					.replace(operands1)))
				).map(operands1 -> Either.ofLeft(operands1
					.flatMap(operand -> operand.getNode().map(NExprOperand.Node::getNext).toConsList())
					.map(next -> next.apply(expr))
				))
			));
	}

	static <S, U, N, T> Parser<S, U, ConsList<NExprOperand<N, T>>> notationP(NExprDefinition<S, U, N, T> definition, ConsList<NExprOperand<N, T>> operands) {
		return operands.stream()
			.flatMap(operand -> operand.getNode().map(NExprOperand.Node::getNotation).stream())
			.distinct()
			.map(notation -> Parser.attempt(definition.getNotationP().apply(notation)).replace(notation))
			.reduce(Parser.none(), Parser::or)
			.map(notation -> operands.filter(operand -> operand.getNode().map(NExprOperand.Node::getNotation)
				.filter(notation1 -> Objects.equals(notation1, notation))
				.isJust()
			));
	}

	static <S, U, N, T> Parser<S, U, T> bracketP(NExprDefinition<S, U, N, T> definition) {
		return definition.getBracketBeginP()
			.andThen(definition.getSpacesP()
			.andThen(Parser.recur(() -> levelP(definition, definition.getLevels())
			.flatMap(expr -> definition.getSpacesP()
			.andThen(definition.getBracketEndP()
			.replace(expr)))))
		).or(definition.getScalarP());
	}
}
