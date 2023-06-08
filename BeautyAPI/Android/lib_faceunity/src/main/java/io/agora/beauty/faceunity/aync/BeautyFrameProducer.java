package io.agora.beauty.faceunity.aync;

import android.opengl.GLES20;
import android.util.Log;

import java.util.concurrent.Callable;

import io.agora.base.TextureBufferHelper;
import io.agora.base.internal.video.EglBase;
import io.agora.base.internal.video.GlRectDrawer;
import io.agora.base.internal.video.GlTextureFrameBuffer;

public class BeautyFrameProducer {
    private final String TAG = this.getClass().getSimpleName();
    private static final int cacheCount = 2;
    private final TextureBufferHelper mTextureBufferHelper;

    private final GlTextureFrameBuffer[] mCacheFrameBuffers = new GlTextureFrameBuffer[cacheCount];
    private final AsyncVideoFrame[] mFrameQueue = new AsyncVideoFrame[cacheCount];
    private final boolean[] mFrameProcessing = new boolean[cacheCount];
    private final Runnable[] mPushRuns = new Runnable[cacheCount];
    private final GlRectDrawer mDrawer = new GlRectDrawer();
    private volatile boolean isReleased = false;


    private final Callback callback;

    public BeautyFrameProducer(EglBase.Context sharedContext, Callback callback) {
        mTextureBufferHelper = TextureBufferHelper.create("BeautyFrameProducer", sharedContext);
        this.callback = callback;
    }

    public void pushFrameSync(int textureId, int texType, float[] texMatrix, int width, int height, boolean isFront) {
        if (isReleased) {
            return;
        }
        int idleIndex = 0;
        for (int i = 0; i < mFrameQueue.length; i++) {
            if (mFrameQueue[i] == null || !mFrameProcessing[i]) {
                idleIndex = i;
                if(mPushRuns[i] == null){
                    break;
                }
            }
        }
        Log.d(TAG, "pushFrameSync -- frame index: " + idleIndex);
        int _index = idleIndex;
        Runnable pushRun = mPushRuns[_index];
        if (pushRun != null) {
            // 丢帧
            Log.d(TAG, "pushFrameSync -- ignore frame index: " + idleIndex);
            mTextureBufferHelper.getHandler().removeCallbacks(pushRun);
        }

        pushRun = () -> {
            if (isReleased) {
                return;
            }
            Log.d(TAG, "pushFrameSync run -- frame index " + _index + " start...");
            mPushRuns[_index] = null;
            GlTextureFrameBuffer frameBuffer = mCacheFrameBuffers[_index];
            if (frameBuffer == null) {
                frameBuffer = mCacheFrameBuffers[_index] = new GlTextureFrameBuffer(GLES20.GL_RGBA);
            }
            frameBuffer.setSize(width, height);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer.getFrameBufferId());
            if (texType == GLES20.GL_TEXTURE_2D) {
                mDrawer.drawRgb(textureId, texMatrix, width, height, 0, 0, width, height);
            } else {
                mDrawer.drawOes(textureId, texMatrix, width, height, 0, 0, width, height);
            }
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

            AsyncVideoFrame _frame = mFrameQueue[_index];
            if (_frame == null) {
                _frame = mFrameQueue[_index] = new AsyncVideoFrame();
            }
            _frame.width = width;
            _frame.height = height;
            _frame.isFront = isFront;
            _frame.textureId = frameBuffer.getTextureId();
            if (callback != null) {
                callback.onFrameProduced(_index);
            }

            Log.d(TAG, "pushFrameSync run -- frame index " + _index + " end...");

        };
        mPushRuns[_index] = pushRun;
        mTextureBufferHelper.getHandler().post(pushRun);
    }

    public AsyncVideoFrame dequeueFrame(int index) {
        if (isReleased) {
            return null;
        }
        AsyncVideoFrame frame = mFrameQueue[index];
        mFrameProcessing[index] = true;
        return frame;
    }

    public void enqueueFrame(int index) {
        if (isReleased) {
            return;
        }
        mFrameQueue[index] = null;
        mFrameProcessing[index] = false;
    }

    public void requestFrame() {
        if (isReleased) {
            return;
        }
        int index = -1;
        for (int i = 0; i < mFrameQueue.length; i++) {
            if (mFrameQueue[i] != null) {
                index = i;
                break;
            }
        }
        if (index != -1 && callback != null) {
            callback.onFrameProduced(index);
        }
    }

    public void release() {
        isReleased = true;
        mTextureBufferHelper.invoke((Callable<Void>) () -> {
            for (int i = 0; i < mCacheFrameBuffers.length; i++) {
                GlTextureFrameBuffer frameBuffer = mCacheFrameBuffers[i];
                if (frameBuffer != null) {
                    frameBuffer.release();
                    mCacheFrameBuffers[i] = null;
                }
            }
            return null;
        });
        mTextureBufferHelper.dispose();
    }

    public interface Callback {
        void onFrameProduced(int index);
    }

}
