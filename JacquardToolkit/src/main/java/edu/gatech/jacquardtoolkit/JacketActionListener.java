package edu.gatech.jacquardtoolkit;

import android.support.annotation.NonNull;

public interface JacketActionListener {
    /**
     * Called when a built-in gesture is performed on the jacket
     * @param gesture The gesture that was performed
     */
    void onGesturePerformed(@NonNull JacquardGesture gesture);
}
