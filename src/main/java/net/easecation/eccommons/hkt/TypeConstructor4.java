package net.easecation.eccommons.hkt;

import net.easecation.eccommons.adt.Equality;

public interface TypeConstructor4<f, A, B, C, D> extends TypeConstructor3<TC<f, A>, B, C, D> {
    @SuppressWarnings({"unchecked", "rawtypes"})
    static <f, A, B, C, D> Equality<TypeConstructor3<TC<f, A>, B, C, D>, TypeConstructor4<f, A, B, C, D>> reflHKT() {
        return (Equality) Equality.ofRefl();
    }

    static <f, A, B, C, D> TypeConstructor4<f, A, B, C, D> coerceHKT(TC<TC<TC<TC<f, A>, B>, C>, D> hkt) {
        return (TypeConstructor4<f, A, B, C, D>) hkt;
    }
}
