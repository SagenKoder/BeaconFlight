/******************************************************************************
 * Copyright (C) BlueLapiz.net - All Rights Reserved                          *
 * Unauthorized copying of this file, via any medium is strictly prohibited   *
 * Proprietary and confidential                                               *
 * Last edited 11/26/18 2:40 PM                                               *
 * Written by Alexander Sagen <alexmsagen@gmail.com>                          *
 ******************************************************************************/

package app.sagen.beaconflight;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

public class CubicSpline3D<T> {

    private Vector<T> points;
    private Vector<Cubic> cubicsX;
    private Vector<Cubic> cubicsY;
    private Vector<Cubic> cubicsZ;
    private Method vector3fGetX;
    private Method vector3fGetY;
    private Method vector3fGetZ;
    private Constructor<T> vector3fConstruct;
    private double heuristicDistance;

    public CubicSpline3D(Class<T> type, Class valueType) {
        this.points = new Vector<>();

        this.cubicsX = new Vector<>();
        this.cubicsY = new Vector<>();
        this.cubicsZ = new Vector<>();

        try {
            vector3fGetX = type.getDeclaredMethod("getX");
            vector3fGetY = type.getDeclaredMethod("getY");
            vector3fGetZ = type.getDeclaredMethod("getZ");
            vector3fConstruct = type.getConstructor(valueType, valueType, valueType);
        } catch (SecurityException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public void addPoint(T point) {
        this.points.add(point);
    }

    public Vector<T> getPoints() {
        return points;
    }

    public void calcSpline() {
        try {
            calcNaturalCubic(points, vector3fGetX, cubicsX);
            calcNaturalCubic(points, vector3fGetY, cubicsY);
            calcNaturalCubic(points, vector3fGetZ, cubicsZ);

            double heuristicDistance = 0f;
            T first = getPoint(0f);
            double lastX = (double) vector3fGetX.invoke(first);
            double lastY = (double) vector3fGetY.invoke(first);
            double lastZ = (double) vector3fGetZ.invoke(first);
            float step = 1f / (getPoints().size() * 6f); // calculate 6 sections for every point
            for (float i = step; i < 1; i += step) {
                T current = getPoint(i);
                double currentX = (double) vector3fGetX.invoke(current);
                double currentY = (double) vector3fGetY.invoke(current);
                double currentZ = (double) vector3fGetZ.invoke(current);

                double dist = Math.sqrt(Math.pow(currentX - lastX, 2) + Math.pow(currentY - lastY, 2) + Math.pow(currentZ - lastZ, 2));
                heuristicDistance += dist;
            }
            this.heuristicDistance = heuristicDistance;
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public T getPoint(float position) {

        System.out.println("getPoint");

        position = position * cubicsX.size();
        int cubicNum = (int) position;
        float cubicPos = (position - cubicNum);
        if (cubicNum >= cubicsX.size()) {
            cubicNum = cubicsX.size() - 1;
            cubicPos = 1;
        }
        try {
            return vector3fConstruct.newInstance(
                    cubicsX.get(cubicNum).eval(cubicPos),
                    cubicsY.get(cubicNum).eval(cubicPos),
                    cubicsZ.get(cubicNum).eval(cubicPos)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void calcNaturalCubic(List valueCollection, Method getVal, Collection<Cubic> cubicCollection) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        int num = valueCollection.size() - 1;

        float[] gamma = new float[num + 1];
        float[] delta = new float[num + 1];
        float[] D = new float[num + 1];

        int i;

        gamma[0] = 1.0f / 2.0f;
        for (i = 1; i < num; i++) {
            gamma[i] = 1.0f / (4.0f - gamma[i - 1]);
        }
        gamma[num] = 1.0f / (2.0f - gamma[num - 1]);

        float p0 = ((Double) getVal.invoke(valueCollection.get(0))).floatValue();
        float p1 = ((Double) getVal.invoke(valueCollection.get(1))).floatValue();

        delta[0] = 3.0f * (p1 - p0) * gamma[0];
        for (i = 1; i < num; i++) {
            p0 = ((Double) getVal.invoke(valueCollection.get(i - 1))).floatValue();
            p1 = ((Double) getVal.invoke(valueCollection.get(i + 1))).floatValue();
            delta[i] = (3.0f * (p1 - p0) - delta[i - 1]) * gamma[i];
        }
        p0 = ((Double) getVal.invoke(valueCollection.get(num - 1))).floatValue();
        p1 = ((Double) getVal.invoke(valueCollection.get(num))).floatValue();

        delta[num] = (3.0f * (p1 - p0) - delta[num - 1]) * gamma[num];

        D[num] = delta[num];
        for (i = num - 1; i >= 0; i--) {
            D[i] = delta[i] - gamma[i] * D[i + 1];
        }

        // now compute the coefficients of the cubics
        cubicCollection.clear();

        for (i = 0; i < num; i++) {
            p0 = ((Double) getVal.invoke(valueCollection.get(i))).floatValue();
            p1 = ((Double) getVal.invoke(valueCollection.get(i + 1))).floatValue();
            cubicCollection.add(new Cubic(p0, D[i], 3 * (p1 - p0) - 2 * D[i] - D[i + 1], 2 * (p0 - p1) + D[i] + D[i + 1]));
        }
    }

    public double getHeuristicDistance() {
        return heuristicDistance;
    }

    public class Cubic {
        private float a, b, c, d;

        public Cubic(float a, float b, float c, float d) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
        }

        public float eval(float u) {
            return (((d * u) + c) * u + b) * u + a;
        }
    }
}
