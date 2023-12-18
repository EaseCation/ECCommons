package net.easecation.eccommons.control;

import net.easecation.eccommons.hkt.TC;

import java.util.function.Function;

public interface Applicative<F> extends Functor<F> {
    <A, B> TC<F, B> applyMap(TC<F, Function<A, B>> f, TC<F, A> a);
}
