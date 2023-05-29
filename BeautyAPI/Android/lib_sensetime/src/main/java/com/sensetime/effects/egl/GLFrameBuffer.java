package com.sensetime.effects.egl;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.sensetime.effects.egl.program.BaseProgram;
import com.sensetime.effects.egl.program.GL2DProgram;
import com.sensetime.effects.egl.program.OESProgram;
import com.sensetime.effects.utils.GlUtil;

public class GLFrameBuffer {

    private int[] mFramebuffer = new int[]{-1};
    private int[] mTargetTexture = new int[]{-1};
    private int mWidth, mHeight, mRotation;
    private boolean isFlipV, isFlipH;

    private final int textureType;

    private final BaseProgram mProgram;

    private float[] mMVPMatrix = new float[16];
    private final float[] mTexMatrix = GlUtil.IDENTITY_MATRIX;

    public GLFrameBuffer(int textureType) {
        this.textureType = textureType;
        mProgram = textureType == GLES20.GL_TEXTURE_2D ? new GL2DProgram() : new OESProgram();
        Matrix.setIdentityM(mMVPMatrix, 0);
    }

    public boolean setSize(int width, int height) {
        if (mWidth != width || mHeight != height) {
            mWidth = width;
            mHeight = height;
            deleteFramebuffer();
            bindFramebuffer(width, height);
            return true;
        }
        return false;
    }

    public void setRotation(int rotation) {
        if (mRotation != rotation) {
            mRotation = rotation;
            mMVPMatrix = GlUtil.createTransformMatrix(rotation, isFlipH, isFlipV);
        }
    }

    public void setFlipV(boolean flipV) {
        if (isFlipV != flipV) {
            isFlipV = flipV;
            mMVPMatrix = GlUtil.createTransformMatrix(mRotation, isFlipH, flipV);
        }
    }

    public void setFlipH(boolean flipH) {
        if (isFlipH != flipH) {
            isFlipH = flipH;
            mMVPMatrix = GlUtil.createTransformMatrix(mRotation, flipH, isFlipV);
        }
    }

    public int getTextureId(){
        return mTargetTexture[0];
    }

    public int getTextureType(){
        return textureType;
    }


    public int process(int textureId) {
        if (mFramebuffer[0] == -1) {
            throw new RuntimeException("setSize firstly!");
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffer[0]);
        GlUtil.checkGlError("glBindFramebuffer");
        GLES20.glViewport(0, 0, mWidth, mHeight);
        GLES20.glClearColor(1f, 0, 0, 1f);

        mProgram.draw(textureId, mTexMatrix, mMVPMatrix);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glUseProgram(0);

        return mTargetTexture[0];
    }

    public void release(){
        deleteFramebuffer();
        mProgram.release();
    }


    private void deleteFramebuffer() {
        if (mTargetTexture[0] != -1) {
            GLES20.glDeleteTextures(1, mTargetTexture, 0);
            mTargetTexture[0] = -1;
        }

        if (mFramebuffer[0] != -1) {
            GLES20.glDeleteFramebuffers(1, mFramebuffer, 0);
            mFramebuffer[0] = -1;
        }
    }

    private void bindFramebuffer(int width, int height) {
        GLES20.glGenFramebuffers(1, mFramebuffer, 0);
        GlUtil.checkGlError("glGenFramebuffers");

        GLES20.glGenTextures(1, mTargetTexture, 0);
        GlUtil.checkGlError("glGenTextures");

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTargetTexture[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffer[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
                GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D,
                mTargetTexture[0], 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

}
