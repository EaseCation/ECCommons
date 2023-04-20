package net.easecation.eccommons.ecparsec.expr;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;
import net.easecation.eccommons.adt.ConsList;
import net.easecation.eccommons.ecparsec.Parser;

@Value
@With
@AllArgsConstructor(staticName = "of")
public class ExprDefinition<S, U, A> {
	Parser<S, U, ?> spaces;
	Parser<S, U, ?> bracketBegin;
	Parser<S, U, ?> bracketEnd;
	Parser<S, U, A> term;
	ConsList<ExprLevel<S, U, A>> levels;

	@SafeVarargs
	public static <S, U, A> ExprDefinition<S, U, A> of(Parser<S, U, ?> spacesP, Parser<S, U, ?> bracketBeginP, Parser<S, U, ?> bracketEndP, Parser<S, U, A> termP, ExprLevel<S, U, A>... levels) {
		return of(spacesP, bracketBeginP, bracketEndP, termP, ConsList.list(levels));
	}
}
