package de.antonpieper.glmaker.opengl;

import android.opengl.GLES32;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class VertexBuffer implements AutoCloseable {
    private final int rendererID;
    private final @NonNull BufferLayout layout;

    public VertexBuffer(@NonNull BufferLayout layout, FloatBuffer data) {
        this.layout = layout;
        int[] tmp = new int[1];
        GLES32.glGenBuffers(1, tmp, 0);
        rendererID = tmp[0];
        setData(data);
    }

    public VertexBuffer(@NonNull BufferLayout layout, ByteBuffer data) {
        this.layout = layout;
        int[] tmp = new int[1];
        GLES32.glGenBuffers(1, tmp, 0);
        rendererID = tmp[0];
        setData(data);
    }

    public void bind() {
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, rendererID);
    }

    public void unbind() {
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
    }

    public void setData(ByteBuffer data) {
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, rendererID);
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, data.remaining(), data, GLES32.GL_STATIC_DRAW);
    }

    public void setData(FloatBuffer data) {
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, rendererID);
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, data.remaining() * Float.BYTES, data, GLES32.GL_STATIC_DRAW);
    }

    public void setData(ByteBuffer data, int usage) {
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, rendererID);
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, data.remaining(), data, usage);
    }

    public void setData(FloatBuffer data, int usage) {
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, rendererID);
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, data.remaining() * Float.BYTES, data, usage);
    }

    public void updateData(ByteBuffer data) {
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, rendererID);
        GLES32.glBufferSubData(GLES32.GL_ARRAY_BUFFER, 0, data.remaining(), data);
    }

    public void updateData(FloatBuffer data) {
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, rendererID);
        GLES32.glBufferSubData(GLES32.GL_ARRAY_BUFFER, 0, data.remaining() * Float.BYTES, data);
    }

    public void updateData(ByteBuffer data, int start) {
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, rendererID);
        GLES32.glBufferSubData(GLES32.GL_ARRAY_BUFFER, start, data.remaining(), data);
    }

    public void updateData(FloatBuffer data, int start) {
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, rendererID);
        GLES32.glBufferSubData(GLES32.GL_ARRAY_BUFFER, start, data.remaining() * Float.BYTES, data);
    }

    @Override
    public void close() {
        GLES32.glDeleteBuffers(1, new int[]{rendererID}, 0);
    }

    @NonNull
    public BufferLayout getLayout() {
        return layout;
    }


}
