package de.antonpieper.glmaker.opengl;

import android.opengl.GLES32;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class IndexBuffer implements AutoCloseable {
    private final int rendererID;
    private final int count;

    public IndexBuffer(@NonNull ByteBuffer data) {
        this.count = data.remaining() / Integer.BYTES;
        int[] tmp = new int[1];
        GLES32.glGenBuffers(1, tmp, 0);
        rendererID = tmp[0];
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, rendererID);

        GLES32.glBufferData(GLES32.GL_ELEMENT_ARRAY_BUFFER, data.remaining(), data, GLES32.GL_STATIC_DRAW);
    }

    public IndexBuffer(@NonNull ByteBuffer data, int count) {
        this.count = count;
        int[] tmp = new int[1];
        GLES32.glGenBuffers(1, tmp, 0);
        rendererID = tmp[0];
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, rendererID);

        GLES32.glBufferData(GLES32.GL_ELEMENT_ARRAY_BUFFER, count * Integer.BYTES, data, GLES32.GL_STATIC_DRAW);
    }

    public IndexBuffer(@NonNull IntBuffer data) {
        this.count = data.remaining();
        int[] tmp = new int[1];
        GLES32.glGenBuffers(1, tmp, 0);
        rendererID = tmp[0];
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, rendererID);

        GLES32.glBufferData(GLES32.GL_ELEMENT_ARRAY_BUFFER, count * Integer.BYTES, data, GLES32.GL_STATIC_DRAW);
    }

    public IndexBuffer(@NonNull IntBuffer data, int count) {
        this.count = count;
        int[] tmp = new int[1];
        GLES32.glGenBuffers(1, tmp, 0);
        rendererID = tmp[0];
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, rendererID);

        GLES32.glBufferData(GLES32.GL_ELEMENT_ARRAY_BUFFER, count * Integer.BYTES, data, GLES32.GL_STATIC_DRAW);
    }

    public void bind() {
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, rendererID);
    }

    public void unbind() {
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void setData(ByteBuffer data) {
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, rendererID);
        GLES32.glBufferData(GLES32.GL_ELEMENT_ARRAY_BUFFER, data.remaining(), data, GLES32.GL_STATIC_DRAW);
    }

    public void setData(@NonNull IntBuffer data) {
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, rendererID);
        GLES32.glBufferData(GLES32.GL_ELEMENT_ARRAY_BUFFER, data.remaining() * Integer.BYTES, data, GLES32.GL_STATIC_DRAW);
    }

    public void setData(@NonNull ByteBuffer data, int usage) {
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, rendererID);
        GLES32.glBufferData(GLES32.GL_ELEMENT_ARRAY_BUFFER, data.remaining(), data, usage);
    }

    public void setData(@NonNull IntBuffer data, int usage) {
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, rendererID);
        GLES32.glBufferData(GLES32.GL_ELEMENT_ARRAY_BUFFER, data.remaining() * Integer.BYTES, data, usage);
    }

    public void updateData(@NonNull ByteBuffer data) {
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, rendererID);
        GLES32.glBufferSubData(GLES32.GL_ELEMENT_ARRAY_BUFFER, 0, data.remaining(), data);
    }

    public void updateData(@NonNull IntBuffer data) {
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, rendererID);
        GLES32.glBufferSubData(GLES32.GL_ELEMENT_ARRAY_BUFFER, 0, data.remaining() * Integer.BYTES, data);
    }

    public void updateData(@NonNull ByteBuffer data, int start) {
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, rendererID);
        GLES32.glBufferSubData(GLES32.GL_ELEMENT_ARRAY_BUFFER, start, data.remaining(), data);
    }

    public void updateData(@NonNull IntBuffer data, int start) {
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, rendererID);
        GLES32.glBufferSubData(GLES32.GL_ELEMENT_ARRAY_BUFFER, start, data.remaining() * Integer.BYTES, data);
    }

    @Override
    public void close() {
        GLES32.glDeleteBuffers(1, new int[]{rendererID}, 0);
    }

    public int getCount() {
        return count;
    }

}
