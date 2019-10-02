package com.romeon0;


import javafx.util.Pair;

import java.util.*;

/**
 * Created by Romeon0 on 4/19/2018.
 */
class Recognizing implements DataProcessor{
    private boolean running;
    Recognizing(){
        running=false;
    }

    List<Pair<Double, String>> recognize(List<double[][]> imgMatrices, List<Layer> layers) {
        double maxOutputValue;
        int[] winLetterIdxes = new int[4];
        FCLayer layer = (FCLayer) layers.get(layers.size() - 1);
        int winLetterIdx = 0;
        int nrOutputNeurons = ((FCLayer) layers.get(layers.size() - 1)).getNrNeurons();

        List<String> correctStrings = new ArrayList<>();
        List<Double> correctCollectedProbabilities = new ArrayList<>();
        for(int a=0; a < 4; ++a) {
            correctStrings.add("");
            correctCollectedProbabilities.add(0.0);
        }

        for(int nrImage=0; nrImage < imgMatrices.size(); ++nrImage) {
            double[][] imgMatrix = imgMatrices.get(nrImage);
            maxOutputValue=-100;

            //forward propagate image
            forwardPropagate(layers, imgMatrix);

            //get output values
            double[] outputValues = new double[26];
            for (int nrOutput = 0; nrOutput < nrOutputNeurons; ++nrOutput) {
                double actual = layer.getNeuron(nrOutput).getFinalValue();
                outputValues[nrOutput] = actual;
            }

            //-----get win letters
            //win letter 1
            for (int nrOutput = 0; nrOutput < nrOutputNeurons; ++nrOutput) {
                double actual = outputValues[nrOutput];
                if (maxOutputValue < actual) {
                    maxOutputValue = actual;
                    winLetterIdx = nrOutput;
                }
            }
            winLetterIdxes[0] = winLetterIdx;
            correctCollectedProbabilities.set(0,correctCollectedProbabilities.get(0)+outputValues[winLetterIdx]);
            outputValues[winLetterIdx] = maxOutputValue = -100;
            //win letter 2
            for (int nrOutput = 0; nrOutput < nrOutputNeurons; ++nrOutput) {
                double actual = outputValues[nrOutput];
                if (maxOutputValue < actual) {
                    maxOutputValue = actual;
                    winLetterIdx = nrOutput;
                }
            }
            winLetterIdxes[1] = winLetterIdx;
            correctCollectedProbabilities.set(1,correctCollectedProbabilities.get(1)+outputValues[winLetterIdx]);
            outputValues[winLetterIdx] = maxOutputValue = -100;
            //win letter 3
            for (int nrOutput = 0; nrOutput < nrOutputNeurons; ++nrOutput) {
                double actual = outputValues[nrOutput];
                if (maxOutputValue < actual) {
                    maxOutputValue = actual;
                    winLetterIdx = nrOutput;
                }
            }
            winLetterIdxes[2] = winLetterIdx;
            correctCollectedProbabilities.set(2,correctCollectedProbabilities.get(2)+outputValues[winLetterIdx]);
            outputValues[winLetterIdx] = maxOutputValue = -100;
            //win letter 4
            for (int nrOutput = 0; nrOutput < nrOutputNeurons; ++nrOutput) {
                double actual = outputValues[nrOutput];
                if (maxOutputValue < actual) {
                    maxOutputValue = actual;
                    winLetterIdx = nrOutput;
                }
            }
            winLetterIdxes[3] = winLetterIdx;
            correctCollectedProbabilities.set(3,correctCollectedProbabilities.get(3)+outputValues[winLetterIdx]);


            String letter1 = getMappedLetter(winLetterIdxes[0]);
            String letter2 = getMappedLetter(winLetterIdxes[1]);
            String letter3 = getMappedLetter(winLetterIdxes[2]);
            String letter4 = getMappedLetter(winLetterIdxes[3]);


            correctStrings.set(0, correctStrings.get(0)+letter1);
            correctStrings.set(1, correctStrings.get(1)+letter2);
            correctStrings.set(2, correctStrings.get(2)+letter3);
            correctStrings.set(3, correctStrings.get(3)+letter4);
        }

        double prob1 = correctCollectedProbabilities.get(0)/imgMatrices.size();
        double prob2 = correctCollectedProbabilities.get(1)/imgMatrices.size();
        double prob3 = correctCollectedProbabilities.get(2)/imgMatrices.size();
        double prob4 = correctCollectedProbabilities.get(3)/imgMatrices.size();


       /* for(int a=0; a < 4; ++a){
            System.out.println("CorrectLetter"+a+1+": " + correctStrings.get(a));
        }
        for(int a=0; a < 4; ++a){
            System.out.println("CorrectProb"+a+1+": " + correctCollectedProbabilities.get(a));
        }
        System.out.println("-----------");*/

        List<Pair<Double, String>> result = new ArrayList<>();
        result.add(new Pair<Double, String>(prob1,correctStrings.get(0)));
        result.add(new Pair<Double, String>(prob2,correctStrings.get(1)));
        result.add(new Pair<Double, String>(prob3,correctStrings.get(2)));
        result.add(new Pair<Double, String>(prob4,correctStrings.get(3)));
        return result;
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

    @Override
    public boolean isRunning() {
        return running;
    }

    void stop(){running=false;}
}
