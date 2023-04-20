package net.easecation.eccommons.adt;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * ADT type with A + 1 element
 */
public abstract class Maybe<A> {
	private Maybe() {

	}

	// Term introduction
	@SuppressWarnings("unchecked")
	public static <A> Maybe<A> ofNothing() {
		return (Maybe<A>) Nothing.NOTHING;
	}

	public static <A> Maybe<A> ofJust(A value) {
		return new Just<>(value);
	}

	// Term elimination
	public abstract boolean isNothing();

	public abstract boolean isJust();

	public abstract A fromJust(A orElse);

	public abstract A coerceJust() throws NoSuchElementException;

	public abstract ConsList<A> toConsList();

	public abstract Stream<A> stream();

	// Monadic Interface
	public abstract <B> Maybe<B> map(Function<A, B> f);

	public abstract <B> Maybe<B> applyMap(Maybe<Function<A, B>> f);

	public abstract <B> Maybe<B> flatMap(Function<A, Maybe<B>> f);

	public abstract Maybe<A> filter(Predicate<A> f);

	// Pattern matching
	public abstract <R> R caseOf(Nothing.Case<A, R> caseNothing, Just.Case<A, R> caseJust);

	public final <R> R match(Match<A, R> match) {
		return caseOf(match, match);
	}

	public interface Match<A, R> extends Nothing.Case<A, R>, Just.Case<A, R> {

	}

	// Java
	@Override
	public abstract boolean equals(Object obj);

	@Override
	public abstract int hashCode();

	@Override
	public abstract String toString();

	// Constructors
	public static final class Nothing<A> extends Maybe<A> {
		private static final Nothing<?> NOTHING = new Nothing<>();

		private Nothing() {

		}

		@Override
		public boolean isJust() {
			return false;
		}

		@Override
		public boolean isNothing() {
			return true;
		}

		@Override
		public A fromJust(A orElse) {
			return orElse;
		}

		@Override
		public A coerceJust() throws NoSuchElementException {
			throw new NoSuchElementException("Just value not found");
		}

		@Override
		public ConsList<A> toConsList() {
			return ConsList.nil();
		}

		@Override
		public Stream<A> stream() {
			return Stream.empty();
		}

		@Override
		public <B> Maybe<B> map(Function<A, B> f) {
			return Maybe.ofNothing();
		}

		@Override
		public <B> Maybe<B> applyMap(Maybe<Function<A, B>> f) {
			return f.flatMap(this::map);
		}

		@Override
		public <B> Maybe<B> flatMap(Function<A, Maybe<B>> f) {
			return Maybe.ofNothing();
		}

		@Override
		public Maybe<A> filter(Predicate<A> f) {
			return this;
		}

		@Override
		public <R> R caseOf(Nothing.Case<A, R> caseNothing, Just.Case<A, R> caseJust) {
			return caseNothing.caseNothing();
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof Nothing;
		}

		@Override
		public int hashCode() {
			return Objects.hash(1);
		}

		@Override
		public String toString() {
			return "Nothing{}";
		}

		public interface Case<A, R> {
			R caseNothing();
		}
	}

	public static final class Just<A> extends Maybe<A> {
		private final A value;

		private Just(A value) {
			this.value = value;
		}

		@Override
		public boolean isJust() {
			return true;
		}

		@Override
		public boolean isNothing() {
			return false;
		}

		@Override
		public A fromJust(A orElse) {
			return value;
		}

		@Override
		public A coerceJust() throws NoSuchElementException {
			return value;
		}

		@Override
		public ConsList<A> toConsList() {
			return ConsList.singleton(value);
		}

		@Override
		public Stream<A> stream() {
			return Stream.of(value);
		}

		@Override
		public <B> Maybe<B> map(Function<A, B> f) {
			return ofJust(f.apply(value));
		}

		@Override
		public <B> Maybe<B> applyMap(Maybe<Function<A, B>> f) {
			return f.flatMap(this::map);
		}

		@Override
		public <B> Maybe<B> flatMap(Function<A, Maybe<B>> f) {
			return f.apply(value);
		}

		@Override
		public Maybe<A> filter(Predicate<A> f) {
			return f.test(value) ? this : ofNothing();
		}

		@Override
		public <R> R caseOf(Nothing.Case<A, R> caseNothing, Just.Case<A, R> caseJust) {
			return caseJust.caseJust(value);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			Just<?> just = (Just<?>) o;
			return Objects.equals(value, just.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(2, value);
		}

		@Override
		public String toString() {
			return "Just{" +
				"value=" + value +
				'}';
		}

		public interface Case<A, R> {
			R caseJust(A value);
		}
	}
}
