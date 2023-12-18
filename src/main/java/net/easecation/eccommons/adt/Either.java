package net.easecation.eccommons.adt;

import net.easecation.eccommons.control.Applicative;
import net.easecation.eccommons.control.Functor;
import net.easecation.eccommons.control.Monad;
import net.easecation.eccommons.hkt.TC;
import net.easecation.eccommons.hkt.TypeConstructor2;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;

/**
 * ADT type with A + B element
 */
public abstract class Either<A, B> implements TypeConstructor2<Either.HKTWitness, A, B> {
	private Either() {

	}

	// Higher Kinded Type
	public enum HKTWitness {}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <A, B> Equality<TypeConstructor2<HKTWitness, A, B>, Either<A, B>> reflHKT() {
		return (Equality) Equality.ofRefl();
	}

	public static <A, B> Either<A, B> coerceHKT(TC<TC<HKTWitness, A>, B> hkt) {
		return (Either<A, B>) hkt;
	}

	// Term introduction
	public static <A, B> Either<A, B> ofLeft(A left) {
		return new Left<>(left);
	}

	public static <A, B> Either<A, B> ofRight(B right) {
		return new Right<>(right);
	}

	// Term elimination
	public abstract boolean isLeft();

	public abstract boolean isRight();

	public abstract A fromLeft(A orElse);

	public abstract B fromRight(B orElse);

	public abstract A coerceLeft() throws NoSuchElementException;

	public abstract B coerceRight() throws NoSuchElementException;

	// Monadic Interface
	public abstract <C> Either<A, C> map(Function<B, C> f);

	public abstract <C> Either<A, C> applyMap(Either<A, Function<B, C>> f);

	public abstract <C> Either<A, C> flatMap(Function<B, Either<A, C>> f);

	// Pattern matching
	public abstract <R> R caseOf(Left.Case<A, B, R> caseLeft, Right.Case<A, B, R> caseRight);

	public final <R> R match(Match<A, B, R> match) {
		return caseOf(match, match);
	}

	public interface Match<A, B, R> extends Left.Case<A, B, R>, Right.Case<A, B, R> {

	}

	// Java
	@Override
	public abstract boolean equals(Object obj);

	@Override
	public abstract int hashCode();

	@Override
	public abstract String toString();

	// Constructors
	public static final class Left<A, B> extends Either<A, B> {
		private final A left;

		private Left(A left) {
			this.left = left;
		}

		@Override
		public boolean isLeft() {
			return true;
		}

		@Override
		public boolean isRight() {
			return false;
		}

		@Override
		public A fromLeft(A orElse) {
			return left;
		}

		@Override
		public B fromRight(B orElse) {
			return orElse;
		}

		@Override
		public A coerceLeft() throws NoSuchElementException {
			return left;
		}

		@Override
		public B coerceRight() throws NoSuchElementException {
			throw new NoSuchElementException("Right value not found");
		}

		@Override
		public <C> Either<A, C> map(Function<B, C> f) {
			return ofLeft(left);
		}

		@Override
		public <C> Either<A, C> applyMap(Either<A, Function<B, C>> f) {
			return f.flatMap(this::map);
		}

		@Override
		public <C> Either<A, C> flatMap(Function<B, Either<A, C>> f) {
			return ofLeft(left);
		}

		@Override
		public <R> R caseOf(Left.Case<A, B, R> caseLeft, Right.Case<A, B, R> caseRight) {
			return caseLeft.caseLeft(left);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			Left<?, ?> left1 = (Left<?, ?>) o;
			return Objects.equals(left, left1.left);
		}

		@Override
		public int hashCode() {
			return Objects.hash(1, left);
		}

		@Override
		public String toString() {
			return "Left{" +
				"left=" + left +
				'}';
		}

		public interface Case<A, B, R> {
			R caseLeft(A left);
		}
	}

	public static final class Right<A, B> extends Either<A, B> {
		private final B right;

		private Right(B right) {
			this.right = right;
		}

		@Override
		public boolean isLeft() {
			return false;
		}

		@Override
		public boolean isRight() {
			return true;
		}

		@Override
		public A fromLeft(A orElse) {
			return orElse;
		}

		@Override
		public B fromRight(B orElse) {
			return right;
		}

		@Override
		public A coerceLeft() throws NoSuchElementException {
			throw new NoSuchElementException("Left value not found");
		}

		@Override
		public B coerceRight() throws NoSuchElementException {
			return right;
		}

		@Override
		public <C> Either<A, C> map(Function<B, C> f) {
			return ofRight(f.apply(right));
		}

		@Override
		public <C> Either<A, C> applyMap(Either<A, Function<B, C>> f) {
			return f.flatMap(this::map);
		}

		@Override
		public <C> Either<A, C> flatMap(Function<B, Either<A, C>> f) {
			return f.apply(right);
		}

		@Override
		public <R> R caseOf(Left.Case<A, B, R> caseLeft, Right.Case<A, B, R> caseRight) {
			return caseRight.caseRight(right);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			Right<?, ?> right1 = (Right<?, ?>) o;
			return Objects.equals(right, right1.right);
		}

		@Override
		public int hashCode() {
			return Objects.hash(2, right);
		}

		@Override
		public String toString() {
			return "Right{" +
				"right=" + right +
				'}';
		}

		public interface Case<A, B, R> {
			R caseRight(B right);
		}
	}

	// Type Classes
	public static <T> Functor<TC<HKTWitness, T>> functor() { return EitherMonad.getInstance(); }
	public static <T> Applicative<TC<HKTWitness, T>> applicative() { return EitherMonad.getInstance(); }
	public static <T> Monad<TC<HKTWitness, T>> monad() { return EitherMonad.getInstance(); }

	static class EitherMonad<T> implements Monad<TC<HKTWitness, T>> {
		static final Monad<? extends TC<HKTWitness, ?>> INSTANCE = new EitherMonad<>();
		@SuppressWarnings("unchecked") static <T> Monad<TC<HKTWitness, T>> getInstance() { return (Monad<TC<HKTWitness, T>>) INSTANCE; }

		@Override public <A> TC<TC<HKTWitness, T>, A> pure(A a) { return Either.ofRight(a); }
		@Override public <A, B> TC<TC<HKTWitness, T>, B> flatMap(Function<A, TC<TC<HKTWitness, T>, B>> f, TC<TC<HKTWitness, T>, A> a) { return coerceHKT(a).flatMap(x -> coerceHKT(f.apply(x))); }
	}
}
