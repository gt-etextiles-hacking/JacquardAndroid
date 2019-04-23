package edu.gatech.muc.jacquard.demo;


import edu.gatech.jacquardtoolkit.CustomGestureRecognizer;
import edu.gatech.jacquardtoolkit.GestureRecognizerListener;

public class MyReleaseGestureRecognizer extends CustomGestureRecognizer {

    private double[] previous;

    public MyReleaseGestureRecognizer(int tag, GestureRecognizerListener listener) {
        super(tag);
        this.setListener(listener);
    }

    @Override
    protected void onReceiveThreadData(double[] forces) {
        if (previous == null) {
            previous = forces;
            return;
        }

        if (sum(previous) > 0 && sum(forces) == 0) {
            gestureRecognized();
        }
        previous = forces;
    }

    private double sum(double[] arr) {
        double sum = 0;
        for (double d : arr) {
            sum += d;
        }
        return sum;
    }

}
