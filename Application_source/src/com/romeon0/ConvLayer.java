package com.romeon0;

import java.util.Random;

/**
 * Created by Romeon0 on 5/8/2018.
 */

public class ConvLayer implements Layer {
    public double[][] inputImage;
    public double[][] outputImage;
    private double[][] filter;
    public ConvLayer(double[][] filter){
        if(filter==null){
            Random rand = new Random();
            filter = new double[3][3];
            for(int y=0; y < 3; ++y)
                for(int x=0; x < 3; ++x)
                    filter[y][x] = rand.nextDouble();
        }

        this.filter = filter;
    }

    //getters
    public double[][] getInputImage() {
        return inputImage;
    }
    public double[][] getOutputImage() {
        return outputImage;
    }
    public double[][] getFilter() {
        return filter;
    }

    //setters
    public void setInputImage(double[][] inputImage) {
        this.inputImage = inputImage;
    }
    public void setOutputImage(double[][] outputImage) {
        this.outputImage = outputImage;
    }
    public void setFilter(double[][] filter) {
        this.filter = filter;
    }

    //other
    public LAYER_TYPE getType(){return LAYER_TYPE.CONV;}

}
