package net.easecation.eccommons.ecparsec.expr;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;
import net.easecation.eccommons.adt.ConsList;

@Value
@With
@AllArgsConstructor(staticName = "of")
public class ExprGroupedOperands<S, U, A> {
	ConsList<ExprOperand.InfixN<S, U, A>> infixNs;
	ConsList<ExprOperand.InfixL<S, U, A>> infixLs;
	ConsList<ExprOperand.InfixR<S, U, A>> infixRs;
	ConsList<ExprOperand.Prefix<S, U, A>> prefixes;
	ConsList<ExprOperand.Suffix<S, U, A>> suffixes;

	public static <S, U, A> ExprGroupedOperands<S, U, A> of(ConsList<ExprOperand<S, U, A>> operands) {
		ConsList<ExprOperand.InfixN<S, U, A>> infixNs = ConsList.nil();
		ConsList<ExprOperand.InfixL<S, U, A>> infixLs = ConsList.nil();
		ConsList<ExprOperand.InfixR<S, U, A>> infixRs = ConsList.nil();
		ConsList<ExprOperand.Prefix<S, U, A>> prefixes = ConsList.nil();
		ConsList<ExprOperand.Suffix<S, U, A>> suffixes = ConsList.nil();
		List<ExprOperand<S, U, A>> operandList = operands.stream().collect(Collectors.toList());
		for (int i = operandList.size(); i-- > 0; ) {
			ExprOperand<S, U, A> operand = operandList.get(i);
			ExprOperand.InfixN<S, U, A> infixN = operand.getInfixN().fromJust(null);
			ExprOperand.InfixL<S, U, A> infixL = operand.getInfixL().fromJust(null);
			ExprOperand.InfixR<S, U, A> InfixR = operand.getInfixR().fromJust(null);
			ExprOperand.Prefix<S, U, A> prefix = operand.getPrefix().fromJust(null);
			ExprOperand.Suffix<S, U, A> suffix = operand.getSuffix().fromJust(null);
			if (infixN != null) infixNs = ConsList.cons(infixN, infixNs);
			if (infixL != null) infixLs = ConsList.cons(infixL, infixLs);
			if (InfixR != null) infixRs = ConsList.cons(InfixR, infixRs);
			if (prefix != null) prefixes = ConsList.cons(prefix, prefixes);
			if (suffix != null) suffixes = ConsList.cons(suffix, suffixes);
		}
		return of(infixNs, infixLs, infixRs, prefixes, suffixes);
	}
}
