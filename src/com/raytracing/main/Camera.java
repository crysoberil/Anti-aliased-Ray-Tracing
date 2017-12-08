package com.raytracing.main;

import com.raytracing.primiteves.Point3;
import com.raytracing.primiteves.Vector3;

public class Camera {
    public static final int WALK_LENGTH = 5;
    public static final double ROTATATION_ANGLE = 0.0872664; // radian

    // private Vector3 dirBackwards, dirUp, dirRight;
    private Vector3 dirUp;
    private Point3 position;
    private Point3 lookAt;

    public Camera() {
        resetCamera();
    }

    public void resetCamera() {
        position = new Point3(100, 35, 0);
        lookAt = new Point3(0, 35, 0);
        dirUp = new Vector3(0, 1, 0);

        lookAt = position.addVector(getForwardVector()); // unit distance
    }

    public Vector3 getForwardVector() {
        return position.vectorTo(lookAt).unitVector();
    }

    public Vector3 getRightVector() {
        return getForwardVector().cross(dirUp).unitVector();
    }

    public Vector3 getUpVector() {
        return dirUp;
    }

    public Point3 getEyePosition() {
        return position;
    }

    public void walk(double units) {
        Vector3 d = getForwardVector().scaleBy(units);
        position = position.addVector(d);
        lookAt = lookAt.addVector(d);
    }

    public void rotateYaw(double rotationAngle) {
        Vector3 forwardDirection = position.vectorTo(lookAt);
        forwardDirection = forwardDirection.rotateByAxis(rotationAngle, dirUp);
        lookAt = position.addVector(forwardDirection);
    }

    public void rotatePitch(double rotationAngle) {
        Vector3 dirRight = getRightVector();
        dirUp = dirUp.rotateByAxis(rotationAngle, dirRight);
        Vector3 dirForward = getForwardVector().rotateByAxis(rotationAngle, dirRight);

        lookAt = position.addVector(dirForward);
    }

    public void rotateRoll(double rotationAngle) {
        dirUp = dirUp.rotateByAxis(rotationAngle, getForwardVector());
    }

    public void straf(double units) {
        Vector3 d = getRightVector().scaleBy(units);
        position = position.addVector(d);
        lookAt = lookAt.addVector(d);
    }

    public void fly(double units) {
        Vector3 d = dirUp.scaleBy(units);
        position = position.addVector(d);
        lookAt = lookAt.addVector(d);
    }
}
