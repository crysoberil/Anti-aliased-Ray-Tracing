package com.raytracing.shapes;

import com.raytracing.primiteves.Color3;
import com.raytracing.primiteves.Plane3;
import com.raytracing.primiteves.Point3;
import com.raytracing.primiteves.Ray;
import com.raytracing.tracer.RaySurfaceIntersection;

public class CheckerBoard extends Shape {
	public Color3 color1, color2;
	public double cellWidth;
	public Plane3 plane;

	private int roundedNumber(double n) {
		if (n > -1e-10)
			return (int) n;
		else
			return (int) (n - 1);
	}

	@Override
	public Color3 getColorAtPoint(Point3 p) {
		double dI = p.x / cellWidth;
		double dJ = p.z / cellWidth;

		int i = roundedNumber(dI);
		int j = roundedNumber(dJ);

		if ((i & 1) == (j & 1))
			return color1;
		return color2;
	}

	@Override
	public RaySurfaceIntersection findIntersection(Ray ray) {
		return plane.findIntersection(ray);
	}

    @Override
    public double getIntersectionT(Ray ray) {
        return plane.getIntersectionT(ray);
    }
}
