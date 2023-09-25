package de.antonpieper.glmaker.opengl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES32;
import android.opengl.GLUtils;

import androidx.annotation.NonNull;

public class Texture implements AutoCloseable {
    public enum Filter {
        LINEAR(GLES32.GL_LINEAR),
        NEAREST(GLES32.GL_NEAREST);
        private final int id;

        Filter(int id) {
            this.id = id;
        }
    }

    public enum Wrap {
        CLAMP(GLES32.GL_CLAMP_TO_EDGE),
        REPEAT(GLES32.GL_REPEAT);
        private final int id;

        Wrap(int id) {
            this.id = id;
        }
    }

    private int rendererID;

    public Texture() {
        // Generate a texture ID.
        int[] textureHandles = new int[1];
        GLES32.glGenTextures(1, textureHandles, 0);
        if (textureHandles[0] != 0) {
            rendererID = textureHandles[0];
            GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, rendererID);

            // Set texture parameters (optional).
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_LINEAR);
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR);
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_S, GLES32.GL_CLAMP_TO_EDGE);
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_T, GLES32.GL_CLAMP_TO_EDGE);
        } else {
            throw new RuntimeException("Error creating texture.");
        }
    }

    public void allocate(int width, int height) {
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, rendererID);
        GLES32.glTexImage2D(GLES32.GL_TEXTURE_2D, 0, GLES32.GL_RGBA, width, height, 0, GLES32.GL_RGBA, GLES32.GL_UNSIGNED_BYTE, null);
    }

    public void loadTexture(@NonNull Context context, int resourceId) {
        // Load the bitmap from a resource.
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false; // No pre-scaling

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

        if (rendererID != 0) {
            // Bind to the texture in OpenGL.
            GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, rendererID);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES32.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap since it's now in OpenGL.
            bitmap.recycle();
        } else {
            throw new RuntimeException("Texture not initialized.");
        }
    }

    public void bind(int textureUnit) {
        GLES32.glActiveTexture(GLES32.GL_TEXTURE0 + textureUnit);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, rendererID);
    }

    public void setFilter(Filter min, Filter mag, Wrap s, Wrap t) {
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, rendererID);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, min.id);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, mag.id);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_S, s.id);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_T, t.id);
    }
    int getRendererID() {
        return rendererID;
    }
    public void unbind() {
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, 0);
    }

    @Override
    public void close() {
        GLES32.glDeleteTextures(1, new int[]{rendererID}, 0);
    }
}
