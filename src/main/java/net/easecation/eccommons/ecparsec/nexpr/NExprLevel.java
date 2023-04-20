package net.easecation.eccommons.ecparsec.nexpr;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;
import net.easecation.eccommons.adt.ConsList;

@Value
@With
@AllArgsConstructor(staticName = "of")
public class NExprLevel<N, T> {
	NExprAssociativity associativity;
	ConsList<NExprOperand<N, T>> operands;

	@SafeVarargs
	public static <N, T> NExprLevel<N, T> of(NExprAssociativity associativity, NExprOperand<N, T>... operands) {
		return new NExprLevel<>(associativity, ConsList.list(operands));
	}
}
