package de.antonpieper.glmaker;

import android.opengl.GLES32;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayDeque;
import java.util.Deque;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import de.antonpieper.glmaker.opengl.BufferElement;
import de.antonpieper.glmaker.opengl.BufferLayout;
import de.antonpieper.glmaker.opengl.Framebuffer;
import de.antonpieper.glmaker.opengl.IndexBuffer;
import de.antonpieper.glmaker.opengl.Shader;
import de.antonpieper.glmaker.opengl.ShaderDataType;
import de.antonpieper.glmaker.opengl.UniformPlugin;
import de.antonpieper.glmaker.opengl.VertexArray;
import de.antonpieper.glmaker.opengl.VertexBuffer;
import de.antonpieper.glmaker.util.BufferUtil;

public class GLRenderer implements Renderer, AutoCloseable {
    private static final String DEFAULT_VERTEX = "#version 320 es\n" +
                                                 "precision highp float;\n" +
                                                 "in vec4 position;\n" +
                                                 "\n" +
                                                 "void main(void) {\n" +
                                                 "    gl_Position = position;\n" +
                                                 "}\n";
    private static final String DEFAULT_FRAGMENT = "#version 320 es\n" +
                                                   "precision highp float;\n" +
                                                   "out vec4 fragColor;\n" +
                                                   "\n" +
                                                   "void main(void) {\n" +
                                                   "    fragColor = vec4(0.5);\n" +
                                                   "}\n";
    private static final String DRAW_VERTEX = "#version 320 es\n" +
                                              "precision highp float;\n" +
                                              "in vec4 position;\n" +
                                              "\n" +
                                              "void main(void) {\n" +
                                              "    gl_Position = position;\n" +
                                              "}\n";
    private static final String DRAW_FRAGMENT = "#version 320 es\n" +
                                                "precision highp float;\n" +
                                                "uniform vec2 resolution;\n" +
                                                "uniform sampler2D frame;\n" +
                                                "out vec4 fragColor;\n" +
                                                "\n" +
                                                "void main(void) {\n" +
                                                "    fragColor = texture(frame, gl_FragCoord.xy / resolution);\n" +
                                                "}\n";
    private Deque<UniformPlugin> plugins = new ArrayDeque<>();

    @FunctionalInterface
    public interface ShaderTransformer {
        @NonNull
        String transform(@NonNull String original);
    }

    private class Program implements AutoCloseable {
        private @NonNull String fragmentSource;
        private @NonNull String vertexSource;
        private final @NonNull Shader shader;

        public Program(@NonNull String vertexSource, @NonNull String fragmentSource) {
            this.fragmentSource = fragmentSource;
            this.vertexSource = vertexSource;
            this.shader = new Shader(fragmentSource, vertexSource);
            this.shader.addErrorListener((error) -> errorHandler.onError(error));
        }

        @NonNull
        public Shader shader() {
            return shader;
        }

        public void compile() {
            shader.compile(transformer.transform(fragmentSource), transformer.transform(vertexSource));
            frameCounter = 0;
        }

        @NonNull
        public String fragmentSource() {
            return fragmentSource;
        }

        public void setFragmentSource(@NonNull String fragmentSource) {
            this.fragmentSource = fragmentSource;
        }

        @NonNull
        public String vertexSource() {
            return vertexSource;
        }

        public void setVertexSource(@NonNull String vertexSource) {
            this.vertexSource = vertexSource;
        }

        @Override
        public void close() {
            shader.close();
        }
    }

    private @NonNull ShaderTransformer transformer = (source) -> source;
    private Shader.OnError errorHandler = (error) -> {
    };
    private final Program userProgram = new Program(DEFAULT_VERTEX, DEFAULT_FRAGMENT);
    private final Program drawProgram = new Program(DRAW_VERTEX, DRAW_FRAGMENT);
    private VertexArray vertexArray;
    private VertexArray drawVertexArray;
    private final float[] resolution = new float[]{0, 0};
    private Framebuffer front;
    private Framebuffer back;
    private int frameCounter = 0;
    private float quality = 1f / 8f;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig) {
        GLES32.glClearColor(0, 0, 0, 1);
        userProgram.compile();
        checkErrors("program shader: create");
        userProgram.shader().bind();
        checkErrors("program shader: bind");
        vertexArray = new VertexArray(
                new IndexBuffer(
                        BufferUtil.direct(new int[]{
                                0, 1, 2, 3
                        }).asIntBuffer()
                ), new VertexBuffer(
                new BufferLayout(
                        new BufferElement(ShaderDataType.FLOAT2, "position")
                ),
                BufferUtil.direct(new float[]{
                        -1, -1,
                        -1, 1,
                        1, -1,
                        1, 1
                }).asFloatBuffer()
        )
        );
        checkErrors("vertexArray: create");
        drawProgram.compile();
        drawProgram.shader().bind();
        checkErrors("drawShader: bind");
        drawVertexArray = new VertexArray(
                new IndexBuffer(
                        BufferUtil.direct(new int[]{
                                0, 1, 2, 3
                        }).asIntBuffer()
                ), new VertexBuffer(
                new BufferLayout(
                        new BufferElement(ShaderDataType.FLOAT2, "position")
                ),
                BufferUtil.direct(new float[]{
                        -1, -1,
                        -1, 1,
                        1, -1,
                        1, 1
                }).asFloatBuffer()
        )
        );
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES32.glViewport(0, 0, width, height);
        resolution[0] = width;
        resolution[1] = height;
        createTargets();
    }

    private void deleteTargets() {
        if (front == null || back == null) {
            return;
        }
        front.close();
        back.close();
        checkErrors("deleteTargets");
    }

    private void createTargets() {
        deleteTargets();
        front = new Framebuffer((int) (resolution[0] * quality), (int) (resolution[1] * quality));
        back = new Framebuffer((int) (resolution[0] * quality), (int) (resolution[1] * quality));
        back.bind();
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);
        checkErrors("createTargets");
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);
        if (!userProgram.shader().isValid() || front == null) {
            return;
        }
        front.bind();
        checkErrors("bind front");
        Shader shader = userProgram.shader();
        shader.bind();
        checkErrors("bind shader");
        for (var plugin : plugins) {
            plugin.apply(shader);
        }
        checkErrors("apply plugins");
        shader.setFloat2("resolution", new float[]{(int) (resolution[0] * quality), (int) (resolution[1] * quality)});
        shader.setFloat("quality", quality);
        shader.setInt("frame", frameCounter);
        checkErrors("set basic uniforms");
        shader.setTexture("backbuffer", back.getTexture(), 0);
        checkErrors("apply backbuffer");
        vertexArray.bind();
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_STRIP, 0, 4);
        ++frameCounter;
        checkErrors("draw shader");
        front.unbind();
        checkErrors("unbind front");
        { // swap buffers
            Framebuffer tmp = front;
            front = back;
            back = tmp;
        }
        Shader drawShader = drawProgram.shader();
        drawShader.bind();
        checkErrors("bind drawShader");
        drawVertexArray.bind();
        checkErrors("bind drawVertexArray");
        drawShader.setFloat2("resolution", resolution);
        drawShader.setFloat("quality", quality);
        checkErrors("set resolution");
        drawShader.setTexture("frame", back.getTexture(), 0);
        checkErrors("set frame");
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_STRIP, 0, 4);
        checkErrors("onDrawFrame");
    }

    public float getQuality() {
        return quality;
    }

    public void setErrorHandler(@NonNull Shader.OnError errorHandler) {
        this.errorHandler = errorHandler;
    }

    public void setTransformer(@NonNull ShaderTransformer transformer) {
        this.transformer = transformer;
    }

    public static void checkErrors(String message) {
        boolean hasError = false;
        int error;
        while ((error = GLES32.glGetError()) != GLES32.GL_NO_ERROR) {
            hasError = true;
            Log.e("GLError", GLU.gluErrorString(error));
        }
        if (hasError)
            throw new RuntimeException(message);
    }

    @Override
    public void close() {
        userProgram.close();
        vertexArray.close();
    }

    public String vertexShader() {
        return userProgram.vertexSource();
    }

    public void setVertexShader(@NonNull String vertexShader) {
        userProgram.setVertexSource(vertexShader);
        userProgram.compile();
    }

    public String fragmentShader() {
        return userProgram.fragmentSource();
    }

    public void setFragmentShader(@NonNull String fragmentShader) {
        userProgram.setFragmentSource(fragmentShader);
        userProgram.compile();
    }

    public GLRenderer attach(@NonNull UniformPlugin plugin) {
        plugins.add(plugin);
        return this;
    }
}
