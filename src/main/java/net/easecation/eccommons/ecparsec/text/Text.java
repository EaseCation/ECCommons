package net.easecation.eccommons.ecparsec.text;

import java.util.NoSuchElementException;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import net.easecation.eccommons.adt.Maybe;
import net.easecation.eccommons.adt.Tuple;
import net.easecation.eccommons.ecparsec.TokenStream;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class Text {
	@Value
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class Empty extends Text {
		// region Implementation
		static final Text SINGLETON = new Empty();

		public interface Case<R> { R caseEmpty(); }
		@Override public <R> R caseOf(Empty.Case<R> caseEmpty, Nonempty.Case<R> caseNonempty) { return caseEmpty.caseEmpty(); };

		@Override public boolean isEmpty() { return true; }
		@Override public boolean isNonempty() { return false; }
		@Override public char fromHead(char other) { return other; }
		@Override public Text fromTail(Text other) { return other; }
		@Override public char coerceHead() throws NoSuchElementException { throw new NoSuchElementException(); }
		@Override public Text coerceTail() throws NoSuchElementException { throw new NoSuchElementException(); }

		@Override public Text concat(Text t) { return t; }
		@Override public Maybe<Character> head() { return Maybe.ofNothing(); }
		@Override public Maybe<Text> tail() { return Maybe.ofNothing(); }
		@Override public Maybe<Tuple<Character, Text>> uncons() { return Maybe.ofNothing(); }
		@Override public int length() { return 0; }

		@Override public Text take(int i) { return emptyText(); }
		@Override public Text drop(int i) { return emptyText(); }
		@Override public Tuple<Text, Text> splitAt(int i) { return Tuple.of(emptyText(), emptyText()); }
		@Override public Text takeWhile(Predicate<Character> p) { return emptyText(); }
		@Override public Text dropWhile(Predicate<Character> p) { return emptyText(); }
		@Override public Tuple<Text, Text> span(Predicate<Character> p) { return Tuple.of(emptyText(), emptyText()); }

		@Override public boolean isPrefixOf(Text t) { return true; }
		@Override public boolean isSuffixOf(Text t) { return true; }

		@Override public Maybe<Character> index(int i) { return Maybe.ofNothing(); }
		@Override public int count(Predicate<Character> p) { return 0; }

		@Override public String toString() { return ""; }
		@Override public boolean equals(Object x) { return x instanceof Empty; }
		@Override public int hashCode() { return "".hashCode(); }
		// endregion
	}
	@Value
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class Nonempty extends Text {
		String s;
		int offset;

		// region Implementation
		public interface Case<R> { R caseNonempty(char head, Text tail); }
		@Override public <R> R caseOf(Empty.Case<R> caseEmpty, Nonempty.Case<R> caseNonempty) { return caseNonempty.caseNonempty(s.charAt(offset), text(s, offset + 1)); };

		@Override public boolean isEmpty() { return false; }
		@Override public boolean isNonempty() { return true; }
		@Override public char fromHead(char other) { return s.charAt(offset); }
		@Override public Text fromTail(Text other) { return text(s, offset + 1); }
		@Override public char coerceHead() throws NoSuchElementException { return s.charAt(offset); }
		@Override public Text coerceTail() throws NoSuchElementException { return text(s, offset + 1); }

		@Override public Text concat(Text t) { return text(toString() + t.toString()); }
		@Override public Maybe<Character> head() { return Maybe.ofJust(s.charAt(offset)); }
		@Override public Maybe<Text> tail() { return Maybe.ofJust(text(s, offset + 1)); }
		@Override public Maybe<Tuple<Character, Text>> uncons() { return Maybe.ofJust(Tuple.of(s.charAt(offset), text(s, offset + 1))); }
		@Override public int length() { return s.length() - offset; }

		@Override public Text take(int i) { return text(s.substring(offset, Math.min(offset + Math.max(i, 0), s.length()))); }
		@Override public Text drop(int i) { return text(s, offset + Math.max(i, 0)); }
		@Override public Tuple<Text, Text> splitAt(int i) { return Tuple.of(take(i), drop(i)); }
		@Override public Text takeWhile(Predicate<Character> p) { return text(s.substring(offset, offset + count(p))); }
		@Override public Text dropWhile(Predicate<Character> p) { return text(s, offset + count(p)); }
		@Override public Tuple<Text, Text> span(Predicate<Character> p) { return Tuple.of(takeWhile(p), dropWhile(p)); }

		@Override public boolean isPrefixOf(Text t) { return t.toString().regionMatches(0, s, offset, length()); }
		@Override public boolean isSuffixOf(Text t) { return t.toString().regionMatches(t.length() - length(), s, offset, length()); }

		@Override public Maybe<Character> index(int i) { return i < 0 || i >= length() ? Maybe.ofNothing() : Maybe.ofJust(s.charAt(offset + i)); }
		@Override public int count(Predicate<Character> p) { int c; for (c = 0; offset + c < s.length() && p.test(s.charAt(offset + c)); c++); return c; }

		@Override public String toString() { return s.substring(offset); }
		@Override public boolean equals(Object x) { return x instanceof Nonempty && ((Nonempty) x).length() == length() && ((Nonempty) x).s.regionMatches(((Nonempty) x).offset, s, offset, length()); }
		@Override public int hashCode() { return toString().hashCode(); }
		// endregion
	}

	public static Text emptyText() { return Empty.SINGLETON; }
	public static Text nonemptyText(char head, Text tail) { return new Nonempty(head + tail.toString(), 0); }
	public static Text text(String s) { return text(s, 0); }
	public static Text text(String s, int offset) { return Math.max(offset, 0) >= s.length() ? Empty.SINGLETON : new Nonempty(s, Math.max(offset, 0)); }

	// region Pattern matching
	public interface Match<R> extends Empty.Case<R>, Nonempty.Case<R> {}
	public final <R> R match(Match<R> match) { return caseOf(match, match); }
	public abstract <R> R caseOf(Empty.Case<R> caseEmpty, Nonempty.Case<R> caseNonempty);

	public abstract boolean isEmpty();
	public abstract boolean isNonempty();
	public abstract char fromHead(char other);
	public abstract Text fromTail(Text other);
	public abstract char coerceHead() throws NoSuchElementException;
	public abstract Text coerceTail() throws NoSuchElementException;
	// endregion

	// region Basic
	public abstract Text concat(Text t);
	public abstract Maybe<Character> head();
	public abstract Maybe<Text> tail();
	public abstract Maybe<Tuple<Character, Text>> uncons();
	public abstract int length();
	// endregion

	// region Substring
	public abstract Text take(int i);
	public abstract Text drop(int i);
	public abstract Tuple<Text, Text> splitAt(int i);
	public abstract Text takeWhile(Predicate<Character> p);
	public abstract Text dropWhile(Predicate<Character> p);
	public abstract Tuple<Text, Text> span(Predicate<Character> p);
	// endregion

	// region Predicate
	public abstract boolean isPrefixOf(Text t);
	public abstract boolean isSuffixOf(Text t);
	// endregion

	// region Indexing
	public abstract Maybe<Character> index(int i);
	public abstract int count(Predicate<Character> p);
	// endregion

	@Override public abstract String toString();
	@Override public abstract boolean equals(Object x);
	@Override public abstract int hashCode();

	public static TokenStream<Text, Character> tokenStream() { return TextTokenStream.INSTANCE; }
}
