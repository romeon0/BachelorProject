package com.romeon0;

/**
 * Created by Romeon0 on 5/8/2018.
 */

public class PoolLayer implements Layer {
    public double[][] inputImage;
    public double[][] outputImage;
    public PoolLayer(){
    }

    //getters
    public double[][] getInputImage() {
        return inputImage;
    }
    public double[][] getOutputImage() {
        return outputImage;
    }

    //setters
    public void setInputImage(double[][] inputImage) {
        this.inputImage = inputImage;
    }
    public void setOutputImage(double[][] outputImage) {
        this.outputImage = outputImage;
    }

    //other
    public LAYER_TYPE getType(){return LAYER_TYPE.POOL;}

}
