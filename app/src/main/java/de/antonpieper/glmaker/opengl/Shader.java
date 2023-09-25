package de.antonpieper.glmaker.opengl;

import android.opengl.GLES32;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class Shader implements AutoCloseable {


    @FunctionalInterface
    public interface OnError {
        void onError(@NonNull ShaderError error);
    }

    public static String TAG = "Shader";
    private int rendererID = 0;
    private Map<String, Integer> uniformIds = new HashMap<>();

    private @NonNull String fragmentSource;
    private @NonNull String vertexSource;
    private final Deque<OnError> errorListeners = new ArrayDeque<>();

    public Shader(@NonNull String vertexSource, @NonNull String fragmentSource) {
        // To make the linter happy
        this.vertexSource = vertexSource;
        this.fragmentSource = fragmentSource;
    }

    public void addErrorListener(@NonNull OnError listener) {
        errorListeners.add(listener);
    }

    public boolean removeListener(@NonNull OnError listener) {
        return errorListeners.remove(listener);
    }

    public void compile(@NonNull String fragmentSrc, @NonNull String vertexSrc) {
        GLES32.glDeleteProgram(rendererID);
        rendererID = compileImpl(vertexSrc, fragmentSrc);
    }

    private void dispatch(@NonNull ShaderError error) {
        for (var listener : errorListeners) {
            listener.onError(error);
        }
    }

    @NonNull
    public String fragmentSource() {
        return fragmentSource;
    }

    @NonNull
    public String vertexSource() {
        return vertexSource;
    }

    private int compileImpl(@NonNull String vertexSource, @NonNull String fragmentSource) {
        this.vertexSource = vertexSource;
        this.fragmentSource = fragmentSource;
        uniformIds.clear();
        // Create an empty vertex shader handle
        int vertexShader = GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER);

        // Send the vertex shader source code to GL
        GLES32.glShaderSource(vertexShader, vertexSource);

        // Compile the vertex shader
        GLES32.glCompileShader(vertexShader);
        boolean errorOccured = false;
        int[] tmp = new int[1];
        GLES32.glGetShaderiv(vertexShader, GLES32.GL_COMPILE_STATUS, tmp, 0);
        if (tmp[0] == GLES32.GL_FALSE) {
            String infoLog = GLES32.glGetShaderInfoLog(vertexShader);

            // We don't need the shader anymore.
            GLES32.glDeleteShader(vertexShader);

            // Handle error
            Log.e(TAG, infoLog);
            errorOccured = true;
            String[] errors = infoLog.split("\n");
            for (String error : errors) {
                int lineNumber = Integer.parseInt(error.substring(2, error.indexOf(':', 2)));
                dispatch(new ShaderError(ShaderError.Type.Vertex, lineNumber, error));
            }
        }

        // Create an empty fragment shader handle
        int fragmentShader = GLES32.glCreateShader(GLES32.GL_FRAGMENT_SHADER);

        // Send the fragment shader source code to GL
        GLES32.glShaderSource(fragmentShader, fragmentSource);

        // Compile the fragment shader
        GLES32.glCompileShader(fragmentShader);

        GLES32.glGetShaderiv(fragmentShader, GLES32.GL_COMPILE_STATUS, tmp, 0);
        if (tmp[0] == GLES32.GL_FALSE) {
            // The maxLength includes the NULL character
            String infoLog = GLES32.glGetShaderInfoLog(fragmentShader);

            // We don't need the shader anymore.
            GLES32.glDeleteShader(fragmentShader);
            // Either of them. Don't leak shaders.
            GLES32.glDeleteShader(vertexShader);

            // Handle error
            Log.e(TAG, infoLog);
            errorOccured = true;
            String[] errors = infoLog.split("\n");
            for (String error : errors) {
                if (error.length() < 3) continue;
                int lineNumber = Integer.parseInt(error.substring(2, error.indexOf(':', 2)));
                dispatch(new ShaderError(ShaderError.Type.Fragment, lineNumber, error));
            }
        }
        if (errorOccured) {
            return 0;
        }
        // Vertex and fragment shaders are successfully compiled.
        // Now time to link them together into a program.
        // Get a program object.
        int program = GLES32.glCreateProgram();

        // Attach our shaders to our program
        GLES32.glAttachShader(program, vertexShader);
        GLES32.glAttachShader(program, fragmentShader);

        // Link our program
        GLES32.glLinkProgram(program);

        // Note the different functions here: glGetProgram* instead of glGetShader*.
        GLES32.glGetProgramiv(program, GLES32.GL_LINK_STATUS, tmp, 0);
        if (tmp[0] == GLES32.GL_FALSE) {
            String infoLog = GLES32.glGetProgramInfoLog(program);

            // We don't need the program anymore.
            GLES32.glDeleteProgram(program);
            // Don't leak shaders either.
            GLES32.glDeleteShader(vertexShader);
            GLES32.glDeleteShader(fragmentShader);

            // Handle error
            Log.e(TAG, infoLog);
            String[] errors = infoLog.split("\n");
            for (String error : errors) {
                dispatch(new ShaderError(ShaderError.Type.Program, 0, error));
            }
            return 0;
        }

        // Always detach shaders after a successful link.
        GLES32.glDetachShader(program, vertexShader);
        GLES32.glDetachShader(program, fragmentShader);

        // Delete shaders after a successful link
        GLES32.glDeleteShader(vertexShader);
        GLES32.glDeleteShader(fragmentShader);
        return program;
    }

    @Override
    public void close() {
        GLES32.glDeleteProgram(rendererID);
    }


    public void bind() {
        GLES32.glUseProgram(rendererID);
    }

    public boolean isValid() {
        return rendererID != 0;
    }

    private int getUniformLoc(@NonNull String name) {
        return uniformIds.computeIfAbsent(name, (v) -> GLES32.glGetUniformLocation(rendererID, v));
    }

    public void setTexture(@NonNull String name, @NonNull Texture texture, int textureUnit) {
        texture.bind(textureUnit);
        setInt(name, textureUnit);
    }

    public void setFloat2(@NonNull String name, @NonNull float[] float2s) {
        int location = getUniformLoc(name);
        if (location >= 0)
            GLES32.glUniform2fv(location, float2s.length / 2, float2s, 0);
    }

    public void setFloat(String name, float value) {
        int location = getUniformLoc(name);
        if (location >= 0)
            GLES32.glUniform1f(location, value);
    }

    public void setFloat(String name, float... floats) {
        int location = getUniformLoc(name);
        if (location >= 0)
            GLES32.glUniform1fv(location, floats.length, floats, 0);
    }

    public void setFloat3(String name, float[] float3s) {
        int location = getUniformLoc(name);
        if (location >= 0)
            GLES32.glUniform3fv(location, float3s.length / 3, float3s, 0);
    }

    public void setInt(@NonNull String name, int value) {
        int location = getUniformLoc(name);
        if (location >= 0)
            GLES32.glUniform1i(location, value);
    }

    public void setInt(@NonNull String name, @NonNull int... ints) {
        int location = getUniformLoc(name);
        if (location >= 0)
            GLES32.glUniform1iv(location, ints.length, ints, 0);
    }

    public void unbind() {
        GLES32.glUseProgram(0);
    }
}
