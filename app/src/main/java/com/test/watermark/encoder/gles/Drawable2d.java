package com.test.watermark.encoder.gles;

import java.nio.FloatBuffer;

public class Drawable2d {
    private static final int SIZEOF_FLOAT = 4;

    private static final float FULL_RECTANGLE_COORDS[] = {
        -1.0f, -1.0f,
         1.0f, -1.0f,
        -1.0f,  1.0f,
         1.0f,  1.0f,
    };
    private static final float FULL_RECTANGLE_TEX_COORDS[] = {
        0.0f, 0.0f,
        1.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f
    };
    private static final FloatBuffer FULL_RECTANGLE_BUF = GlUtil.createFloatBuffer(FULL_RECTANGLE_COORDS);
    private static final FloatBuffer FULL_RECTANGLE_TEX_BUF = GlUtil.createFloatBuffer(FULL_RECTANGLE_TEX_COORDS);

    private FloatBuffer mVertexArray;
    private FloatBuffer mTexCoordArray;
    private int mVertexCount;
    private int mCoordsPerVertex;
    private int mVertexStride;
    private int mTexCoordStride;
    private Prefab mPrefab;

    public enum Prefab {
        FULL_RECTANGLE
    }

    public Drawable2d(Prefab shape) {
        if (shape == Prefab.FULL_RECTANGLE) {
            mVertexArray = FULL_RECTANGLE_BUF;
            mTexCoordArray = FULL_RECTANGLE_TEX_BUF;
            mCoordsPerVertex = 2;
            mVertexStride = mCoordsPerVertex * SIZEOF_FLOAT;
            mVertexCount = FULL_RECTANGLE_COORDS.length / mCoordsPerVertex;
        } else {
            throw new RuntimeException("Unknown shape " + shape);
        }
        mTexCoordStride = 2 * SIZEOF_FLOAT;
        mPrefab = shape;
    }

    public FloatBuffer getVertexArray() {
        return mVertexArray;
    }

    public FloatBuffer getTexCoordArray() {
        return mTexCoordArray;
    }

    public int getVertexCount() {
        return mVertexCount;
    }

    public int getVertexStride() {
        return mVertexStride;
    }

    public int getTexCoordStride() {
        return mTexCoordStride;
    }

    public int getCoordsPerVertex() {
        return mCoordsPerVertex;
    }
}
