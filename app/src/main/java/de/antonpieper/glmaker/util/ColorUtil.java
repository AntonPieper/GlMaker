package de.antonpieper.glmaker.util;

public class ColorUtil {
	public static final int rgba2argb(final int rgba, final int numChannels) {
		switch(numChannels) {
			case(3):
				return 0xff_000000 |
					((rgba >>> 8) & 0x00_f00000) | ((rgba >>> 12) & 0x00_0f0000) |
					((rgba >>> 16) & 0x00_00f000) | ((rgba >>> 20) & 0x00_000f00) |
					((rgba >>> 24) & 0x00_0000f0) | ((rgba >>> 28) & 0x00_00000f);
			case(4):
				return ((rgba << 24) & 0xf0_000000) | ((rgba << 20) & 0x0f_000000) |
					((rgba >>> 8) & 0x00_f00000) | ((rgba >>> 12) & 0x00_0f0000) |
					((rgba >>> 16) & 0x00_00f000) | ((rgba >>> 20) & 0x00_000f00) |
					((rgba >>> 24) & 0x00_0000f0) | ((rgba >>> 28) & 0x00_00000f);
			case(6):
				return 0xff_000000 |
					((rgba >>> 8) & 0x00_ff0000) |
					((rgba >>> 16) & 0x00_00ff00) |
					((rgba >>> 24) & 0x00_0000ff);
			case(8):
				return ((rgba << 24) & 0xff_000000) |
					((rgba >>> 8) & 0x00_ff0000) |
					((rgba >>> 16) & 0x00_00ff00) |
					((rgba >>> 24) & 0x00_0000ff);
			default:
				return 0;
		}
	}
}
