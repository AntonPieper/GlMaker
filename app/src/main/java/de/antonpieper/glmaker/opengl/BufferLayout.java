package de.antonpieper.glmaker.opengl;

import androidx.annotation.NonNull;

public class BufferLayout {
    public @NonNull BufferElement[] elements;
    private int stride;

    public BufferLayout(@NonNull BufferElement... elements) {
        this.elements = elements;
        calculateOffsetsAndStride();
    }

    public int getStride() {
        return stride;
    }

    private void calculateOffsetsAndStride() {
        int offset = 0;
        stride = 0;
        for (BufferElement element : elements) {
            element.offset = offset;
            offset += element.size;
            stride += element.size;
        }
    }
}
