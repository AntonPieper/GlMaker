package de.antonpieper.glmaker.opengl;

import androidx.annotation.NonNull;

public class BufferElement {
    public @NonNull ShaderDataType type;
    public @NonNull String name;
    public int offset;
    public int size;
    public boolean normalized;

    public BufferElement(@NonNull ShaderDataType type) {
        this.type = type;
        this.name = type.name();
    }

    public BufferElement(@NonNull ShaderDataType type, int index) {
        this.type = type;
        this.name = String.valueOf(index);
    }

    public BufferElement(@NonNull ShaderDataType type, @NonNull String name) {
        this.type = type;
        this.name = name;
        this.size = ShaderDataType.size(type);
    }

    public BufferElement(@NonNull ShaderDataType type, @NonNull String name, boolean normalized) {
        this.type = type;
        this.name = name;
        this.normalized = normalized;
        this.size = ShaderDataType.size(type);
    }

    public int getComponentCount() {
        switch (type) {
            case FLOAT:
            case INT:
            case BOOL:
                return 1;
            case FLOAT2:
            case INT2:
                return 2;
            case FLOAT3:
            case INT3:
                return 3;
            case FLOAT4:
            case INT4:
            case MAT2:
                return 4;
            case MAT3:
                return 9;
            case MAT4:
                return 16;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static int getComponentCount(@NonNull ShaderDataType type) {
        switch (type) {
            case FLOAT:
            case BOOL:
            case INT:
                return 1;
            case FLOAT2:
            case INT2:
                return 2;
            case FLOAT3:
            case INT3:
                return 3;
            case FLOAT4:
            case MAT2:
            case INT4:
                return 4;
            case MAT3:
                return 3 * 3;
            case MAT4:
                return 4 * 4;
            default:
                throw new IllegalArgumentException();
        }
    }
}
