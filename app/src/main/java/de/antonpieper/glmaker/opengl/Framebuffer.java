package de.antonpieper.glmaker.opengl;


import android.opengl.GLES32;

import androidx.annotation.NonNull;

public class Framebuffer implements AutoCloseable {
    private final int rendererId;
    private final int width;
    private final int height;
    private final @NonNull Texture texture;

    public Framebuffer(int width, int height) {
        this.width = width;
        this.height = height;

        // Create and bind the framebuffer.
        int[] framebufferHandles = new int[1];
        GLES32.glGenFramebuffers(1, framebufferHandles, 0);
        rendererId = framebufferHandles[0];
        GLES32.glBindFramebuffer(GLES32.GL_FRAMEBUFFER, rendererId);

        // Create a texture attachment for the framebuffer.
        texture = new Texture();
        // Set texture parameters (optional).
        texture.setFilter(Texture.Filter.LINEAR, Texture.Filter.LINEAR, Texture.Wrap.CLAMP, Texture.Wrap.CLAMP);

        // Allocate storage for the texture.
        texture.allocate(width, height);

        // Attach the texture to the framebuffer.
        GLES32.glFramebufferTexture2D(GLES32.GL_FRAMEBUFFER, GLES32.GL_COLOR_ATTACHMENT0, GLES32.GL_TEXTURE_2D, texture.getRendererID(), 0);

        // Check for framebuffer completeness.
        int status = GLES32.glCheckFramebufferStatus(GLES32.GL_FRAMEBUFFER);
        if (status != GLES32.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer is not complete: " + Integer.toHexString(status));
        }

        // Unbind the framebuffer.
        GLES32.glBindFramebuffer(GLES32.GL_FRAMEBUFFER, 0);
    }

    public void bind() {
        GLES32.glBindFramebuffer(GLES32.GL_FRAMEBUFFER, rendererId);
    }

    public void unbind() {
        GLES32.glBindFramebuffer(GLES32.GL_FRAMEBUFFER, 0);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @NonNull
    public Texture getTexture() {
        return texture;
    }

    public void close() {
        texture.close();
        GLES32.glDeleteFramebuffers(1, new int[]{rendererId}, 0);
    }
}

