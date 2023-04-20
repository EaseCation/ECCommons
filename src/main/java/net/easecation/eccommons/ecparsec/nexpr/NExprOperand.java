package net.easecation.eccommons.ecparsec.nexpr;

import java.util.NoSuchElementException;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.With;
import net.easecation.eccommons.adt.Maybe;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class NExprOperand<N, T> {
	@Value
	@With
	@EqualsAndHashCode(callSuper = false)
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class Node<N, T> extends NExprOperand<N, T> {
		N notation;
		Function<T, NExprOperand<N, T>> next;

		// region Pattern matching
		public interface Case<N, T, R> { R caseNode(Node<N, T> node); }
		@Override public <R> R caseOf(Node.Case<N, T, R> caseNode, Tail.Case<N, T, R> caseTail) { return caseNode.caseNode(this); }

		@Override public boolean isNode() { return true; }
		@Override public boolean isTail() { return false; }
		@Override public Maybe<Node<N, T>> getNode() { return Maybe.ofJust(this); }
		@Override public Maybe<Tail<N, T>> getTail() { return Maybe.ofNothing(); }
		@Override public Node<N, T> coerceNode() throws NoSuchElementException { return this; }
		@Override public Tail<N, T> coerceTail() throws NoSuchElementException { throw new NoSuchElementException(); }
		// endregion
	}

	@Value
	@With
	@EqualsAndHashCode(callSuper = false)
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class Tail<N, T> extends NExprOperand<N, T> {
		Function<T, T> next;

		// region Pattern matching
		public interface Case<N, T, R> { R caseTail(Tail<N, T> tail); }
		@Override public <R> R caseOf(Node.Case<N, T, R> caseNode, Tail.Case<N, T, R> caseTail) { return caseTail.caseTail(this); }

		@Override public boolean isNode() { return false; }
		@Override public boolean isTail() { return true; }
		@Override public Maybe<Node<N, T>> getNode() { return Maybe.ofNothing(); }
		@Override public Maybe<Tail<N, T>> getTail() { return Maybe.ofJust(this); }
		@Override public Node<N, T> coerceNode() throws NoSuchElementException { throw new NoSuchElementException(); }
		@Override public Tail<N, T> coerceTail() throws NoSuchElementException { return this; }
		// endregion
	}

	public static <N, T> NExprOperand<N, T> node(N notation, Function<T, NExprOperand<N, T>> next) { return new Node<>(notation, next); }
	public static <N, T> NExprOperand<N, T> tail(Function<T, T> next) { return new Tail<>(next); }

	// region Pattern matching
	public interface Match<N, T, R> extends Node.Case<N, T, R>, Tail.Case<N, T, R> {}
	public final <R> R match(Match<N, T, R> match) { return caseOf(match, match); }
	public abstract <R> R caseOf(Node.Case<N, T, R> caseNode, Tail.Case<N, T, R> caseTail);

	public abstract boolean isNode();
	public abstract boolean isTail();
	public abstract Maybe<Node<N, T>> getNode();
	public abstract Maybe<Tail<N, T>> getTail();
	public abstract Node<N, T> coerceNode() throws NoSuchElementException;
	public abstract Tail<N, T> coerceTail() throws NoSuchElementException;
	// endregion
}
