package de.antonpieper.glmaker.widget;

import android.content.Context;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import de.antonpieper.glmaker.highlighter.Highlight;
import de.antonpieper.glmaker.highlighter.Lexer;
import de.antonpieper.glmaker.highlighter.Token;

public class SyntaxEditor extends AppCompatEditText {
    private static final @ColorInt int[] COLORS = new int[Highlight.values().length];

    List<Token> tokens = new ArrayList<>();

    public SyntaxEditor(@NonNull Context context) {
        this(context, null);
    }

    public SyntaxEditor(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setHorizontallyScrolling(true);
        for (Highlight highlight : Highlight.values()) {
            COLORS[highlight.ordinal()] = ContextCompat.getColor(context, highlight.id());
        }
        addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable e) {
                highlight(e, false);
            }
        });
    }

    private void highlight(@Nullable Editable e, boolean complete) {
        List<Token> oldTokens = tokens;
        tokens = new ArrayList<>();
        if (e == null || e.length() == 0) {
            return;
        }
        for (Token token : new Lexer(e.toString())) {
            tokens.add(token);
        }
        if (complete) {
            clearSpans(e, 0, e.length(), ForegroundColorSpan.class);
            for (Token token : tokens) {
                e.setSpan(
                        new ForegroundColorSpan(COLORS[Highlight.from(token.type()).ordinal()]),
                        token.startOffset(), token.endOffset(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        } else {
            Lexer.Diff diff = Lexer.diff(oldTokens, tokens);
            if (diff.start() <= diff.deleteEnd()) {
                int startOffset = tokens.get(diff.start()).startOffset();
                int endOffset = tokens.get(diff.insertEnd()).endOffset();
                clearSpans(e, startOffset, endOffset, ForegroundColorSpan.class);
            }
            for (int i = diff.start(); i <= diff.insertEnd(); ++i) {
                Token token = tokens.get(i);
                e.setSpan(
                        new ForegroundColorSpan(COLORS[Highlight.from(token.type()).ordinal()]),
                        token.startOffset(), token.endOffset(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        tokens = new ArrayList<>();
        highlight(getText(), false);
    }

    private static <T> void clearSpans(Spannable e, int start, int end, Class<T> clazz) {
        // Remove foreground color spans.
        T[] spans = e.getSpans(start, end, clazz);
        for (T span : spans) {
            e.removeSpan(span);
        }
    }
}
