package edu.gatech.muc.jacquard.lib;

import android.support.annotation.Nullable;

public abstract class CustomGestureRecognizer {

    private GestureRecognizerListener listener;
    private boolean enabled;

    protected CustomGestureRecognizer() {
        this.enabled = true;
    }

    public void setListener(@Nullable GestureRecognizerListener listener) {
        this.listener = listener;
    }

    protected abstract void onReceiveThreadData();

    protected void gestureRecognized() {
        if (this.listener != null) {
            this.listener.onGestureRecognized(this);
        }
    }

}
