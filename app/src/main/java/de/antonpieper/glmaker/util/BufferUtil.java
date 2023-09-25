package de.antonpieper.glmaker.util;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BufferUtil {
	public static final ByteBuffer direct(final byte[] values) {
		final ByteBuffer bb = ByteBuffer.allocateDirect(values.length).order(ByteOrder.nativeOrder());
		bb.put(values).position(0);
		return bb;
	}
	public static final ByteBuffer direct(final float[] values) {
		final ByteBuffer bb = ByteBuffer.allocateDirect(values.length * Float.BYTES).order(ByteOrder.nativeOrder());
		bb.asFloatBuffer().put(values).position(0);
		return bb;
	}
	public static final ByteBuffer direct(final int[] values) {
		final ByteBuffer bb = ByteBuffer.allocateDirect(values.length * Integer.BYTES).order(ByteOrder.nativeOrder());
		bb.asIntBuffer().put(values).position(0);
		return bb;
	}
}
