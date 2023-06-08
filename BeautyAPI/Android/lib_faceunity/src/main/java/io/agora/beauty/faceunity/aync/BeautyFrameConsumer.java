package io.agora.beauty.faceunity.aync;

import io.agora.base.TextureBufferHelper;

public class BeautyFrameConsumer {

    private final TextureBufferHelper mTextureBufferHelper;
    private final Callback callback;
    private volatile boolean isProcessing = false;

    public BeautyFrameConsumer(TextureBufferHelper textureBufferHelper, Callback callback) {
        mTextureBufferHelper = textureBufferHelper;
        this.callback = callback;
    }


    public void consume(BeautyFrameProducer producer, int index) {
        if (!isProcessing && callback != null) {
            mTextureBufferHelper.getHandler().post(() -> {
                AsyncVideoFrame frame = producer.dequeueFrame(index);
                if (frame != null) {
                    isProcessing = true;
                    callback.onFrameConsumed(frame);
                    producer.enqueueFrame(index);
                    isProcessing = false;
                    producer.requestFrame();
                }
            });
        }
    }

    public interface Callback {
        void onFrameConsumed(AsyncVideoFrame videoFrame);
    }

}
