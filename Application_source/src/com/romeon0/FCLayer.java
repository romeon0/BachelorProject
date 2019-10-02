package com.romeon0;

import java.util.List;

/**
 * Created by Romeon0 on 11/7/2017.
 */
public class FCLayer implements Layer {
    private List<Neuron> neurons;
    private int inputFunction=INPUTFUNC_SUM;
    private int activationFunction=ACTFUNC_SIGMOID;
    private boolean isBinary=false;

    private double a=1.0;
    private double tetha=0.0;
    private double g=1.0;

    //constants
    public final static int INPUTFUNC_SUM=0;
    public final static int INPUTFUNC_PRODUCT=1;
    public final static int INPUTFUNC_MIN=2;
    public final static int INPUTFUNC_MAX=3;
    public final static int ACTFUNC_LINEAR=0;
    public final static int ACTFUNC_SIGN=1;
    public final static int ACTFUNC_SIGMOID=2;
    public final static int ACTFUNC_HYPERBOLIC =3;
    public final static int ACTFUNC_STEP =4;
    public final static int ACTFUNC_IDENTITY=5;


    public double getG() {
        return g;
    }
    public void setG(double g) {
        this.g = g;
    }
    public double getA() {
        return a;
    }
    public void setA(double a) {
        this.a = a;
    }
    public double getTetha() {
        return tetha;
    }
    public void setTetha(double tetha) {
        this.tetha = tetha;
    }
    public FCLayer(List<Neuron> neurons){
        this.neurons = neurons;
    }

    //getters-------------------------
    public boolean isBinary() {
        return isBinary;
    }

    public int getNrNeurons(){
        return neurons.size();
    }

    public List<Neuron> getNeurons(){
        return neurons;
    }
    public Neuron getNeuron(int id){
        return neurons.get(id);
    }
    public int getActivationFunction() {
        return activationFunction;
    }
    public int getInputFunction() {
        return inputFunction;
    }
    public boolean getIsBinary() {
        return isBinary;
    }
    public LAYER_TYPE getType(){return LAYER_TYPE.FCL;}
    //-------------------------------------

    //setters------------------------
    public void setActivationFunction(int activationFunction) {
        this.activationFunction = activationFunction;
    }
    public void setInputFunction(int inputFunction) {
        this.inputFunction = inputFunction;
    }
    public void setBinary(boolean binary) {
        isBinary = binary;
    }
    public void setNeuronValue(int neuronNumber, double value){
        neurons.get(neuronNumber).setInputValue(value);
    }
    public void setNeuronWeight(int neuronNumber, double weight){
    }
    public void setNeuronValues(int neuronNumber, double value, double weight){
        neurons.get(neuronNumber).setInputValue(value);
    }
    public void setInputValues(double[] inputValues) {
        if(inputValues.length<getNrNeurons())
            throw new ArrayIndexOutOfBoundsException("Input vector length smaller than number of neurons!");
        for(int a=0; a < inputValues.length; ++a){
            setNeuronValue(a,inputValues[a]);
        }
    }

    public void setOutputValues(double[] outputValues) {
        int len1 = outputValues.length;
        int len2 = getNrNeurons();
        if(len1!=len2)
            throw new ArrayIndexOutOfBoundsException(
                    String.format("Input vector length smaller or higher than number of neurons! Needs/has: %d/%d", len2,len1));


        for(int a=0; a < neurons.size(); ++a){
            neurons.get(a).setFinalValue(outputValues[a]);
        }
    }

    //-------------------------------------
}
