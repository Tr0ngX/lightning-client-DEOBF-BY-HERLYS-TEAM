package lightning.utils.animations;

public class Smoother {
    private double smoothedValue;

    public double smooth(double arg0, double arg1, double arg2) {
        double var7 = (1.0 - Math.exp((-(arg1) * arg2)));
        this.smoothedValue = (this.smoothedValue + ((arg0 - this.smoothedValue) * var7));
        return this.smoothedValue;
    }

    public void clear() {
        this.smoothedValue = 0.0;
    }

    public double getSmoothedValue() {
        return this.smoothedValue;
    }

}
