package com.romeon0;

import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by Romeon0 on 5/8/2018.
 */


public interface Layer {
    enum LAYER_TYPE{
        FCL,
        CONV,
        POOL,
        INPUT,
        NONE
    }

    LAYER_TYPE getType();
}
