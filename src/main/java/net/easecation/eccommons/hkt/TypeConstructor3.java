package net.easecation.eccommons.hkt;

import net.easecation.eccommons.adt.Equality;

public interface TypeConstructor3<f, A, B, C> extends TypeConstructor2<TC<f, A>, B, C> {
    @SuppressWarnings({"unchecked", "rawtypes"})
    static <f, A, B, C> Equality<TypeConstructor2<TC<f, A>, B, C>, TypeConstructor3<f, A, B, C>> reflHKT() {
        return (Equality) Equality.ofRefl();
    }

    static <f, A, B, C> TypeConstructor3<f, A, B, C> coerceHKT(TC<TC<TC<f, A>, B>, C> hkt) {
        return (TypeConstructor3<f, A, B, C>) hkt;
    }
}
