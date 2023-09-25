package de.antonpieper.glmaker.opengl;

import androidx.annotation.NonNull;

public final class ShaderError {
    public enum Type {
        Vertex, Fragment, Program
    }

    public @NonNull Type type;
    public int line;
    public String message;

    public ShaderError(final @NonNull Type type, final int line, final String message) {
        this.type = type;
        this.line = line;
        this.message = message;
    }
}
