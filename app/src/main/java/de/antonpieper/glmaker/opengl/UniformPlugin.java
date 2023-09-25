package de.antonpieper.glmaker.opengl;

import androidx.annotation.NonNull;

import java.util.Arrays;

public interface UniformPlugin {
    void apply(@NonNull Shader shader);
    abstract class BasePlugin<T> implements UniformPlugin {
        protected final @NonNull String name;
        protected final @NonNull T values;
        public BasePlugin(@NonNull String name, @NonNull T values) {
            this.name = name;
            this.values = values;
        }
    }
    class Float3 extends BasePlugin<float[]> {
        public Float3(@NonNull String name, int length) {
            super(name, new float[length * 3]);
        }

        public void set(int index, float x, float y, float z) {
            int offset = index * 3;
            values[offset++] = x;
            values[offset++] = y;
            values[offset] = z;
        }

        @Override
        public void apply(@NonNull Shader shader) {
            shader.setFloat3(name, values);
        }
    }
    class Int extends BasePlugin<int[]> {
        public Int(@NonNull String name) {
            super(name, new int[1]);
        }

        public void set(int value) {
            values[0] = value;
        }

        @Override
        public void apply(@NonNull Shader shader) {
            shader.setInt(name, values);
        }
    }
}
