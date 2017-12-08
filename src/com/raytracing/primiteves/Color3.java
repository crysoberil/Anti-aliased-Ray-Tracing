package com.raytracing.primiteves;

import java.awt.Color;
import java.util.Scanner;

public class Color3 {
	public static final Color3 COLOR_WHITE = new Color3(1, 1, 1);
	public static final Color3 COLOR_BLACK = new Color3(0, 0, 0);

	public final double r, g, b;

	public Color3(double r, double g, double b) {
		this.r = r;
		this.g = g;
		this.b = b;
	}

	public Color3 scaleBy(double scaleBy) {
		return new Color3(r * scaleBy, g * scaleBy, b * scaleBy);
	}

	public Color3 add(Color3 sec) {
		return new Color3(r + sec.r, g + sec.g, b + sec.b);
	}

	public double getMagnitude() {
		return Math.sqrt(r * r + g * g + b * b);
	}

	public static Color3 read(Scanner in) {
		double r = in.nextDouble();
		double g = in.nextDouble();
		double b = in.nextDouble();
		return new Color3(r, g, b);
	}

	public int getRGB() {
		return new Color((float) r, (float) g, (float) b).getRGB();
	}

	@Override
	public boolean equals(Object sec) {
		Color3 col = (Color3) sec;
		return Math.abs(r - col.r) < 1e-8 && Math.abs(g - col.g) < 1e-8 && Math.abs(b - col.b) < 1e-8;
	}

	@Override
	public String toString() {
		return "C=(" + r + "," + g + "," + b + ")";
	}
}
