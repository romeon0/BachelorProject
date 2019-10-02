package com.romeon0;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Created by Romeon0 on 4/19/2018.
 */
public class TestingTableDataModel {
    SimpleIntegerProperty number;
    SimpleStringProperty actualAnswer;
    SimpleStringProperty desiredAnswer;

    TestingTableDataModel(int number, String actualAnswer, String desiredAnswer){
        this.number = new SimpleIntegerProperty(number);
        this.actualAnswer = new SimpleStringProperty(actualAnswer);
        this.desiredAnswer = new SimpleStringProperty(desiredAnswer);
    }

    public int getNumber() {
        return number.get();
    }

    public SimpleIntegerProperty numberProperty() {
        return number;
    }

    public void setNumber(int number) {
        this.number.set(number);
    }

    public String getActualAnswer() {
        return actualAnswer.get();
    }

    public SimpleStringProperty actualAnswerProperty() {
        return actualAnswer;
    }

    public void setActualAnswer(String actualAnswer) {
        this.actualAnswer.set(actualAnswer);
    }

    public String getDesiredAnswer() {
        return desiredAnswer.get();
    }

    public SimpleStringProperty desiredAnswerProperty() {
        return desiredAnswer;
    }

    public void setDesiredAnswer(String desiredAnswer) {
        this.desiredAnswer.set(desiredAnswer);
    }
}
