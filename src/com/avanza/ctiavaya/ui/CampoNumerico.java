package com.avanza.ctiavaya.ui;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 *
 * @author michelo.vacca
 */
public class CampoNumerico implements ChangeListener<String> {

    @Override
    public void changed(ObservableValue<? extends String> observable,
            String valorAnterior, String nuevoValor) {
        if ("[0-9]*".matches(nuevoValor)) {
            ((StringProperty) observable).setValue(nuevoValor);
        }
    }

}
