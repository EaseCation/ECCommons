package net.easecation.eccommons.hkt;

import net.easecation.eccommons.adt.Equality;

public interface TypeConstructor9<f, A, B, C, D, E, F, G, H, I> extends TypeConstructor8<TC<f, A>, B, C, D, E, F, G, H, I> {
    @SuppressWarnings({"unchecked", "rawtypes"})
    static <f, A, B, C, D, E, F, G, H, I> Equality<TypeConstructor8<TC<f, A>, B, C, D, E, F, G, H, I>, TypeConstructor9<f, A, B, C, D, E, F, G, H, I>> reflHKT() {
        return (Equality) Equality.ofRefl();
    }

    static <f, A, B, C, D, E, F, G, H, I> TypeConstructor9<f, A, B, C, D, E, F, G, H, I> coerceHKT(TC<TC<TC<TC<TC<TC<TC<TC<TC<f, A>, B>, C>, D>, E>, F>, G>, H>, I> hkt) {
        return (TypeConstructor9<f, A, B, C, D, E, F, G, H, I>) hkt;
    }
}
