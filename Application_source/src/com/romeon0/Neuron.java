package com.romeon0;



/**
 * Created by Romeon0 on 11/7/2017.
 */
public class Neuron {
    private double weights[];
    private double inputValue;
    private double activationValue;
    private double exitValue;
    private double finalValue;
    private double bias=0.0;

    public Neuron(double inputValue, double[] weights){
        this.inputValue = inputValue;
        this.weights = weights;
    }


    public double getWeight(int index) {
        if(weights==null) throw new NullPointerException("Null weights!");
        if(index>weights.length-1) throw new IndexOutOfBoundsException("Index>weights.lenght!");
        return weights[index];
    }
    public double[] getWeights() {
        return weights;
    }
    public int getNrWeights() {
        if(weights==null) return 0;
        return weights.length;
    }
    public double getBias(){return bias;}


    public void setWeight( int index, double weight) {
        this.weights[index] = weight;
    }
    public void setWeights(double weights[]) throws IllegalArgumentException {
        if(weights==null)
            throw new IllegalArgumentException("Weights can't be null!");
        else if(this.weights!=null){
            int len1 = this.weights.length;
            int len2 = weights.length;
            if(len1<len2)
                throw new IllegalArgumentException(
                        String.format("Too small weights array! Needs/has: (%d/%d)", len1,len2));
        }
        this.weights = weights;
    }


    public void setFinalValue(double finalValue) {
        this.finalValue = finalValue;
    }

    public void setInputValue(double inputValue) {
        this.inputValue = inputValue;
    }

    public void setActivationValue(double activationValue) {
        this.activationValue = activationValue;
    }

    public void setExitValue(double exitValue) {
        this.exitValue = exitValue;
    }

    public double getInputValue() {
        return inputValue;
    }
    public double getActivationValue() {
        return activationValue;
    }
    public double getExitValue() {
        return exitValue;
    }
    public double getFinalValue() {
        return finalValue;
    }
    public void setBias(double bias){this.bias = bias;}
}
