package de.antonpieper.glmaker.highlighter;

import androidx.annotation.NonNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SemanticEngine {
	private final @NonNull List<Token> tokens;
	private final Deque<Scope> scopes = new ArrayDeque<>();
	public SemanticEngine(@NonNull List<Token> tokens) {
		this.tokens = tokens;
	}
	private static class Scope {
		private final Set<Identifier> variables = new HashSet<>();
		private final Set<Identifier> types = new HashSet<>();
	}
}
