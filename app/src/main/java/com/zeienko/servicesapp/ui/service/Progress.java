package com.zeienko.servicesapp.ui.service;

import android.os.Build;
import androidx.annotation.RequiresApi;
import okhttp3.*;
import okio.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class Progress {

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void main(String... args) {
        new Progress().run();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void run() {
        Request request = new Request.Builder()
                .url("https://upload.wikimedia.org/wikipedia/commons/f/ff/Pizigani_1367_Chart_10MB.jpg")
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(chain -> {
                    Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                            .body(new ProgressResponseBodyWrapper(originalResponse.body(), new ProgressListenerImp(getImageLoaderImp())))
                            .build();
                })
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("Failure");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody responseBody = response.body();
                BufferedSource bufferedSource = responseBody.source();
                Buffer buffer = new Buffer();
                buffer.writeAll(bufferedSource);
                OutputStream outputStream = new FileOutputStream("Pizigani_1367_Chart_10MB.jpg");
                buffer.copyTo(outputStream);
            }
        });
    }

    private ImageLoaderCallback getImageLoaderImp() {
        return new ImageLoaderCallbackImp();
    }

    private ProgressListener getProgressListener() {
        return new ProgressListener() {
            boolean firstUpdate = true;

            @Override
            public void update(long bytesRead, long contentLength, boolean done) {
                if (done) {
                    System.out.println("completed");
                } else {
                    if (firstUpdate) {
                        firstUpdate = false;
                        if (contentLength == -1) {
                            System.out.println("content-length: unknown");
                        } else {
                            System.out.format("content-length: %d\n", contentLength);
                        }
                    }

                    System.out.println(bytesRead);

                    if (contentLength != -1) {
                        System.out.format("%d%% done\n", (100 * bytesRead) / contentLength);
                    }
                }
            }
        };
    }

    interface ProgressListener {
        void update(long bytesRead, long contentLength, boolean done);
    }

    interface ImageLoaderCallback {
        void onComplete();

        void onFirstUpdate(long bytesRead, long contentLength);

        void onBytesReadUpdate(long bytesRead, long contentLength);

        void onUpdate(long bytesRead, long contentLength);
    }

    private static class ImageLoaderCallbackImp implements ImageLoaderCallback {
        @Override
        public void onComplete() {
            System.out.println("completed");
        }

        @Override
        public void onFirstUpdate(long bytesRead, long contentLength) {
            if (contentLength == -1) {
                System.out.println("content-length: unknown");
            } else {
                System.out.format("content-length: %d\n", contentLength);
            }
        }

        @Override
        public void onBytesReadUpdate(long bytesRead, long contentLength) {
            System.out.println(bytesRead);
        }

        @Override
        public void onUpdate(long bytesRead, long contentLength) {
            System.out.format("%d%% done\n", (100 * bytesRead) / contentLength);
        }
    }

    private static class ProgressListenerImp implements ProgressListener {
        private boolean firstUpdate = true;
        private ImageLoaderCallback callback;

        ProgressListenerImp(ImageLoaderCallback callback) {
            this.callback = callback;
        }

        @Override
        public void update(long bytesRead, long contentLength, boolean done) {
            if (done) {
                callback.onComplete();
            } else {
                if (firstUpdate) {
                    firstUpdate = false;
                    callback.onFirstUpdate(bytesRead, contentLength);
                }
                callback.onBytesReadUpdate(bytesRead, contentLength);
                if (contentLength != -1) {
                    callback.onUpdate(bytesRead, contentLength);
                }
            }
        }
    }

    private static class ProgressResponseBodyWrapper extends ResponseBody {

        private final ResponseBody responseBody;
        private final ProgressListener progressListener;
        private BufferedSource bufferedSource;

        ProgressResponseBodyWrapper(ResponseBody responseBody,
                                    ProgressListener progressListener) {
            this.responseBody = responseBody;
            this.progressListener = progressListener;
        }

        @Override
        public MediaType contentType() {
            return responseBody.contentType();
        }

        @Override
        public long contentLength() {
            return responseBody.contentLength();
        }

        @Override
        public BufferedSource source() {
            if (bufferedSource == null) {
                bufferedSource = Okio.buffer(source(responseBody.source()));
            }
            return bufferedSource;
        }

        private Source source(Source source) {
            return new ForwardingSource(source) {
                long totalBytesRead = 0L;

                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    long bytesRead = super.read(sink, byteCount);
                    // read() returns the number of bytes read, or -1 if this source is exhausted.
                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                    progressListener.update(totalBytesRead, responseBody.contentLength(), bytesRead == -1);
                    return bytesRead;
                }
            };
        }
    }
}