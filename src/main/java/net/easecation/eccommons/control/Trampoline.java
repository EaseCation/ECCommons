package net.easecation.eccommons.control;

import java.util.function.Function;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.easecation.eccommons.adt.Either;
import net.easecation.eccommons.adt.Maybe;
import net.easecation.eccommons.adt.Unit;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class Trampoline<A> {
	@Value
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	@EqualsAndHashCode(callSuper = false)
	static class Done<A> extends Trampoline<A> {
		A a;

		interface Case<A, R> { R caseDone(Done<A> done); }
		@Override <R> R caseOf(Done.Case<A, R> caseDone, More.Case<A, R> caseMore, FlatMap.Case<A, R> caseFlatMap) { return caseDone.caseDone(this); }
	}
	@Value
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	@EqualsAndHashCode(callSuper = false)
	static class More<A> extends Trampoline<A> {
		Supplier<Trampoline<A>> ka;

		interface Case<A, R> { R caseMore(More<A> more); }
		@Override <R> R caseOf(Done.Case<A, R> caseDone, More.Case<A, R> caseMore, FlatMap.Case<A, R> caseFlatMap) { return caseMore.caseMore(this); }
	}
	@Value
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	@EqualsAndHashCode(callSuper = false)
	static class FlatMap<X, A> extends Trampoline<A> {
		Trampoline<X> tx;
		Function<X, Trampoline<A>> ka;

		interface Case<A, R> { <X> R caseFlatMap(FlatMap<X, A> flatMap); }
		@Override <R> R caseOf(Done.Case<A, R> caseDone, More.Case<A, R> caseMore, FlatMap.Case<A, R> caseFlatMap) { return caseFlatMap.caseFlatMap(this); }
	}

	interface Match<A, R> extends Done.Case<A, R>, More.Case<A, R>, FlatMap.Case<A, R> {}
	final <R> R match(Match<A, R> match) { return caseOf(match, match, match); }
	abstract <R> R caseOf(Done.Case<A, R> caseDone, More.Case<A, R> caseMore, FlatMap.Case<A, R> caseFlatMap);

	public static <A> Trampoline<A> done(A a) { return new Done<>(a); }
	public static <A> Trampoline<A> more(Supplier<Trampoline<A>> ka) { return new More<>(ka); }

	public final <B> Trampoline<B> map(Function<A, B> f) {
		return this.match(new Match<A, Trampoline<B>>() {
			@Override public Trampoline<B> caseDone(Done<A> done) {
				return new FlatMap<>(done, f.andThen(Done::new));
			}
			@Override public Trampoline<B> caseMore(More<A> more) {
				return new FlatMap<>(more, f.andThen(Done::new));
			}
			@Override public <X> Trampoline<B> caseFlatMap(FlatMap<X, A> flatMap) {
				return new FlatMap<>(flatMap.tx, x -> flatMap.ka.apply(x).map(f));
			}
		});
	}
	public final <B> Trampoline<B> applyMap(Trampoline<Function<A, B>> fab) {
		return fab.match(new Match<Function<A, B>, Trampoline<B>>() {
			@Override public Trampoline<B> caseDone(Done<Function<A, B>> done) {
				return new FlatMap<>(fab, Trampoline.this::map);
			}
			@Override public Trampoline<B> caseMore(More<Function<A, B>> more) {
				return new FlatMap<>(fab, Trampoline.this::map);
			}
			@Override public <X> Trampoline<B> caseFlatMap(FlatMap<X, Function<A, B>> flatMap) {
				return new FlatMap<>(flatMap.tx, x -> flatMap.ka.apply(x).flatMap(Trampoline.this::map));
			}
		});
	}
	public final <B> Trampoline<B> flatMap(Function<A, Trampoline<B>> f) {
		return this.match(new Match<A, Trampoline<B>>() {
			@Override public Trampoline<B> caseDone(Done<A> done) {
				return new FlatMap<>(done, f);
			}
			@Override public Trampoline<B> caseMore(More<A> more) {
				return new FlatMap<>(more, f);
			}
			@Override public <X> Trampoline<B> caseFlatMap(FlatMap<X, A> flatMap) {
				return new FlatMap<>(flatMap.tx, x -> flatMap.ka.apply(x).flatMap(f));
			}
		});
	}

	public static <A> Trampoline<A> pure(A a) { return done(a); }
	public static <A, B> Trampoline<B> replace(Trampoline<A> fa, B b) { return fa.map(a -> b); }
	public static <A> Trampoline<Unit> discard(Trampoline<A> fa) { return fa.map(a -> Unit.UNIT); }

	final <T extends Throwable> Either<Supplier<Trampoline<A>>, A> resume(Maybe<Thrower<T>> interrupter) throws T {
		Either<Trampoline<A>, Either<Supplier<Trampoline<A>>, A>> tco = Either.ofLeft(this);
		while (tco.isLeft()) tco = interrupter.isJust() && Thread.interrupted()
			? interrupter.coerceJust().apply()
			: tco.coerceLeft().match(new Match<A, Either<Trampoline<A>, Either<Supplier<Trampoline<A>>, A>>>() {
				@Override public Either<Trampoline<A>, Either<Supplier<Trampoline<A>>, A>> caseDone(Done<A> done) {
					return Either.ofRight(Either.ofRight(done.a));
				}
				@Override public Either<Trampoline<A>, Either<Supplier<Trampoline<A>>, A>> caseMore(More<A> more) {
					return Either.ofRight(Either.ofLeft(more.ka));
				}
				@Override public <X> Either<Trampoline<A>, Either<Supplier<Trampoline<A>>, A>> caseFlatMap(FlatMap<X, A> flatMap) {
					return flatMap.tx.match(new Match<X, Either<Trampoline<A>, Either<Supplier<Trampoline<A>>, A>>>() {
						@Override public Either<Trampoline<A>, Either<Supplier<Trampoline<A>>, A>> caseDone(Done<X> done) {
							return Either.ofLeft(flatMap.ka.apply(done.a));
						}
						@Override public Either<Trampoline<A>, Either<Supplier<Trampoline<A>>, A>> caseMore(More<X> more) {
							return Either.ofRight(Either.ofLeft(() -> more.ka.get().flatMap(flatMap.ka)));
						}
						@Override public <Y> Either<Trampoline<A>, Either<Supplier<Trampoline<A>>, A>> caseFlatMap(FlatMap<Y, X> flatMap1) {
							return Either.ofLeft(flatMap1.tx.flatMap(y -> flatMap1.ka.apply(y).flatMap(flatMap.ka)));
						}
					});
				}
			});
		return tco.coerceRight();
	}
	final <T extends Throwable> A run(Maybe<Thrower<T>> interrupter) throws T {
		Either<Trampoline<A>, A> tco = Either.ofLeft(this);
		while (tco.isLeft()) tco = interrupter.isJust() && Thread.interrupted()
			? interrupter.coerceJust().apply()
			: tco.coerceLeft().resume(interrupter).caseOf(
				ka -> Either.ofLeft(ka.get()),
				Either::ofRight
			);
		return tco.coerceRight();
	}

	public final A run() { return run(Maybe.ofNothing()); }
	public final A runInterruptibly() throws InterruptedException { return run(Maybe.ofJust(Thrower.of(InterruptedException::new))); }
}
