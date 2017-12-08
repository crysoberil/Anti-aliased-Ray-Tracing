package com.raytracing.shapes;

import com.raytracing.primiteves.Color3;
import com.raytracing.primiteves.Point3;
import com.raytracing.primiteves.Ray;
import com.raytracing.tracer.RaySurfaceIntersection;

public abstract class Shape {
    public double ambientCoeff;
    public double diffuseCoeff;
    public double specularCoeff;
    public double specularExponent;
    public double refractiveIndex;
    public double refCoeff; // Reflection or refraction co-efficient
    public Color3 color;

    public abstract RaySurfaceIntersection findIntersection(Ray ray);

    public abstract double getIntersectionT(Ray ray);

    public Color3 getColorAtPoint(Point3 p) {
        return color;
    }

    public boolean isRefractable() {
        return false;
    }
}
