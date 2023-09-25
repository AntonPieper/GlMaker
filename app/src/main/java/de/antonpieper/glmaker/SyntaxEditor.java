package de.antonpieper.glmaker;

public class SyntaxEditor /*extends SourceCodeEditor*/ {
	/*public SyntaxHighlighter syntaxHighlighter;
	public SyntaxEditor(final Context context) {
		super(context);
		init();
	}
	public SyntaxEditor(final Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	public SyntaxEditor(final Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	private void init() {
		addTextChangedListener(new SyntaxHighlighter(this, new GLSLRuleSet()));
	}
	public static class SyntaxHighlighter implements TextWatcher {
		final StringBuilder functionNames = new StringBuilder();
		final Map<Scope, StringBuilder> variableNames = new HashMap<>();
		final Map<Scope, StringBuilder> constantNames = new HashMap<>();
		final StringBuilder structNames = new StringBuilder();

		final List<Integer> tabPos = new ArrayList<>();
		
		final EditText et;
		final Rule ruleSet;
		
		public SyntaxHighlighter(final EditText et, final Rule ruleSet) {
			this.et = et;
			this.ruleSet = ruleSet;
		}
		
		@Override public void afterTextChanged(final Editable e) {
			final int bigChar=(int)et.getPaint().measureText("m");
			final String src = e.toString();
			for (final int i : tabPos) {
				e.setSpan(new ReplacementSpan() {
						@Override public void draw(Canvas p1, CharSequence p2, int p3, int p4, float p5, int p6, int p7, int p8, Paint paint) { }
						@Override public int getSize(Paint paint, CharSequence s, int start, int end, Paint.FontMetricsInt fm) {
							final String src = s.toString();
							final int lastNewLine = src.lastIndexOf('\n', start - 1);
							final int lastTab = src.lastIndexOf('\t', start - 1);
							final int last = Math.max(lastNewLine, lastTab);
							if (last < 0) return bigChar * 4;
							final int dist = 4 - ((start - last - 1) % 4);
							return bigChar * dist;
						}
					}, i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			removeSpans(e, ForegroundColorSpan.class);

			functionNames.setLength(0);
			variableNames.clear();
			constantNames.clear();
			structNames.setLength(0);

			final Pattern structDeclPattern = Pattern.compile(ruleSet.getStructDeclarePattern());
			Grammar.getAllOccurences(structDeclPattern.matcher(e), structNames);

			final StringBuilder types = Grammar.getTypes(ruleSet.getPrimitiveTypes(), structNames);
			final Pattern typesPattern = Pattern.compile(Grammar.createWordMap(types));

			final Pattern varDeclPattern = Pattern.compile(Grammar.addTypes(ruleSet.getVariableDeclarePattern(), types));
			Grammar.createScopeMap(varDeclPattern, src, variableNames);

			final Pattern constDeclPattern = Pattern.compile(Grammar.addTypes(ruleSet.getConstantDeclarePattern(), types));
			Grammar.createScopeMap(constDeclPattern, src, constantNames);

			final Pattern fnDeclPattern = Pattern.compile(Grammar.addTypes(ruleSet.getFunctionDeclarationPattern(), types));
			Grammar.getAllOccurences(fnDeclPattern.matcher(e), functionNames);

			final Pattern fnPattern = Pattern.compile(Grammar.createWordMap(functionNames));
			final Pattern structPattern = Pattern.compile(Grammar.createWordMap(structNames));

			for (final Matcher m = Grammar.globalMatcher(Pattern.compile(ruleSet.getCommentPattern()), e); m.find();)
				e.setSpan(new ForegroundColorSpan(Theme.commentsColor), m.start(1), m.end(1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			for (final Matcher m = Grammar.globalMatcher(Pattern.compile(ruleSet.getBooleanPattern()), e); m.find();)
				e.setSpan(new ForegroundColorSpan(Theme.booleanColor), m.start(1), m.end(1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			for (final Matcher m = Grammar.globalMatcher(Pattern.compile(ruleSet.getNumericConstantPattern()), e); m.find();)
				e.setSpan(new ForegroundColorSpan(Theme.numericConstantColor), m.start(1), m.end(1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			for (final Matcher m = Grammar.globalMatcher(Pattern.compile(ruleSet.getPreprocessorPattern()), e); m.find();)
				e.setSpan(new ForegroundColorSpan(Theme.preprocessorColor), m.start(1), m.end(1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			for (final Matcher m = Grammar.globalMatcher(Pattern.compile(ruleSet.getPreprocessorStringPattern()), e); m.find();)
				e.setSpan(new ForegroundColorSpan(Theme.preprocessorStringColor), m.start(1), m.end(1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			for (final Matcher m = Grammar.globalMatcher(Pattern.compile(ruleSet.getPreprocessorNumericPattern()), e); m.find();)
				e.setSpan(new ForegroundColorSpan(Theme.preprocessorNumericColor), m.start(1), m.end(1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			for (final Matcher m = Grammar.globalMatcher(Pattern.compile(ruleSet.getStoragePattern()), e); m.find();)
				e.setSpan(new ForegroundColorSpan(Theme.storageColor), m.start(1), m.end(1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			for (final Matcher m = Grammar.globalMatcher(Pattern.compile(ruleSet.getStorageTypePattern()), e); m.find();)
				e.setSpan(new ForegroundColorSpan(Theme.storageTypeColor), m.start(1), m.end(1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			for (final Matcher m = Grammar.globalMatcher(Pattern.compile(ruleSet.getStorageModifierPattern()), e); m.find();)
				e.setSpan(new ForegroundColorSpan(Theme.storageModifierColor), m.start(1), m.end(1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			for (final Matcher m = Grammar.globalMatcher(Pattern.compile(ruleSet.getKeywordPattern()), e); m.find();)
				e.setSpan(new ForegroundColorSpan(Theme.keywordColor), m.start(1), m.end(1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			for (final Matcher m = Grammar.globalMatcher(Pattern.compile(ruleSet.getKeywordOperatorPattern()), e); m.find();)
				e.setSpan(new ForegroundColorSpan(Theme.keywordOperatorColor), m.start(1), m.end(1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			for (final Matcher m = Grammar.globalMatcher(Pattern.compile(ruleSet.getUnitPattern()), e); m.find();)
				e.setSpan(new ForegroundColorSpan(Theme.unitColor), m.start(1), m.end(1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			for (final Matcher m = Grammar.globalMatcher(Pattern.compile(ruleSet.getLanguageVariablePattern()), e); m.find();)
				e.setSpan(new ForegroundColorSpan(Theme.languageVariableColor), m.start(1), m.end(1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			for (final Matcher m = Grammar.globalMatcher(typesPattern, e); m.find();)
				e.setSpan(new ForegroundColorSpan(Theme.typesColor), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			for (final Matcher m = Grammar.globalMatcher(fnPattern, e); m.find();)
				e.setSpan(new ForegroundColorSpan(Theme.functionsColor), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

			for (final Matcher m = Grammar.globalMatcher(Pattern.compile(ruleSet.getControlFlowPattern()), e); m.find();)
				e.setSpan(new ForegroundColorSpan(Theme.controlFlowColor), m.start(1), m.end(1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

			for (final Map.Entry<Scope, StringBuilder> variableName : variableNames.entrySet()) {
				for (final Matcher m = Grammar.scopedMatcher(variableName, e); m.find();)
					e.setSpan(new ForegroundColorSpan(Theme.variablesColor), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			for (final Map.Entry<Scope, StringBuilder> constantName : constantNames.entrySet()) {
				for (final Matcher m = Grammar.scopedMatcher(constantName, e); m.find();)
					e.setSpan(new ForegroundColorSpan(Theme.constantsColor), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			for (final Matcher m = Grammar.globalMatcher(structPattern, e); m.find();)
				e.setSpan(new ForegroundColorSpan(Theme.structsColor), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

			for (final Matcher m = Grammar.globalMatcher(Pattern.compile(ruleSet.getFunctionPattern()), e); m.find();)
				e.setSpan(new ForegroundColorSpan(Theme.functionsColor), m.start(1), m.end(1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		}
		@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
		@Override public void onTextChanged(final CharSequence s, int start, int before, int count) {
			tabPos.clear();
			final String edited =s.subSequence(start, start + count).toString();
			for (int i = 0; i < count; i++)
				if (edited.charAt(i) == '\t') tabPos.add(start + i);
		}
		
		private void removeSpans(final Editable e, final Class<? extends CharacterStyle> type) {
			final CharacterStyle[] spans = e.getSpans(0, e.length(), type);
			for (CharacterStyle span : spans) e.removeSpan(span);
		}
	}*/
}
