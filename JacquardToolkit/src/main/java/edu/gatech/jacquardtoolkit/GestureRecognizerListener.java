package edu.gatech.jacquardtoolkit;

import android.support.annotation.NonNull;

public interface GestureRecognizerListener {
    void onGestureRecognized(@NonNull CustomGestureRecognizer recognizer);
}
