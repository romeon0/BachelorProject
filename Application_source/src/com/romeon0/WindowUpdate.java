package com.romeon0;

/**
 * Created by Romeon0 on 3/19/2018.
 */
enum WindowUpdateType{
    TESTING_TABLEVALUE,
    TESTING_NRCORRECTANSWERS,
    TESTING_LOADDATA,
    TESTING_STARTED, TESTING_STOPPED,
    TRAINING_STARTED, TRAINING_STOPPED,
    TRAINING_EPOCHERROR, 
    TRAINING_NREPOCH, 
    TRAINING_LOADDATA
}

public class WindowUpdate {
    private WindowUpdateType type;
    private Object value;
    public WindowUpdate(WindowUpdateType type, Object value){
        this.type = type;
        this.value = value;
    }

    public WindowUpdateType getType(){return type;}
    public Object getValue(){return value;}
}
