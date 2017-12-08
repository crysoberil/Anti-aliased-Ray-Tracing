package com.raytracing.primiteves;

import java.util.Scanner;

public class Vector3 {
    public final double x, y, z;

    public Vector3() {
        x = y = z = 0.0;
    }

    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3(Vector3 ref) {
        this.x = ref.x;
        this.y = ref.y;
        this.z = ref.z;
    }

    public Vector3 add(Vector3 sec) {
        return new Vector3(x + sec.x, y + sec.y, z + sec.z);
    }

    public Vector3 subtract(Vector3 sec) {
        return new Vector3(x - sec.x, y - sec.y, z - sec.z);
    }

    public Vector3 scaleBy(double scaleBy) {
        return new Vector3(scaleBy * x, scaleBy * y, scaleBy * z);
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public Vector3 unitVector() {
        double len = length();

        if (Math.abs(len) < 1e-8)
            return null;

        return new Vector3(x / len, y / len, z / len);
    }

    public Vector3 reverseVector() {
        return new Vector3(-x, -y, -z);
    }

    @Override
    public boolean equals(Object sec) {
        Vector3 a = (Vector3) sec;
        return Math.abs(x - a.x) < 1e-8 && Math.abs(y - a.y) < 1e-8 && Math.abs(z - a.z) < 1e-8;
    }

    public double dot(Vector3 sec) {
        return x * sec.x + y * sec.y + z * sec.z;
    }

    public Vector3 cross(Vector3 sec) {
        double newX = y * sec.z - z * sec.y;
        double newY = z * sec.x - x * sec.z;
        double newZ = x * sec.y - y * sec.x;
        return new Vector3(newX, newY, newZ);
    }

    public boolean isZeroVector() {
        return Math.abs(x) < 1e-8 && Math.abs(y) < 1e-8 && Math.abs(z) < 1e-8;
    }

    public boolean isParallelTo(Vector3 sec) {
        Vector3 crossed = this.cross(sec);
        return crossed.isZeroVector();
    }

    public boolean isPerpendiculerTo(Vector3 sec) {
        double prod = this.dot(sec);
        return Math.abs(prod) < 1e-8;
    }

    public Vector3 getReflectionDirection(Vector3 normal) {
        if (this.isPerpendiculerTo(normal))
            return this.unitVector();

        // a - 2(a.n)n
        Vector3 v2 = normal.scaleBy(2 * this.dot(normal));
        Vector3 ret = this.subtract(v2).unitVector();
        return ret;
    }

    /**
     * 
     * @param theta In radian
     * @param axis
     * @return
     */
    public Vector3 rotateByAxis(double theta, Vector3 axis) {
        double u = axis.x;
        double v = axis.y;
        double w = axis.z;

        double xPrime = u * (u * x + v * y + w * z) * (1.0 - Math.cos(theta)) + x * Math.cos(theta)
                + (-w * y + v * z) * Math.sin(theta);
        double yPrime = v * (u * x + v * y + w * z) * (1d - Math.cos(theta)) + y * Math.cos(theta)
                + (w * x - u * z) * Math.sin(theta);
        double zPrime = w * (u * x + v * y + w * z) * (1d - Math.cos(theta)) + z * Math.cos(theta)
                + (-v * x + u * y) * Math.sin(theta);

        return new Vector3(xPrime, yPrime, zPrime);
    }

    public static Vector3 read(Scanner in) {
        double x = in.nextDouble();
        double y = in.nextDouble();
        double z = in.nextDouble();
        return new Vector3(x, y, z);
    }

    @Override
    public String toString() {
        return "V=(" + x + "," + y + "," + z + ")";
    }
}
