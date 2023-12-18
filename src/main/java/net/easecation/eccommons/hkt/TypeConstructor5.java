package net.easecation.eccommons.hkt;

import net.easecation.eccommons.adt.Equality;

public interface TypeConstructor5<f, A, B, C, D, E> extends TypeConstructor4<TC<f, A>, B, C, D, E> {
    @SuppressWarnings({"unchecked", "rawtypes"})
    static <f, A, B, C, D, E> Equality<TypeConstructor4<TC<f, A>, B, C, D, E>, TypeConstructor5<f, A, B, C, D, E>> reflHKT() {
        return (Equality) Equality.ofRefl();
    }

    static <f, A, B, C, D, E> TypeConstructor5<f, A, B, C, D, E> coerceHKT(TC<TC<TC<TC<TC<f, A>, B>, C>, D>, E> hkt) {
        return (TypeConstructor5<f, A, B, C, D, E>) hkt;
    }
}