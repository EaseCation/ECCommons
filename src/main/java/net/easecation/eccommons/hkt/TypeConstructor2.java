package net.easecation.eccommons.hkt;

import net.easecation.eccommons.adt.Equality;

public interface TypeConstructor2<f, A, B> extends TC<TC<f, A>, B> {
    @SuppressWarnings({"unchecked", "rawtypes"})
    static <f, A, B> Equality<TC<TC<f, A>, B>, TypeConstructor2<f, A, B>> reflHKT() {
        return (Equality) Equality.ofRefl();
    }

    static <f, A, B> TypeConstructor2<f, A, B> coerceHKT(TC<TC<f, A>, B> hkt) {
        return (TypeConstructor2<f, A, B>) hkt;
    }
}
