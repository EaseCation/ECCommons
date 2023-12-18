package net.easecation.eccommons.adt;

import net.easecation.eccommons.hkt.TC;
import net.easecation.eccommons.hkt.TypeConstructor2;

/**
 * Equality between types
 */
public abstract class Equality<A, B> implements TypeConstructor2<Equality.HKTWitness, A, B> {
	private Equality() {

	}

	// Higher Kinded Type
    public enum HKTWitness {}

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <A, B> Equality<TypeConstructor2<HKTWitness, A, B>, Equality<A, B>> reflHKT() {
        return (Equality) ofRefl();
    }

    public static <A, B> Equality<A, B> coerceHKT(TC<TC<HKTWitness, A>, B> hkt) {
        return Equality.<A, B>reflHKT().coerce(TypeConstructor2.coerceHKT(hkt));
    }

	// Term introduction
    @SuppressWarnings("unchecked")
    public static <A> Equality<A, A> ofRefl() {
        return (Equality<A, A>) Refl.REFL;
    }

	// Term elimination
    public abstract Equality<B, A> symmetry();

    public abstract <C> Equality<A, C> transitivity(Equality<B, C> equality);

    public abstract <f> TC<f, B> substitute(TC<f, A> fa);

    public abstract <f> Equality<TC<f, A>, TC<f, B>> congruence();

    public abstract B coerce(A a);

	// Java
	@Override
	public abstract String toString();

	// Constructors
    public static final class Refl<A> extends Equality<A, A> {
		private static final Refl<?> REFL = new Refl<>();

        @Override
        public Equality<A, A> symmetry() {
            return ofRefl();
        }

        @Override
        public <C> Equality<A, C> transitivity(Equality<A, C> equality) {
            return equality;
        }

        @Override
        public <f> TC<f, A> substitute(TC<f, A> fa) {
            return fa;
        }

        @Override
        public <f> Equality<TC<f, A>, TC<f, A>> congruence() {
            return ofRefl();
        }

        @Override
        public A coerce(A a) {
            return a;
        }

		@Override
		public String toString() {
			return "Refl{}";
		}
	}
}
