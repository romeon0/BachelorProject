package com.romeon0;


import javafx.application.Platform;
import javafx.util.Pair;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Created by Romeon0 on 4/13/2018.
 */
public class Testing extends Observable implements DataProcessor{
    private boolean running=false;
    private TestingThread thread;
    private String dataPath;
    private List<Pair<Integer, double[][]>> dataSet;
    private List<Layer> layers;
    private double maxTestError=0.2;
    private int nrGoodAnswers=0;
    private List<Observer> observers;
    private boolean destroyThread=false;

    //thread of testing process
    class TestingThread extends Thread implements Runnable {
        @Override
        public void run() {
            running=true;
            int nrOutputNeurons = ((FCLayer) layers.get(layers.size() - 1)).getNrNeurons();
            Helper h = new Helper();
            int[] wrongLabels = new int[26];//nr. of wrong examples in each class
            int[] nrLabels = new int[26];//nr. of examples from each class
            nrGoodAnswers=0;

            for (int nrDataFile = 1; nrDataFile <= 1; ++nrDataFile) {
                //read file with examples
                if(dataSet.size()==0) {
                    for(Observer o : observers){
                        Integer object=0;
                        WindowUpdate update = new WindowUpdate(WindowUpdateType.TESTING_LOADDATA,object);
                        WindowUpdate update2 = new WindowUpdate(WindowUpdateType.TESTING_STARTED, null);
                        o.update(Testing.this,update);
                        o.update(Testing.this, update2);
                    }
                    dataSet = h.readDataFile(dataPath);
                    for(Observer o : observers){
                        Integer object=1;
                        WindowUpdate update = new WindowUpdate(WindowUpdateType.TESTING_LOADDATA,object);
                        o.update(Testing.this,update);
                    }
                }else {
                    WindowUpdate update2 = new WindowUpdate(WindowUpdateType.TESTING_STARTED, null);
                	 for(Observer o : observers){
                         o.update(Testing.this, update2);
                     }
                }

                for(Pair<Integer,double[][]> data: dataSet) {
                    if(!running) break;
                    if(destroyThread) break;
                    int labelId = data.getKey();
                    double[][] pixels = data.getValue();

                    //forward propagate data in network
                    forwardPropagate(layers, pixels);

                    //get win letter index
                    double maxOutputValue = 0.0;
                    int winLetterIdx=-1;
                    FCLayer layer = (FCLayer) layers.get(layers.size() - 1);
                    for (int nrOutput = 0; nrOutput < nrOutputNeurons; ++nrOutput) {
                        double actual = layer.getNeuron(nrOutput).getFinalValue();
                        if (maxOutputValue < actual) {
                            maxOutputValue = actual;
                            winLetterIdx = nrOutput;
                        }
                    }

                    //check if network answered correct
                    if (winLetterIdx == labelId) ++nrGoodAnswers;
                    else wrongLabels[labelId] += 1;

                    //count nr. of examples in each class
                    nrLabels[labelId] += 1;

                    //add row to table on window
                    String winLetter = getMappedLetter(winLetterIdx);
                    String correctLetter = getMappedLetter(labelId);
                    Pair<String,String> object = new Pair<String,String>(winLetter,correctLetter);
                    WindowUpdate update = new WindowUpdate(WindowUpdateType.TESTING_TABLEVALUE,object);
                    for(Observer o: observers){
                        o.update(Testing.this,update);
                    }
                }

                
                if(destroyThread) running=false;
                
                //if user didn't stop testing process, show nr. of correct answered letters
                if(running) {
                    String object = String.format("%d/%d", nrGoodAnswers, dataSet.size());
                    WindowUpdate update = new WindowUpdate(WindowUpdateType.TESTING_NRCORRECTANSWERS, object);
                    
                    for (Observer o : observers) {
                        o.update(Testing.this, update);
                    }
                }

                //reset
                nrGoodAnswers = 0;
              
            	boolean normalStopped = running? true : false;
                WindowUpdate update2 = new WindowUpdate(WindowUpdateType.TESTING_STOPPED, normalStopped);
                for (Observer o : observers) {
                    o.update(Testing.this, update2);
                }
                running=false;
            }
        }
    }

    //constructor
    public Testing(){
        running=false;
        dataSet = new ArrayList<>();
        observers = new ArrayList<>();
    }

    //controls
    public void start(String dataPath, List<Layer> layers){
        this.layers = layers;
        this.dataPath = dataPath;
        if(!running){
            thread = new TestingThread();
            thread.start();
        }
    }
    public void stop(){
        running=false;
    }
    public void destroy(){
         dataSet.clear();
         destroyThread=true;
    }

    //getters
    @Override
    public boolean isRunning() {
        return running;
    }
    private String getMappedLetter(int idx) {
        String letter="";
        switch (idx){
            case 0: letter="A"; break;
            case 1: letter="B"; break;
            case 2: letter="C"; break;
            case 3: letter="D"; break;
            case 4: letter="E"; break;
            case 5: letter="F"; break;
            case 6: letter="G"; break;
            case 7: letter="H"; break;
            case 8: letter="I"; break;
            case 9: letter="J"; break;
            case 10: letter="K"; break;
            case 11: letter="L"; break;
            case 12: letter="M"; break;
            case 13: letter="N"; break;
            case 14: letter="O"; break;
            case 15: letter="P"; break;
            case 16: letter="R"; break;
            case 17: letter="Q"; break;
            case 18: letter="S"; break;
            case 19: letter="T"; break;
            case 20: letter="U"; break;
            case 21: letter="V"; break;
            case 22: letter="W"; break;
            case 23: letter="X"; break;
            case 24: letter="Y"; break;
            case 25: letter="Z"; break;
        }
        return letter;
    }

    //observable for communication with window
    @Override
    public synchronized void addObserver(Observer o) {
        observers.add(o);
    }
    @Override
    public synchronized void deleteObserver(Observer o) {
        observers.remove(o);
    }
}
