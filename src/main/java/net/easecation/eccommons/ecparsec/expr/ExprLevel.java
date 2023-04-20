package net.easecation.eccommons.ecparsec.expr;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;
import net.easecation.eccommons.adt.ConsList;

@Value
@With
@AllArgsConstructor(staticName = "of")
public class ExprLevel<S, U, A> {
	ConsList<ExprOperand<S, U, A>> operands;

	@SafeVarargs
	public static <S, U, A> ExprLevel<S, U, A> of(ExprOperand<S, U, A>... operands) {
		return of(ConsList.list(operands));
	}
}
