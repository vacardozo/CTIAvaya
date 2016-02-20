package com.avanza.ctiavaya.ui;

import com.avanza.ctiavaya.controlllamada.AdministradorLlamada;
import com.avanza.ctiavaya.controlllamada.AvayaCallCenterExtension;
import com.avanza.ctiavaya.controlllamada.DetalleAgente;

import java.net.URL;
import java.util.Observable;
import java.util.Observer;

import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javax.telephony.InvalidArgumentException;
import javax.telephony.InvalidStateException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author michelo.vacca
 */
public class CTIAvayaUI implements Initializable, Observer {

    @FXML
    private MenuItem miCerrarSesion;
    @FXML
    private MenuItem miSalir;
    @FXML
    private TextArea txtRegistro;

    private static final Logger LOGGER = LogManager.getLogger(
            CTIAvayaUI.class);

    private AvayaCallCenterExtension avayaCallCenterExtension;
    private AdministradorLlamada administradorLlamada;
    private CTIAvaya ctiAvaya;
    private DetalleAgente detalleAgente;

    private void iniciarObservacion() {
        avayaCallCenterExtension.addObserver(this);
        administradorLlamada.addObserver(this);
    }

    private void pararObservacion() {
        avayaCallCenterExtension.deleteObserver(this);
        administradorLlamada.deleteObserver(this);
    }

    private void cerrarSesion() {
        try {
            avayaCallCenterExtension.desconexionAgente();
        } catch (InvalidArgumentException |
                InvalidStateException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    private void mostrarFormularioInicioSesion() {
        CTIAvaya.crearAdministradorLlamada();
            ctiAvaya.getStagePrincipal().close();
            CTIAvaya.reiniciarCanal();
            ctiAvaya.crearFormularioInicioSesion();
    }
    
    private void cierreSesionAvayaCallCenterExtension() {
        avayaCallCenterExtension.pararMonitoreroAgente();
        pararObservacion();
        mostrarFormularioInicioSesion();
    }
    
    private void actualizarInfoLlamada(DetalleAgente detalleAgente) {
        
    }
    
    public void inicializar(AvayaCallCenterExtension avayaCallCenterExtension,
            AdministradorLlamada administradorLlamada, CTIAvaya ctiAvaya) {
        this.avayaCallCenterExtension = avayaCallCenterExtension;
        this.administradorLlamada = administradorLlamada;
        this.ctiAvaya = ctiAvaya;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        iniciarObservacion();
        miCerrarSesion.setOnAction((ActionEvent evento) -> {
            cerrarSesion();
            mostrarFormularioInicioSesion();
        });
        miSalir.setOnAction((ActionEvent evento) -> {
            cerrarSesion();
            CTIAvaya.cerrarAplicacion();
        });
    }

    @Override
    public void update(Observable observado, Object objeto) {

    }
}
