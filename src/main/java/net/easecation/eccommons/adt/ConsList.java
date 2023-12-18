package net.easecation.eccommons.adt;

import net.easecation.eccommons.control.Applicative;
import net.easecation.eccommons.control.Functor;
import net.easecation.eccommons.control.Monad;
import net.easecation.eccommons.hkt.TC;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;

public abstract class ConsList<A> implements Iterable<A>, TC<ConsList.HKTWitness, A> {
	public static final class Nil<A> extends ConsList<A> {
		Nil() {}

		public interface Case<A, R> { R caseNil(); }
		@Override public <R> R caseOf(Nil.Case<A, R> caseNil, Cons.Case<A, R> caseCons) { return caseNil.caseNil(); }

		@Override public boolean isNil() { return true; }
		@Override public boolean isCons() { return false; }
		@Override public A coerceHead() throws NoSuchElementException { throw new NoSuchElementException("Head not found"); }
		@Override public ConsList<A> coerceTail() throws NoSuchElementException { throw new NoSuchElementException("Tail not found"); }

		@Override public int length() { return 0; }
		@Override public ConsList<A> filter(Predicate<A> p) { return nil(); }
		@Override public boolean any(Predicate<A> p) { return false; }
		@Override public boolean all(Predicate<A> p) { return false; }
		@Override public <B> B foldl(BiFunction<B, A, B> f, B b) { return b; }
		@Override public <B> B foldr(BiFunction<A, B, B> f, B b) { return b; }
		@Override public ConsList<A> concat(ConsList<A> list) { return list; }

		@Override public <B> ConsList<B> map(Function<A, B> f) { return nil(); }
		@Override public <B> ConsList<B> applyMap(ConsList<Function<A, B>> fab) { return nil(); }
		@Override public <B> ConsList<B> flatMap(Function<A, ConsList<B>> f) { return nil(); }
		@Override public ConsList<A> plus(ConsList<A> fa) { return fa; }

		@Override public boolean equals(Object x) { return x instanceof Nil; }
		@Override public int hashCode() { return Objects.hash(1); }
	}
	public static final class Cons<A> extends ConsList<A> {
		final A head;
		final ConsList<A> tail;
		final int length;

		Cons(A head, ConsList<A> tail) { this.head = head; this.tail = tail; this.length = tail.length() + 1; }

		public interface Case<A, R> { R caseCons(A head, ConsList<A> tail); }
		@Override public <R> R caseOf(Nil.Case<A, R> caseNil, Cons.Case<A, R> caseCons) { return caseCons.caseCons(head, tail); }

		@Override public boolean isNil() { return false; }
		@Override public boolean isCons() { return true; }
		@Override public A coerceHead() throws NoSuchElementException { return head; }
		@Override public ConsList<A> coerceTail() throws NoSuchElementException { return tail; }

		@Override public int length() { return length; }
		@Override public ConsList<A> filter(Predicate<A> p) { return p.test(head) ? cons(head, tail.filter(p)) : tail.filter(p); }
		@Override public boolean any(Predicate<A> p) { return p.test(head) || tail.any(p); }
		@Override public boolean all(Predicate<A> p) { return p.test(head) && tail.all(p); }
		@Override public <B> B foldl(BiFunction<B, A, B> f, B b) { return tail.foldl(f, f.apply(b, head)); }
		@Override public <B> B foldr(BiFunction<A, B, B> f, B b) { return f.apply(head, tail.foldr(f, b)); }
		@Override public ConsList<A> concat(ConsList<A> list) { return cons(head, tail.concat(list)); }

		@Override public <B> ConsList<B> map(Function<A, B> f) { return cons(f.apply(head), tail.map(f)); }
		@Override public <B> ConsList<B> applyMap(ConsList<Function<A, B>> fab) { return fab.flatMap(this::map); }
		@Override public <B> ConsList<B> flatMap(Function<A, ConsList<B>> f) { return f.apply(head).concat(tail.flatMap(f)); }
		@Override public ConsList<A> plus(ConsList<A> fa) { return concat(fa); }

		@Override public boolean equals(Object x) { return x instanceof Cons && Objects.equals(head, ((Cons<?>) x).head) && Objects.equals(tail, ((Cons<?>) x).tail); }
		@Override public int hashCode() { return Objects.hash(2, head, tail); }
	}

	ConsList() {}

	public enum HKTWitness {}
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <A> Equality<TC<HKTWitness, A>, ConsList<A>> reflHKT() { return (Equality) Equality.ofRefl(); }
	public static <A> ConsList<A> coerceHKT(TC<HKTWitness, A> hkt) { return (ConsList<A>) hkt; }

	public interface Match<A, R> extends Nil.Case<A, R>, Cons.Case<A, R> {}
	public final <R> R match(Match<A, R> match) { return caseOf(match, match); }
	public abstract <R> R caseOf(Nil.Case<A, R> caseNil, Cons.Case<A, R> caseCons);

	public static <A> ConsList<A> nil() { return new Nil<>(); }
	public static <A> ConsList<A> cons(A head, ConsList<A> tail) { return new Cons<>(head, tail); }
	public static <A> ConsList<A> singleton(A a) { return cons(a, nil()); }
	@SafeVarargs public static <A> ConsList<A> list(A... as) {
		ConsList<A> list = nil();
		for (int i = as.length - 1; i >= 0; i--)
			list = cons(as[i], list);
		return list;
	}
	public static <A> ConsList<A> list(A[] as, int from, int to) throws IndexOutOfBoundsException, IllegalArgumentException {
		if (from < 0 || from > as.length)
			throw new IndexOutOfBoundsException();
		if (from > to)
			throw new IllegalArgumentException();
		ConsList<A> list = nil();
		for (int i = to - 1; i >= from; i--)
			list = cons(as[i], list);
		return list;
	}

	public abstract boolean isNil();
	public abstract boolean isCons();
	public abstract A coerceHead() throws NoSuchElementException;
	public abstract ConsList<A> coerceTail() throws NoSuchElementException;

	public abstract int length();
	public abstract ConsList<A> filter(Predicate<A> p);
	public abstract boolean any(Predicate<A> p);
	public abstract boolean all(Predicate<A> p);
	public abstract <B> B foldl(BiFunction<B, A, B> f, B b);
	public abstract <B> B foldr(BiFunction<A, B, B> f, B b);
	public abstract ConsList<A> concat(ConsList<A> list);

	public abstract <B> ConsList<B> map(Function<A, B> f);
	public abstract <B> ConsList<B> applyMap(ConsList<Function<A, B>> fab);
	public abstract <B> ConsList<B> flatMap(Function<A, ConsList<B>> f);
	public abstract ConsList<A> plus(ConsList<A> fa);

	public static <A> ConsList<A> pure(A a) { return singleton(a); }
	public static <A> ConsList<A> empty() { return nil(); }
	public static <A> ConsList<Maybe<A>> optional(ConsList<A> fa) { return fa.map(Maybe::ofJust).plus(pure(Maybe.ofNothing())); }
	public static <A, B> ConsList<B> replace(ConsList<A> fa, B b) { return fa.map(a -> b); }
	public static <A> ConsList<Unit> discard(ConsList<A> fa) { return fa.map(a -> Unit.UNIT); }

	static final class ListIterator<A> implements Iterator<A> {
		ConsList<A> current;
		ListIterator(ConsList<A> current) { this.current = current; }
		@Override public boolean hasNext() { return current.isCons(); }
		@Override public A next() {
			if (current.isNil()) throw new NoSuchElementException();
			A next = current.coerceHead();
			current = current.coerceTail();
			return next;
		}
	}
	@Nonnull @Override public final Iterator<A> iterator() { return new ListIterator<>(this); }
	@Override public Spliterator<A> spliterator() { return Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED | Spliterator.IMMUTABLE); }
	public final Stream<A> stream() { return StreamSupport.stream(spliterator(), false); }

	@Override public abstract boolean equals(Object x);
	@Override public abstract int hashCode();

	// Type Classes
	public static Functor<HKTWitness> functor() { return ConsListMonad.getInstance(); }
	public static Applicative<HKTWitness> applicative() { return ConsListMonad.getInstance(); }
	public static Monad<HKTWitness> monad() { return ConsListMonad.getInstance(); }

	static class ConsListMonad implements Monad<HKTWitness> {
		static final Monad<HKTWitness> INSTANCE = new ConsListMonad();
		static Monad<HKTWitness> getInstance() { return INSTANCE; }

		@Override public <A> TC<HKTWitness, A> pure(A a) { return singleton(a); }
		@Override public <A, B> TC<HKTWitness, B> flatMap(Function<A, TC<HKTWitness, B>> f, TC<HKTWitness, A> a) { return coerceHKT(a).flatMap(x -> coerceHKT(f.apply(x))); }
	}
}
