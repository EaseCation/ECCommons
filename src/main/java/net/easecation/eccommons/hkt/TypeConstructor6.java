package net.easecation.eccommons.hkt;

import net.easecation.eccommons.adt.Equality;

public interface TypeConstructor6<f, A, B, C, D, E, F> extends TypeConstructor5<TC<f, A>, B, C, D, E, F> {
    @SuppressWarnings({"unchecked", "rawtypes"})
    static <f, A, B, C, D, E, F> Equality<TypeConstructor5<TC<f, A>, B, C, D, E, F>, TypeConstructor6<f, A, B, C, D, E, F>> reflHKT() {
        return (Equality) Equality.ofRefl();
    }

    static <f, A, B, C, D, E, F> TypeConstructor6<f, A, B, C, D, E, F> coerceHKT(TC<TC<TC<TC<TC<TC<f, A>, B>, C>, D>, E>, F> hkt) {
        return (TypeConstructor6<f, A, B, C, D, E, F>) hkt;
    }
}
