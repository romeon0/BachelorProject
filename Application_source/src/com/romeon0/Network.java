package com.romeon0;

import javafx.scene.Parent;
import javafx.scene.image.*;
import javafx.util.Pair;

import java.io.*;
import java.util.*;

/**
 * Created by Romeon0 on 2/28/2018.
 */
public class Network extends Observable implements Observer {
    private Training training;
    private Testing testing;
    private Parent windowRoot;
    private List<Layer> layers;
    private List<Observer> observers;
    private Recognizing recognizing;

    public Network(Parent windowRoot) {
        this.windowRoot = windowRoot;
        layers = new ArrayList<>();
        observers = new ArrayList<>();
        initDefaultNetwork();
        training = new Training(layers);
        training.addObserver(this);
        testing = new Testing();
        testing.addObserver(this);
        recognizing = new Recognizing();
    }

    //interface for window
    void startTesting(String dataPath) {
        if (!testing.isRunning())
            testing.start(dataPath, training.getLayers());
    }
    void stopTesting() {
        testing.stop();
    }
    void startTraining(String dataPath) {
        if (!training.isRunning())
            training.start(dataPath);
    }
    void stopTraining() {
        training.stop();
    }
    public void destroy() {
        if (training != null)
            training.stop();
        if (testing != null)
            testing.stop();
        if(recognizing!=null)
            recognizing.stop();
    }
    List<Pair<Double, String>> startRecognizing(List<double[][]> images) {
        if(!recognizing.isRunning()) {
            List<Pair<Double, String>> result = recognizing.recognize(images, training.getLayers());
            return result;
        }
        return null;
    }
    void save(File file){
        saveNetwork(file);
    }
    boolean load(File file){
        layers = loadNetwork(file);
        Helper h = new Helper();
        training = new Training(layers);

        return layers!=null;
    }
    boolean isRunningTraining(){return training.isRunning();}
    boolean isRunningTesting(){return testing.isRunning();}
    void setLearningRate(double learningRate){training.setLearningRate(learningRate);}
    void setMaxTrainingError(double err){training.setMaxError(err);}
    void setNrEpochs(int epochs){training.setNrEpochs(epochs);}

    //utils
    private void initDefaultNetwork() {
        layers = new ArrayList<>();
        InputLayer layer0 = new InputLayer();
        PoolLayer layer1 = new PoolLayer();
        FCLayer layer2 = initLayer(196,1,null,null);
        FCLayer layer3 = initLayer(100,196,null,null);
        FCLayer layer4 = initLayer(30,100,null,null);
        FCLayer layer5 = initLayer(26,30,null,null);
        layers.add(layer0);
        layers.add(layer1);
        layers.add(layer2);
        layers.add(layer3);
        layers.add(layer4);
        layers.add(layer5);
    }
    private FCLayer initLayer(int nrNeurons, int nrWeights, double[] inputValues,double[][] weights) {
        try {
            FCLayer layer = null;
            Random rand = new Random();
            ArrayList<Neuron> neurons = new ArrayList<>();
            for (int nrNeuron = 0; nrNeuron < nrNeurons; ++nrNeuron) {
                double inputValue = inputValues == null ? 0 : inputValues[nrNeuron];
                double[] weights1 = null;
                if (weights == null) {
                    weights1 = new double[nrWeights];
                    for (int nrWeight = 0; nrWeight < nrWeights; ++nrWeight) {
                        weights1[nrWeight] = rand.nextDouble() / Math.sqrt(nrNeurons);
                    }
                } else weights1 = weights[nrNeuron];
                Neuron neuron = new Neuron(inputValue, weights1);
                neurons.add(neuron);
            }
            layer = new FCLayer(neurons);
            return layer;
        }catch(Exception ex){System.out.println(ex.getMessage());}
        return null;
    }
    private void saveNetwork(File fileName){
        FileWriter writer=null;
        StringBuilder builder;
        try {
            writer = new FileWriter(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(int nrLayer=0; nrLayer < layers.size(); ++nrLayer){
            Layer abstrLayer = layers.get(nrLayer);
            if(abstrLayer.getType()== Layer.LAYER_TYPE.CONV) {
                ConvLayer layer = (ConvLayer) abstrLayer;
                double[][] filter = layer.getFilter();
                builder = new StringBuilder();

                for (int a = 0; a < filter.length; ++a)
                    for (int b = 0; b < filter[0].length; ++b) {
                        builder.append(filter[a][b]);
                        builder.append(" ");
                    }
                builder.append("\n");
                try {
                    writer.write("#Conv\n");
                    writer.write(builder.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(abstrLayer.getType()== Layer.LAYER_TYPE.POOL){
                PoolLayer layer = (PoolLayer)abstrLayer;
                try {
                    writer.write("#Pool\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(abstrLayer.getType()== Layer.LAYER_TYPE.FCL){
                FCLayer layer = (FCLayer)abstrLayer;
                builder = new StringBuilder();
                int nrNeurons = layer.getNrNeurons();
                int nrWeights = layer.getNeuron(0).getNrWeights();
                builder.append("#FCL ");
                builder.append(nrNeurons+" ");
                builder.append(nrWeights+"\n");
                if(nrWeights!=0)
                    for(int nrNeuron=0; nrNeuron < nrNeurons; ++nrNeuron){
                        Neuron neuron = layer.getNeuron(nrNeuron);
                        double[] weights = neuron.getWeights();
                        for(int nrWeight=0; nrWeight < nrWeights; ++nrWeight){
                            builder.append(weights[nrWeight]);
                            builder.append(" ");
                        }
                        builder.append("\n");
                    }
                try {
                    writer.write(builder.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private List<Layer> loadNetwork(File file){
        List<Layer> layers = new ArrayList<>();
        FileReader reader;
        String line;
        Scanner scanner;

        InputLayer inputLayer = new InputLayer();
        layers.add(inputLayer);

        try {
            reader = new FileReader(file);
            BufferedReader bReader = new BufferedReader(reader);

            while((line=bReader.readLine())!=null){
                if(line.startsWith("#Pool")){
                    PoolLayer layer = new PoolLayer();
                    layers.add(layer);
                    System.out.println("Pool layer");
                }else if(line.startsWith("#Conv")){
                    line = bReader.readLine();
                    scanner = new Scanner(line);
                    double[][] filter = new double[3][3];
                    for(int a=0; a < 3; ++a){
                        for(int b=0; b < 3; ++b){
                            filter[a][b] = scanner.nextDouble();
                        }
                    }
                    ConvLayer layer = new ConvLayer(filter);
                    layers.add(layer);
                    System.out.println("Conv layer");
                    for(int a=0; a < 3; ++a){
                        for(int b=0; b < 3; ++b){
                            System.out.print(filter[a][b]+" ");
                        }
                        System.out.println();
                    }
                }else if(line.startsWith("#FCL")){
                    System.out.println("FC layer");
                    scanner = new Scanner(line);
                    scanner.next();//remove #FCL
                    int nrNeurons = scanner.nextInt();
                    int nrWeights = scanner.nextInt();
                    System.out.println(String.format("Params: %d %d", nrNeurons,nrWeights));
                    double[] weights = new double[nrWeights];
                    List<Neuron> neurons = new ArrayList<>();
                    for(int nrNeuron=0; nrNeuron < nrNeurons; ++nrNeuron) {
                        //   System.out.print(String.format("Neuron %d: ", nrNeuron));
                        Neuron neuron;
                        weights = new double[nrWeights];
                        if (nrWeights != 0) {
                            line = bReader.readLine();
                            scanner = new Scanner(line);
                            for (int nrWeight = 0; nrWeight < nrWeights; ++nrWeight) {
                                weights[nrWeight] = scanner.nextDouble();
                                //  System.out.print(weights[nrWeight] + " ");
                            }
                            neuron = new Neuron(0, weights);
                        } else
                            neuron = new Neuron(0, null);
                        neurons.add(neuron);
                        //  System.out.println();
                    }
                    FCLayer layer = new FCLayer(neurons);
                    layers.add(layer);
                }
                else throw new IOException("Wrong file format!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return layers;
    }

    //communicating with window
    @Override
    public void update(Observable o, Object arg) {
        for(Observer observer : observers){
            observer.update(o,arg);
        }
    }
    @Override
    public synchronized void addObserver(Observer o) {
       observers.add(o);
    }
    @Override
    public synchronized void deleteObserver(Observer o) {
        observers.remove(o);
    }
}
