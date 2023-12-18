package net.easecation.eccommons.control;

import net.easecation.eccommons.adt.Unit;
import net.easecation.eccommons.hkt.TC;

import java.util.function.Function;

public interface Functor<F> {
    <A, B> TC<F, B> map(Function<A, B> f, TC<F, A> a);

    default <A, B> TC<F, B> replace(B b, TC<F, A> a) {
        return map(x -> b, a);
    }

    default <A> TC<F, Unit> discard(TC<F, A> a) {
        return replace(Unit.UNIT, a);
    }
}
