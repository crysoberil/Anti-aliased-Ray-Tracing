package com.raytracing.primiteves;

import java.util.Random;

public class Ray {
    private static final double RAY_ADVANCE_EPSILON = 1e-3;

    public static final Random rand = new Random();
    public Point3 start;
    public final Vector3 direction;

    public Ray(Point3 start, Vector3 direction) {
        this.start = start;
        this.direction = direction.unitVector();
    }

    public Ray(Point3 start, Point3 toPoint) {
        if (start.equals(toPoint)) {
            double scale = 1e-4;
            double dx = (rand.nextDouble() - 0.5) * scale;
            double dy = (rand.nextDouble() - 0.5) * scale;
            double dz = (rand.nextDouble() - 0.5) * scale;
            start = new Point3(start.x + dx, start.y + dy, start.z + dz);
        }

        this.start = start;
        this.direction = start.vectorTo(toPoint).unitVector();
    }

    public void advanceByEpsilon() {
        Vector3 advanceBy = direction.scaleBy(RAY_ADVANCE_EPSILON);
        start = start.addVector(advanceBy);
    }

    public Point3 getPointOnLine(double t) {
        return start.addVector(direction.scaleBy(t));
    }

    @Override
    public String toString() {
        return "[" + start.toString() + ", " + direction.toString() + "]";
    }
}
