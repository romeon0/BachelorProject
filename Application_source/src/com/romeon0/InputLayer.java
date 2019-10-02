package com.romeon0;

/**
 * Created by Romeon0 on 5/9/2018.
 */
public class InputLayer implements Layer {
    public double[][] outputImage;

    @Override
    public LAYER_TYPE getType() {
        return LAYER_TYPE.INPUT;
    }



    //getters
    public double[][] getOutputImage() {
        return outputImage;
    }

    //setters
    public void setOutputImage(double[][] outputImage) {
        this.outputImage = outputImage;
    }

}
