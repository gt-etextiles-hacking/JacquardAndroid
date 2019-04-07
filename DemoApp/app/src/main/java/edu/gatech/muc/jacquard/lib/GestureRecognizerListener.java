package edu.gatech.muc.jacquard.lib;

import android.support.annotation.NonNull;

public interface GestureRecognizerListener {
    void onGestureRecognized(@NonNull CustomGestureRecognizer recognizer);
}
