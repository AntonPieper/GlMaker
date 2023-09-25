package de.antonpieper.glmaker.highlighter;

import androidx.annotation.NonNull;

public final class Identifier {
    @NonNull
    public String source() {
        return source;
    }

    public int start() {
        return start;
    }

    public int length() {
        return length;
    }

    private final @NonNull String source;
    private final int start;
    private final int length;
    public Identifier(@NonNull String source, int start, int length) {
        this.source = source;
        this.start = start;
        this.length = length;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Identifier.class != o.getClass()) return false;

        Identifier that = (Identifier) o;

        if (length != that.length) return false;
        int end = start + length;
        int thatEnd = that.start + that.length;
        for (
                int i = start, j = that.start;
                i < end && j < thatEnd;
                i = CharIterator.nextC(i, source), j = CharIterator.nextC(j, that.source)
        ) {
            if (CharIterator.ch(i, source) != CharIterator.ch(j, that.source)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = length;
        for (int i = start, end = start + result; i < end; i = CharIterator.nextC(i, source)) {
            result = 31 * result + CharIterator.ch(i, source);
        }
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = start, end = start + length; i < end; i = CharIterator.nextC(i, source)) {
            builder.append(CharIterator.ch(i, source));
        }
        return builder.toString();
    }
}
