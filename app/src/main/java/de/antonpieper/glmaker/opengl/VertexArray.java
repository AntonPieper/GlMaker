package de.antonpieper.glmaker.opengl;

import android.opengl.GLES32;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class VertexArray implements AutoCloseable {
    private final int rendererID;
    private final List<VertexBuffer> vertexBuffers = new ArrayList<>();
    private IndexBuffer indexBuffer;

    public VertexArray(@NonNull IndexBuffer indexBuffer, VertexBuffer... vertexBuffers) {
        int[] tmp = new int[1];
        GLES32.glGenVertexArrays(1, tmp, 0);
        rendererID = tmp[0];
        addVertexBuffers(vertexBuffers);
        setIndexBuffer(indexBuffer);
    }

    public void bind() {
        GLES32.glBindVertexArray(rendererID);
    }

    public void unbind() {
        GLES32.glBindVertexArray(0);
    }

    @Override
    public void close() {
        GLES32.glDeleteVertexArrays(1, new int[]{rendererID}, 0);
    }

    public void addVertexBuffers(@NonNull VertexBuffer[] vertexBuffers) {
        for (var vertexBuffer : vertexBuffers) {
            addVertexBuffer(vertexBuffer);
        }
    }

    public void addVertexBuffer(@NonNull VertexBuffer vertexBuffer) {
        BufferLayout layout = vertexBuffer.getLayout();
        if (layout == null) throw new IllegalArgumentException("Vertex buffer has no layout!");
        GLES32.glBindVertexArray(rendererID);
        vertexBuffer.bind();
        int size = layout.elements.length;
        for (int i = 0; i < size; i++) {
            BufferElement e = vertexBuffer.getLayout().elements[i];
            GLES32.glEnableVertexAttribArray(i);
            GLES32.glVertexAttribPointer(i, e.getComponentCount(), ShaderDataType.getGLType(e.type), e.normalized, layout.getStride(), e.offset);
        }

        vertexBuffers.add(vertexBuffer);
    }

    public void setIndexBuffer(@NonNull IndexBuffer indexBuffer) {
        GLES32.glBindVertexArray(rendererID);
        indexBuffer.bind();
        this.indexBuffer = indexBuffer;
    }

    public @NonNull List<VertexBuffer> getVertexBuffers() {
        return vertexBuffers;
    }

    public @NonNull IndexBuffer getIndexBuffer() {
        return indexBuffer;
    }
}
