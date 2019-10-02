package com.romeon0;


import javafx.application.Platform;
import javafx.util.Pair;

import java.io.*;
import java.util.*;

/**
 * Created by Romeon0 on 12/13/2017.
 */
public class Training extends Observable implements DataProcessor{
    private double maxError=0.0001;
    private double learningRate=0.2;
    private int nrEpochs=100;
    private List<Layer> layers;
    private double currEpochError;
    private List<Pair<Integer,double[][]>> dataSet;
    private List<Observer> observers;
    private String datasetPath;
    private boolean running=false;

    //thread of training process
    private class TrainingRunnable implements Runnable {
        public void run() {
            int nrOutputNeurons = ((FCLayer) layers.get(layers.size() - 1)).getNrNeurons();
            Helper h = new Helper();

            for (int nrDataFile = 1; nrDataFile <= 1; ++nrDataFile) {
                if(dataSet.size()==0) {
                    for(Observer o : observers){
                        Integer object=0;
                        WindowUpdate update = new WindowUpdate(WindowUpdateType.TRAINING_LOADDATA,object);
                        WindowUpdate update2 = new WindowUpdate(WindowUpdateType.TRAINING_STARTED, null);
                        o.update(Training.this,update);
                        o.update(Training.this, update2);
                    }
                    dataSet = h.readDataFile(datasetPath);
                    for(Observer o : observers){
                        Integer object=1;
                        WindowUpdate update = new WindowUpdate(WindowUpdateType.TRAINING_LOADDATA,object);
                        o.update(Training.this,update);
                    }
                }else {
                	 WindowUpdate update2 = new WindowUpdate(WindowUpdateType.TRAINING_STARTED, null);
                	 for(Observer o : observers){
                         o.update(Training.this, update2);
                     }
                }
                
                //training process
                double[] correctAnswer = new double[26];
                for (int nrEpoch = 1; nrEpoch <= nrEpochs; ++nrEpoch) {
                    Iterator<Pair<Integer, double[][]>> it = dataSet.iterator();
                    while (it.hasNext()) {
                        if(!running) break;

                        //update nr. epoch
                        int finalNrEpoch = nrEpoch;
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                WindowUpdate update = new WindowUpdate(WindowUpdateType.TRAINING_NREPOCH, finalNrEpoch);
                                for (Observer o : observers) {
                                    o.update(Training.this, update);
                                }
                            }
                        });

                        Pair<Integer, double[][]> element = it.next();
                        int labelId = element.getKey();
                        double[][] pixels = element.getValue();

                        //making correct answer
                        for (int b = 0; b < 26; ++b) {
                            if (labelId != b) correctAnswer[b] = 0.1f;
                            else correctAnswer[b] = 0.9f;
                        }

                        //forward process
                        forwardPropagate(layers, pixels);

                        //calculate step error
                        double stepError = 0;
                        FCLayer outLayer = (FCLayer) layers.get(layers.size() - 1);
                        for (int b = 0; b < nrOutputNeurons; ++b) {
                            double desired = correctAnswer[b];
                            double actual = outLayer.getNeuron(b).getFinalValue();
                            stepError += (0.5 * Math.pow(desired - actual, 2));
                        }
                        currEpochError += stepError;

                        //backward process
                        backPropagate(layers, correctAnswer, learningRate);
                    }

                    //calculate total error
                    currEpochError /= dataSet.size();

                    if(running) {
                        //update window
                        int finalNrEpoch = nrEpoch;
                        double finalCurrEpochError = currEpochError;
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                Pair<Integer, Double> object = new Pair<Integer, Double>(finalNrEpoch, finalCurrEpochError);
                                WindowUpdate update = new WindowUpdate(WindowUpdateType.TRAINING_EPOCHERROR, object);
                                for (Observer o : observers) {
                                    o.update(Training.this, update);
                                }
                            }
                        });
                    } else break;

                    //reset epoch error -> do new epoch
                    currEpochError = 0;
                }
                
                boolean normalStopped = running?false:true;
                WindowUpdate update2 = new WindowUpdate(WindowUpdateType.TRAINING_STOPPED, normalStopped);
	           	for(Observer o : observers){
	           		o.update(Training.this, update2);
	            }
	           	running=false;
            }
        }
    }

    //constructor
    Training(List<Layer> layers){
        currEpochError=0.0;
        dataSet = new ArrayList<>();
        observers = new ArrayList<>();
        this.layers = layers;
    }

    //getters
    public int getNrExamples() {
        return dataSet.size();
    }
    public double getMaxError() {
        return maxError;
    }
    public int getNrEpochs() {
        return nrEpochs;
    }
    public double getLearningRate() {
        return learningRate;
    }
    public double getCurrEpochError(){
        return currEpochError;
    }
    public List<Layer> getLayers(){return layers;}
    @Override
    public boolean isRunning() {
        return running;
    }

    //setters
    public void setMaxError(double maxError) {
        this.maxError = maxError;
    }
    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }
    public void setNrEpochs(int nrEpochs) {
        this.nrEpochs = nrEpochs;
    }


    //back propagate process
    private static void backPropagate(List<Layer> layers, double[] correctAnswer, double learningRate) {
        Helper h = new Helper();
        Layer.LAYER_TYPE prevLayerType = Layer.LAYER_TYPE.NONE;
        Pair<double[], double[][]> fclResult;
        Pair<double[][], double[][]> convResult;
        List<double[]> errors = new ArrayList<>();//neuron errors for backpropagation
        List<double[][]> convErrors = new ArrayList<>();//neuron errors for backpropagation in conv and pool layers
        List<double[][]> weightErrors = new ArrayList<>();//weight errors for updating weights
        List<double[][]> filterErrors = new ArrayList<>();//weight errors for updating weights
        double[] prevErrors = null;
        double[][] prevConvErrors = null;

        for (int nrLayer = layers.size() - 1; nrLayer >= 0; --nrLayer) {
            Layer.LAYER_TYPE layerType = layers.get(nrLayer).getType();

            if (layerType == Layer.LAYER_TYPE.POOL) {//pool layer
                if (prevLayerType == Layer.LAYER_TYPE.FCL) {
                    double[][] i = ((PoolLayer) layers.get(nrLayer)).getOutputImage();
                    int imgHeight = i.length;
                    int imgWidth = i[0].length;
                    try {
                        prevConvErrors = h.vectorToMatrix(prevErrors, imgWidth, imgHeight);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }
                convResult = backpropPoolLayer(layers, nrLayer, prevConvErrors);
                convErrors.add(0, convResult.getKey());
                prevConvErrors = convResult.getKey();
            } else if (layerType == Layer.LAYER_TYPE.CONV) {//convolution layer
                if (prevLayerType == Layer.LAYER_TYPE.FCL) {
                    double[][] i = ((ConvLayer) layers.get(nrLayer)).getOutputImage();
                    int imgHeight = i.length;
                    int imgWidth = i[0].length;
                    try {
                        prevConvErrors = h.vectorToMatrix(prevErrors, imgWidth, imgHeight);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                convResult = backpropConvLayer(layers, nrLayer, prevConvErrors, learningRate);
                convErrors.add(0, convResult.getKey());
                filterErrors.add(0, convResult.getValue());
                prevConvErrors = convResult.getKey();
            } else if (layerType == Layer.LAYER_TYPE.FCL && prevLayerType == Layer.LAYER_TYPE.NONE) {//last FCL layer
                fclResult = backpropLastFCLayer(layers, correctAnswer, learningRate);
                errors.add(0, fclResult.getKey());
                weightErrors.add(0, fclResult.getValue());
                prevErrors = fclResult.getKey();
            } else if (layerType == Layer.LAYER_TYPE.FCL) {//FCL layer (hidden or input)
                fclResult = backpropFCLayer(layers, nrLayer, prevErrors, learningRate);
                errors.add(0, fclResult.getKey());
                weightErrors.add(0, fclResult.getValue());
                prevErrors = fclResult.getKey();

            }
            prevLayerType = layerType;
        }

//
        //---UPDATE WEIGHTS
        int fclIterator = 0, convIterator = 0;
        for (int nrLayer = 0; nrLayer < layers.size(); ++nrLayer) {
            Layer.LAYER_TYPE layerType = layers.get(nrLayer).getType();

            if (layerType == Layer.LAYER_TYPE.CONV) {
                double[][] filter = ((ConvLayer) layers.get(nrLayer)).getFilter();
                double[][] fErrors = filterErrors.get(convIterator++);
                for (int y = 0; y < 3; ++y) {
                    for (int x = 0; x < 3; ++x) {
                        double currWeight = filter[y][x];
                        currWeight -= learningRate * fErrors[y][x];
                        filter[y][x] = currWeight;
                    }
                }
                ((ConvLayer) layers.get(nrLayer)).setFilter(filter);
            } else if (layerType == Layer.LAYER_TYPE.FCL) {
                if (prevLayerType == Layer.LAYER_TYPE.FCL) {//hidden or output layer that has weights
                    FCLayer layer = (FCLayer) layers.get(nrLayer);
                    FCLayer prevLayer = (FCLayer) layers.get(nrLayer - 1);
                    int nrNeurons = layer.getNrNeurons();
                    int nrWeights = prevLayer.getNrNeurons();

                    double[][] wErrors = weightErrors.get(fclIterator++);
                    for (int nrNeuron = 0; nrNeuron < nrNeurons; ++nrNeuron) {
                        double[] newWeights = new double[nrWeights];
                        for (int nrWeight = 0; nrWeight < nrWeights; ++nrWeight) {
                            double currWeight = layer.getNeuron(nrNeuron).getWeight(nrWeight);
                            newWeights[nrWeight] = currWeight - learningRate * wErrors[nrNeuron][nrWeight];

                            //double momentum = 0.5;
                            //newWeights[nrWeight] = currWeight + learningRate * wErrors[nrNeuron][nrWeight] + momentum * currWeight;
                        }
                        try {
                            ((FCLayer) layers.get(nrLayer)).getNeuron(nrNeuron).setWeights(newWeights);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    fclIterator++;
                }//input FCL layer, no weights
            }

            prevLayerType = layerType;
        }
        //---

        h = null;
        prevLayerType = null;
        fclResult = null;
        convResult = null;
        errors = null;
        convErrors = null;
        weightErrors = null;
        filterErrors = null;
        prevErrors = null;
        prevConvErrors = null;
    }
    private static Pair<double[], double[][]> backpropLastFCLayer(List<Layer> layers, double[] correctAnswer, double learningRate) {
        double[] errors;
        double[][] weightErrors;
        double netRatio, outRatio, weightRatio;
        int nrLayer = layers.size() - 1;
        int nrNeurons = ((FCLayer) layers.get(nrLayer)).getNrNeurons();
        Layer.LAYER_TYPE prevLayerType = layers.get(nrLayer - 1).getType();
        int nrWeights = prevLayerType == Layer.LAYER_TYPE.FCL ? ((FCLayer) layers.get(nrLayer - 1)).getNrNeurons() : 0;

        errors = new double[nrNeurons];
        weightErrors = new double[nrNeurons][nrWeights];
        FCLayer layer = (FCLayer) layers.get(layers.size() - 1);
        for (int nrNeuron = 0; nrNeuron < nrNeurons; ++nrNeuron) {
            Neuron neuron = layer.getNeuron(nrNeuron);
            double actualOutput = neuron.getFinalValue();
            double desiredOutput = correctAnswer[nrNeuron];
            //out ratio
            outRatio = (actualOutput - desiredOutput);
            //net ratio
            netRatio = actualOutput * (1 - actualOutput);
            //neuron errors
            errors[nrNeuron] = outRatio * netRatio;//for next FCLayer
            //weight ratio
            if(nrWeights!=0) {
                FCLayer prevLayer = (FCLayer) layers.get(layers.size() - 2);
                for (int nrWeight = 0; nrWeight < nrWeights; ++nrWeight) {
                    weightRatio = prevLayer.getNeuron(nrWeight).getFinalValue();
                    double weightImpact = errors[nrNeuron]
                            * weightRatio;
                    weightErrors[nrNeuron][nrWeight] = weightImpact;
                }
            }
        }

        Pair<double[], double[][]> result = new Pair<>(errors, weightErrors);
        return result;
    }
    private static Pair<double[], double[][]> backpropFCLayer(List<Layer> layers, int nrLayer, double[] prevErrors, double learningRate) {
        double[] errors = null;
        double[][] weightErrors = null;
        double netRatio, outRatio, weightRatio;
        int nrNeurons = ((FCLayer) layers.get(nrLayer)).getNrNeurons();
        int nrWeights = 0;
        if(layers.get(nrLayer-1).getType()== Layer.LAYER_TYPE.FCL)
            nrWeights = ((FCLayer) layers.get(nrLayer - 1)).getNrNeurons();

        //----calculate weight and neuron errors input<---hidden<---...<--hiddenN
        errors = new double[nrNeurons];
        weightErrors = new double[nrNeurons][nrWeights];
        FCLayer layer = (FCLayer) layers.get(nrLayer);
        for (int nrNeuron = 0; nrNeuron < nrNeurons; ++nrNeuron) {
            Neuron currNeuron = layer.getNeuron(nrNeuron);
            double exitValue = currNeuron.getFinalValue();
            //outRatio: sum all previous errors weighted with necessary weight
            outRatio = 0;
            int nrPrevNeurons = ((FCLayer) layers.get(nrLayer + 1)).getNrNeurons();
            for (int nrPrevNeuron = 0; nrPrevNeuron < nrPrevNeurons; ++nrPrevNeuron) {
                double weight = ((FCLayer) layers.get(nrLayer + 1)).getNeuron(nrPrevNeuron).getWeight(nrNeuron);
                outRatio += prevErrors[nrPrevNeuron] * weight;
            }
            //net ratio
            netRatio = exitValue * (1 - exitValue);
            //neuron errors
            errors[nrNeuron] = outRatio * netRatio;
            //weight errors
            for (int nrWeight = 0; nrWeight < nrWeights; ++nrWeight) {
                weightRatio = ((FCLayer) layers.get(nrLayer - 1)).getNeuron(nrWeight).getFinalValue();
                double weightImpact = errors[nrNeuron]
                        * weightRatio;
                weightErrors[nrNeuron][nrWeight] = weightImpact;
            }
        }

        Pair<double[], double[][]> result = new Pair<>(errors, weightErrors);
        return result;
    }
    private static Pair<double[][], double[][]> backpropConvLayer(List<Layer> layers, int nrLayer, double[][] prevConvErrors, double learningRate) {
        Helper h = new Helper();
        ConvLayer layer = (ConvLayer) layers.get(nrLayer);
        double[][] filter = layer.getFilter();
        double[][] outputImg = layer.getOutputImage();
        double[][] inputImg = layer.getInputImage();

        //---calculate image errors
        double[][] imgErrors = h.fullConvolve3x3(prevConvErrors, filter);

        //---calculate filter weight errors
        double[][] filterErrors = new double[3][3];
        for (int nrWeightY = 0; nrWeightY < 3; ++nrWeightY) {
            for (int nrWeightX = 0; nrWeightX < 3; ++nrWeightX) {
                filterErrors[nrWeightY][nrWeightX] = 0;
                for (int a = 0; a < prevConvErrors.length; ++a) {
                    for (int b = 0; b < prevConvErrors[0].length; ++b) {
                        filterErrors[nrWeightY][nrWeightX] += inputImg[nrWeightY + a][nrWeightX + b] * prevConvErrors[a][b];
                    }
                }
            }
        }

        return new Pair<double[][], double[][]>(imgErrors, filterErrors);
    }
    public static Pair<double[][],double[][]> backpropPoolLayer(List<Layer> layers, int nrLayer, double[][] prevErrors) {
        Helper h = new Helper();
        //---calculate convolution and pooling layer deltas and weight errors
        //calculate errors of Pooling layer
        int inputImgWidth = 0;
        int inputImgHeight = 0;



        inputImgHeight = ((PoolLayer)layers.get(nrLayer)).getInputImage().length;
        inputImgWidth = ((PoolLayer)layers.get(nrLayer)).getInputImage()[0].length;
        double[][] inputImg = ((PoolLayer)layers.get(nrLayer)).getInputImage();
        double[][] poolingErrors = new double[inputImgHeight][inputImgWidth];
        for (int y = 0,i=0; y < inputImg.length - 1; y+=2,++i) {
            for (int x = 0, j = 0; x < inputImg[0].length - 1; x += 2, ++j) {
                boolean[][] maxPoolMatrix = h.maxPoolOp2x2(inputImg[y][x],
                        inputImg[y][x + 1],
                        inputImg[y + 1][x],
                        inputImg[y + 1][x + 1]);
                for (int poolY = 0; poolY < 2; ++poolY) {
                    for (int poolX = 0; poolX < 2; ++poolX) {
                        if (!maxPoolMatrix[poolY][poolX])
                            poolingErrors[y + poolY][x + poolX] = 0;
                        else
                            poolingErrors[y + poolY][x + poolX] = prevErrors[i][j];
                    }
                }
            }
        }

        return new Pair<>(poolingErrors,null);
    }

    //controls
    void start(String dataPath) {
        running=true;
        datasetPath = dataPath;
        TrainingRunnable runnable = new TrainingRunnable();
        Thread thread = new Thread(runnable);
        thread.start();
    }
    void stop(){
        running=false;
    }

    //observable for communicating with window
    @Override
    public synchronized void addObserver(Observer o) {
        observers.add(o);
    }
    @Override
    public synchronized void deleteObserver(Observer o) {
        observers.remove(o);
    }
}
