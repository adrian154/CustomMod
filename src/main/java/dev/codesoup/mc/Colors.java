package dev.codesoup.mc;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.text.TextFormatting;

public class Colors {
	
	public static Map<String, TextFormatting> colors;
	public static Map<TextFormatting, Integer> RGB;
	
	static {
		
		colors = new HashMap<>();
		colors.put("black", TextFormatting.BLACK);
		colors.put("darkblue", TextFormatting.DARK_BLUE);
		colors.put("darkgreen", TextFormatting.DARK_GREEN);
		colors.put("darkaqua", TextFormatting.DARK_AQUA);
		colors.put("darkred", TextFormatting.DARK_RED);
		colors.put("purple", TextFormatting.DARK_PURPLE);
		colors.put("gold", TextFormatting.GOLD);
		colors.put("gray", TextFormatting.GRAY);
		colors.put("darkgray", TextFormatting.DARK_GRAY);
		colors.put("blue", TextFormatting.BLUE);
		colors.put("green", TextFormatting.GREEN);
		colors.put("aqua", TextFormatting.AQUA);
		colors.put("red", TextFormatting.RED);
		colors.put("magenta", TextFormatting.LIGHT_PURPLE);
		colors.put("yellow", TextFormatting.YELLOW);
		colors.put("white", TextFormatting.WHITE);

		RGB = new HashMap<>();
		RGB.put(TextFormatting.BLACK, 0x000000);
		RGB.put(TextFormatting.DARK_BLUE, 0x0000AA);
		RGB.put(TextFormatting.DARK_GREEN, 0x00AA00);
		RGB.put(TextFormatting.DARK_AQUA, 0x00AAAA);
		RGB.put(TextFormatting.DARK_RED, 0xAA0000);
		RGB.put(TextFormatting.DARK_PURPLE, 0xAA00AA);
		RGB.put(TextFormatting.GOLD, 0xFFAA00);
		RGB.put(TextFormatting.GRAY, 0xAAAAAA);
		RGB.put(TextFormatting.DARK_GRAY, 0x555555);
		RGB.put(TextFormatting.BLUE, 0x5555FF);
		RGB.put(TextFormatting.GREEN, 0x55FF55);
		RGB.put(TextFormatting.AQUA, 0x55FFFF);
		RGB.put(TextFormatting.RED, 0xFF5555);
		RGB.put(TextFormatting.LIGHT_PURPLE, 0xFF55FF);
		RGB.put(TextFormatting.YELLOW, 0xFFFF55);
		RGB.put(TextFormatting.WHITE, 0xFFFFFF);
		
	}
	
	public static TextFormatting fromString(String string) {
		return colors.get(string);
	}

	public static int toRGB(TextFormatting fmt) {
		return RGB.get(fmt);
	}
	
}
