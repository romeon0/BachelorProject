package com.romeon0;

import java.util.List;
import java.util.Random;

/**
 * Created by Romeon0 on 4/13/2018.
 */
public interface DataProcessor {
    //forward propagate
    default void forwardPropagate(List<Layer> layers, double[][] inputImg) {
        Helper h = new Helper();
        double[][] filter = null;

        //input layer
        double[][] img = inputImg;
        ((InputLayer) layers.get(0)).setOutputImage(img);

        //     System.out.println("Input image: ");
        //     h.showMatrix(inputImg);

        //other layers
        Layer.LAYER_TYPE prevLayerType = layers.get(0).getType();
        for (int nrLayer = 1; nrLayer < layers.size(); ++nrLayer) {
            Layer.LAYER_TYPE layerType = layers.get(nrLayer).getType();

            if (layerType == Layer.LAYER_TYPE.POOL) {//pool layer
                ((PoolLayer) layers.get(nrLayer)).setInputImage(img);
                img = h.poolMatrix2x2(img);
                ((PoolLayer) layers.get(nrLayer)).setOutputImage(img);
//                System.out.println("Pool image: ");
//               h.showMatrix(img);
            } else if (layerType == Layer.LAYER_TYPE.CONV) {//convolution layer
                filter = ((ConvLayer) layers.get(nrLayer)).getFilter();
                ((ConvLayer) layers.get(nrLayer)).setInputImage(img);
                img = h.convolvePixelMatrix3x3(img, filter);
                ((ConvLayer) layers.get(nrLayer)).setOutputImage(img);
//                System.out.println("Conv image: ");
//                h.showMatrix(img);
                //               System.out.println("Conv image filter: ");
                //               h.showMatrix(filter);
            } else if (layerType == Layer.LAYER_TYPE.FCL && prevLayerType != Layer.LAYER_TYPE.FCL) {//input FCL layer
                double[] values = h.matrixToVector(img);
                ((FCLayer) layers.get(nrLayer)).setOutputValues(values);
            } else if (layerType == Layer.LAYER_TYPE.FCL) {//other FCL layers
                //forward propagate FC layers
                for (int nrFCLayer = nrLayer; nrFCLayer < layers.size(); ++nrFCLayer) {
                    int nrNeurons = ((FCLayer) layers.get(nrFCLayer)).getNrNeurons();
                    for (int neuronNumber = 0; neuronNumber < nrNeurons; ++neuronNumber) {
                        calculateInputValue(layers, nrFCLayer, neuronNumber);
                        calculateActivationValue(layers, nrFCLayer, neuronNumber);
                        calculateExitValue(layers, nrFCLayer, neuronNumber);

                        //  double input = ((FCLayer) layers.get(nrFCLayer)).getNeuron(neuronNumber).getInputValue();
                        //  double act = ((FCLayer) layers.get(nrFCLayer)).getNeuron(neuronNumber).getActivationValue();
                        // double out = ((FCLayer) layers.get(nrFCLayer)).getNeuron(neuronNumber).getFinalValue();
                    }
                }
                break;//processed all layers, forward propagation finished
            }
            prevLayerType = layerType;
        }
    }
    default void calculateInputValue(List<Layer> layers, int layerNumber, int neuronNumber) {
        FCLayer layer = (FCLayer)layers.get(layerNumber);
        FCLayer previousLayer = (FCLayer)layers.get(layerNumber-1);
        Neuron neuron = layer.getNeuron(neuronNumber);

        //calculate probabilities for dropout
        Random rand = new Random();
        double[] dropoutProbabilities = new double[previousLayer.getNrNeurons()];
        for(int a =0; a < previousLayer.getNrNeurons(); ++a){
            dropoutProbabilities[a] = rand.nextDouble();
            //      System.out.print(dropoutProbabilities[a]+" ");
        }
        //   System.out.println();

        int inputFunction = layer.getInputFunction();
        final double[] globalInputValue = {0.0};
        switch (inputFunction) {
            case FCLayer.INPUTFUNC_SUM:
                globalInputValue[0] = 0.0;
                for(int a =0; a < previousLayer.getNrNeurons(); ++a){
                    Neuron n = previousLayer.getNeuron(a);
                    double value = n.getFinalValue() * neuron.getWeight(a);
                    //         value = value>dropoutProb?0:value;
                    globalInputValue[0] = globalInputValue[0] + value;
                }
                break;
            case FCLayer.INPUTFUNC_PRODUCT:
                globalInputValue[0] = 1.0;
                for(int a =0; a < previousLayer.getNrNeurons(); ++a){
                    Neuron n = previousLayer.getNeuron(a);
                    globalInputValue[0] = globalInputValue[0] * (n.getExitValue() * neuron.getWeight(a));
                }
                break;
            case FCLayer.INPUTFUNC_MIN:
                globalInputValue[0] = 1000.0;
                for(int a =0; a < previousLayer.getNrNeurons(); ++a){
                    Neuron n = previousLayer.getNeuron(a);
                    double val = n.getExitValue() * neuron.getWeight(a);
                    if(val<globalInputValue[0])
                        globalInputValue[0] = val;
                }
                break;
            case FCLayer.INPUTFUNC_MAX:
                globalInputValue[0] = -1000.0;
                for(int a =0; a < previousLayer.getNrNeurons(); ++a){
                    Neuron n = previousLayer.getNeuron(a);
                    double val = n.getExitValue() * neuron.getWeight(a);
                    if(val>globalInputValue[0])
                        globalInputValue[0] = val;
                }
                break;
            default: System.out.println("Some error input function");
        }
        double bias = ((FCLayer)layers.get(layerNumber)).getNeuron(neuronNumber).getBias();
        ((FCLayer)layers.get(layerNumber)).getNeuron(neuronNumber).setInputValue(globalInputValue[0]+bias);
    }
    default void calculateActivationValue(List<Layer> layers, int layerNumber, int neuronNumber) {
        FCLayer layer = (FCLayer)layers.get(layerNumber);
        Neuron neuron = layer.getNeuron(neuronNumber);
        int activationFunction = layer.getActivationFunction();
        double  a = layer.getA();
        double tetha = layer.getTetha();
        double g = layer.getG();
        double inputValue = neuron.getInputValue();
        double exitValue=inputValue;
        switch (activationFunction) {
            case FCLayer.ACTFUNC_LINEAR:
                if (inputValue < -a) {
                    exitValue = -1;
                } else if (inputValue > a) {
                    exitValue = 1;
                } else {
                    if (a == 0) a = 1.0;
                    exitValue = inputValue / a;
                }
                break;
            case FCLayer.ACTFUNC_STEP:
                if (inputValue >= 0)
                    exitValue = 1;
                else
                    exitValue = 0;
                break;
            case FCLayer.ACTFUNC_SIGMOID:
                if (exitValue < -45)
                    exitValue = 0;
                else if (exitValue > 45)
                    exitValue = 1;
                else
                    exitValue = 1.0 / (1.0 + Math.pow(Math.E, -g * (exitValue - tetha)));
                break;
            case FCLayer.ACTFUNC_HYPERBOLIC:
                double one = Math.pow(Math.E, g * (inputValue - tetha));
                double two = Math.pow(Math.E, -g * (inputValue - tetha));
                exitValue = (one - two) / (one + two);
                break;
            case FCLayer.ACTFUNC_SIGN:
                if (inputValue >= 0)
                    exitValue = 1;
                else
                    exitValue = -1;
                break;
            case FCLayer.ACTFUNC_IDENTITY:
                exitValue = inputValue;
        }
        ((FCLayer)layers.get(layerNumber)).getNeuron(neuronNumber).setActivationValue(exitValue);
    }
    default void calculateExitValue(List<Layer> layers, int layerNumber, int neuronNumber) {
        FCLayer layer = (FCLayer)layers.get(layerNumber);
        Neuron neuron = layer.getNeuron(neuronNumber);
        int activationFunction = layer.getActivationFunction();
        double activationValue = neuron.getActivationValue();
        double exitValue=activationValue;


        //if exit value need to be binary change it, else activationValue=exitValue=finalValue
        if (layer.isBinary()) {
            switch (activationFunction) {
                case FCLayer.ACTFUNC_SIGMOID:
                    if (activationValue >= 0.5)
                        exitValue = 1.0;
                    else if(activationValue<0.5)
                        exitValue = 0.0;
                    break;
                case FCLayer.ACTFUNC_HYPERBOLIC:
                    if (activationValue > 0.0)
                        exitValue = 1;
                    else
                        exitValue = -1;
                    break;
                case FCLayer.ACTFUNC_LINEAR:
                    if (activationValue >= 0.0)
                        exitValue = 1;
                    else
                        exitValue = -1;
            }
        }

        ((FCLayer)layers.get(layerNumber)).getNeuron(neuronNumber).setExitValue(exitValue);
        ((FCLayer)layers.get(layerNumber)).getNeuron(neuronNumber).setFinalValue(exitValue);
    }

    boolean isRunning();
}
