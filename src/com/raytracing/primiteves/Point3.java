package com.raytracing.primiteves;

import java.util.Scanner;

public class Point3 {
    public final double x, y, z;

    public Point3() {
        x = y = z = 0.0;
    }

    public Point3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point3(Point3 ref) {
        this.x = ref.x;
        this.y = ref.y;
        this.z = ref.z;
    }

    @Override
    public boolean equals(Object sec) {
        Point3 a = (Point3) sec;
        return Math.abs(x - a.x) < 1e-8 && Math.abs(y - a.y) < 1e-8 && Math.abs(z - a.z) < 1e-8;
    }

    public double distanceTo(Point3 p) {
        double d_2 = (x - p.x) * (x - p.x) + (y - p.y) * (y - p.y) + (z - p.z) * (z - p.z);
        return Math.sqrt(d_2);
    }

    public Vector3 vectorTo(Point3 sec) {
        return new Vector3(sec.x - x, sec.y - y, sec.z - z);
    }

    public Point3 addVector(Vector3 vec) {
        return new Point3(x + vec.x, y + vec.y, z + vec.z);
    }

    public Point3 subtractVector(Vector3 vec) {
        return new Point3(x - vec.x, y - vec.y, z - vec.z);
    }

    public static Point3 read(Scanner in) {
        double x = in.nextDouble();
        double y = in.nextDouble();
        double z = in.nextDouble();
        return new Point3(x, y, z);
    }

    @Override
    public String toString() {
        return "P=(" + x + "," + y + "," + z + ")";
    }

    public Vector3 asVector() {
        return new Vector3(x, y, z);
    }

    public Vector3 subtractPoint(Point3 center) {
        return new Vector3(x - center.x, y - center.y, z - center.z);
    }
}
