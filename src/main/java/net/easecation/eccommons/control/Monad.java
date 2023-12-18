package net.easecation.eccommons.control;

import net.easecation.eccommons.hkt.TC;

import java.util.function.Function;

public interface Monad<F> extends Applicative<F> {
    <A> TC<F, A> pure(A a);

    <A, B> TC<F, B> flatMap(Function<A, TC<F, B>> f, TC<F, A> a);

    default <A> TC<F, A> join(TC<F, TC<F, A>> a) {
        return flatMap(x -> x, a);
    }

    default <A, B> TC<F, B> andThen(TC<F, A> a, TC<F, B> b) {
        return flatMap(x -> b, a);
    }

    @Override
    default <A, B> TC<F, B> map(Function<A, B> f, TC<F, A> a) {
        return flatMap(x -> pure(f.apply(x)), a);
    }

    @Override
    default <A, B> TC<F, B> applyMap(TC<F, Function<A, B>> f, TC<F, A> a) {
        return flatMap(g -> map(g, a), f);
    }
}
