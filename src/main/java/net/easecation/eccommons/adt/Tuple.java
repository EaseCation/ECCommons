package net.easecation.eccommons.adt;

import net.easecation.eccommons.hkt.TC;
import net.easecation.eccommons.hkt.TypeConstructor2;

import java.util.Objects;

/**
 * ADT type with A * B element
 */
public final class Tuple<A, B> implements TypeConstructor2<Tuple.HKTWitness, A, B> {
	public final A first;
	public final B second;

	private Tuple(A first, B second) {
		this.first = first;
		this.second = second;
	}

	// Higher Kinded Type
	public enum HKTWitness {}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <A, B> Equality<TypeConstructor2<HKTWitness, A, B>, Tuple<A, B>> reflHKT() {
		return (Equality) Equality.ofRefl();
	}

	public static <A, B> Tuple<A, B> coerceHKT(TC<TC<HKTWitness, A>, B> hkt) {
		return (Tuple<A, B>) hkt;
	}

	// Term introduction
	public static <A, B> Tuple<A, B> of(A first, B second) {
		return new Tuple<>(first, second);
	}

	// Term elimination
	public A getFirst() {
		return first;
	}

	public B getSecond() {
		return second;
	}

	public <T> Tuple<T, B> setFirst(T first) {
		return of(first, second);
	}

	public <T> Tuple<A, T> setSecond(T second) {
		return of(first, second);
	}

	// Pattern matching
	public interface Case<A, B, R> {
		R caseTuple(A first, B second);
	}

	public interface Match<A, B, R> extends Tuple.Case<A, B, R> {

	}

	public <R> R match(Match<A, B, R> match) {
		return caseOf(match);
	}

	public <R> R caseOf(Tuple.Case<A, B, R> caseTuple) {
		return caseTuple.caseTuple(first, second);
	}

	// Java
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Tuple<?, ?> tuple = (Tuple<?, ?>) o;
		return Objects.equals(first, tuple.first) && Objects.equals(second, tuple.second);
	}

	@Override
	public int hashCode() {
		return Objects.hash(first, second);
	}

	@Override
	public String toString() {
		return "Tuple{" +
			"first=" + first +
			", second=" + second +
			'}';
	}
}
