package net.easecation.eccommons.hkt;

import net.easecation.eccommons.adt.Equality;

public interface TypeConstructor8<f, A, B, C, D, E, F, G, H> extends TypeConstructor7<TC<f, A>, B, C, D, E, F, G, H> {
    @SuppressWarnings({"unchecked", "rawtypes"})
    static <f, A, B, C, D, E, F, G, H> Equality<TypeConstructor7<TC<f, A>, B, C, D, E, F, G, H>, TypeConstructor8<f, A, B, C, D, E, F, G, H>> reflHKT() {
        return (Equality) Equality.ofRefl();
    }

    static <f, A, B, C, D, E, F, G, H> TypeConstructor8<f, A, B, C, D, E, F, G, H> coerceHKT(TC<TC<TC<TC<TC<TC<TC<TC<f, A>, B>, C>, D>, E>, F>, G>, H> hkt) {
        return (TypeConstructor8<f, A, B, C, D, E, F, G, H>) hkt;
    }
}
