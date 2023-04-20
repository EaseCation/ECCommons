package net.easecation.eccommons.ecparsec.nexpr;

import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;
import net.easecation.eccommons.adt.ConsList;
import net.easecation.eccommons.ecparsec.Parser;

@Value
@With
@AllArgsConstructor(staticName = "of")
public class NExprDefinition<S, U, N, T> {
	Parser<S, U, ?> spacesP;
	Parser<S, U, ?> bracketBeginP;
	Parser<S, U, ?> bracketEndP;
	Function<N, Parser<S, U, ?>> notationP;
	Parser<S, U, T> scalarP;
	ConsList<NExprLevel<N, T>> levels;

	@SafeVarargs
	public static <S, U, N, T> NExprDefinition<S, U, N, T> of(Parser<S, U, ?> spacesP, Parser<S, U, ?> bracketBeginP, Parser<S, U, ?> bracketEndP, Function<N, Parser<S, U, ?>> notationP, Parser<S, U, T> termP, NExprLevel<N, T>... levels) {
		return of(spacesP, bracketBeginP, bracketEndP, notationP, termP, ConsList.list(levels));
	}
}
