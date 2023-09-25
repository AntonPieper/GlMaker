package de.antonpieper.glmaker.opengl;

import android.opengl.GLES32;

import androidx.annotation.NonNull;

public enum ShaderDataType {
    FLOAT, FLOAT2, FLOAT3, FLOAT4, INT, INT2, INT3, INT4, BOOL, MAT2, MAT3, MAT4;

    public static int size(@NonNull ShaderDataType type) {
        switch (type) {
            case FLOAT:
                return Float.BYTES;
            case FLOAT2:
                return 2 * Float.BYTES;
            case FLOAT3:
                return 3 * Float.BYTES;
            case FLOAT4:
                return 4 * Float.BYTES;
            case INT:
                return Integer.BYTES;
            case INT2:
                return 2 * Integer.BYTES;
            case INT3:
                return 3 * Integer.BYTES;
            case INT4:
                return 4 * Integer.BYTES;
            case MAT2:
                return 2 * 2 * Float.BYTES;
            case MAT3:
                return 3 * 3 * Float.BYTES;
            case MAT4:
                return 4 * 4 * Float.BYTES;
            case BOOL:
                return 1;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static int getGLType(@NonNull ShaderDataType type) {
        switch (type) {
            case FLOAT:
            case FLOAT2:
            case FLOAT3:
            case FLOAT4:
            case MAT4:
            case MAT3:
            case MAT2:
                return GLES32.GL_FLOAT;
            case INT:
            case INT4:
            case INT3:
            case INT2:
                return GLES32.GL_INT;
            case BOOL:
                return GLES32.GL_BOOL;
            default:
                throw new IllegalArgumentException();
        }
    }

}
