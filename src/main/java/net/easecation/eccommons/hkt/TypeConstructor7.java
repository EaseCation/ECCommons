package net.easecation.eccommons.hkt;

import net.easecation.eccommons.adt.Equality;

public interface TypeConstructor7<f, A, B, C, D, E, F, G> extends TypeConstructor6<TC<f, A>, B, C, D, E, F, G> {
    @SuppressWarnings({"unchecked", "rawtypes"})
    static <f, A, B, C, D, E, F, G> Equality<TypeConstructor6<TC<f, A>, B, C, D, E, F, G>, TypeConstructor7<f, A, B, C, D, E, F, G>> reflHKT() {
        return (Equality) Equality.ofRefl();
    }

    static <f, A, B, C, D, E, F, G> TypeConstructor7<f, A, B, C, D, E, F, G> coerceHKT(TC<TC<TC<TC<TC<TC<TC<f, A>, B>, C>, D>, E>, F>, G> hkt) {
        return (TypeConstructor7<f, A, B, C, D, E, F, G>) hkt;
    }
}
