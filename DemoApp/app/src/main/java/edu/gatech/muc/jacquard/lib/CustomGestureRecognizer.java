package edu.gatech.muc.jacquard.lib;

import android.support.annotation.Nullable;

public abstract class CustomGestureRecognizer {

    private GestureRecognizerListener listener;
    private boolean enabled;
    private int tag;

    protected CustomGestureRecognizer(int tag) {
        this.enabled = true;
        this.tag = tag;
    }

    public void setListener(@Nullable GestureRecognizerListener listener) {
        this.listener = listener;
    }

    protected abstract void onReceiveThreadData(double[] forces);

    protected void gestureRecognized() {
        if (this.listener != null && this.enabled) {
            this.listener.onGestureRecognized(this);
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getTag() {
        return tag;
    }

}
