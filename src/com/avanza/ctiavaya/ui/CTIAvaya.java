package com.avanza.ctiavaya.ui;

import com.avanza.ctiavaya.controlllamada.AdministradorLlamada;
import com.avanza.ctiavaya.controlllamada.AvayaCallCenterExtension;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import javax.telephony.JtapiPeerUnavailableException;
import javax.telephony.ProviderUnavailableException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author michelo.vacca
 */
public class CTIAvaya extends Application {

    private static final Logger LOGGER = LogManager.getLogger(
            CTIAvaya.class);

    private Stage stagePrincipal;
    private AvayaCallCenterExtension avayaCallCenterExtension;

    /**
     * Opens a channel
     */
    public static FileChannel channel;

    private static AdministradorLlamada administradorLlamada;

    public Stage getStagePrincipal() {
        return stagePrincipal;
    }

    public void crearFormularioInicioSesion() {
        try {
            Stage frmInicioSesion = new Stage();
            frmInicioSesion.initStyle(StageStyle.UNDECORATED);
            FXMLLoader fXMLLoader = new FXMLLoader(getClass().getResource(
                    "/com/avanza/ctiavaya/ui/InicioSesion.fxml"));
            Parent root = fXMLLoader.load();
            EscenaControlada escenaControlada = ((EscenaControlada) fXMLLoader.
                    getController());
            escenaControlada.setCTIAvaya(this);
            Scene escenaInicioSesion = new Scene(root);

            frmInicioSesion.setOnCloseRequest((WindowEvent evento) -> {
                if (null == channel || !channel.isOpen()) {
                    cerrarAplicacion();
                } else {
                    try {
                        FXMLLoader fXMLLoader1 = new FXMLLoader(getClass().
                                getResource(
                                        "/com/avanza/ctiavaya/ui/CTIAvayaUI.fxml"));
                        Parent vistaCTIAvayaUI = fXMLLoader1.load();
                        CTIAvayaUI cTIAvayaUI = (CTIAvayaUI) fXMLLoader1.
                                getController();
                        cTIAvayaUI.inicializar(avayaCallCenterExtension,
                                administradorLlamada, this);
                        Scene scene = new Scene(vistaCTIAvayaUI);
                        stagePrincipal.setScene(scene);
                        stagePrincipal.show();
                    } catch (IOException ex) {
                        LOGGER.error(ex.getMessage());
                    }
                }
            });
            frmInicioSesion.setScene(escenaInicioSesion);
            frmInicioSesion.show();
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    public void setAvayaCallCenterExtension(
            AvayaCallCenterExtension avayaCallCenterExtension) {
        this.avayaCallCenterExtension = avayaCallCenterExtension;
    }
    
    public void pararMonitoreoAvayaCallCenterExtension() {
        avayaCallCenterExtension.pararMonitoreroAgente();
    }

    @Override
    public void start(Stage stage) throws Exception {
        stagePrincipal = stage;
        stagePrincipal.initStyle(StageStyle.UNDECORATED);
        crearFormularioInicioSesion();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (crearAdministradorLlamada()) {
            launch(args);
        } else {
            Platform.exit();
            System.exit(0);
        }
    }

    public static boolean crearAdministradorLlamada() {
        boolean conexionAbierta = false;
        StringBuilder mensaje = null;
        try {
            if (comprobarInstancia()) {
                administradorLlamada = new AdministradorLlamada();
                conexionAbierta = true;
            } else {
                administradorLlamada = null;
                mensaje = new StringBuilder(Short.MAX_VALUE);
                mensaje.append(
                        "Ya hay una instancia de la aplicación ejecutandose.");
            }
        } catch (ProviderUnavailableException ex) {
            mensaje = new StringBuilder(Short.MAX_VALUE);
            String mensajeError = ex.getMessage();
            if (null != mensajeError) {
                switch (mensajeError) {
                    case "initialization failed":
                        mensaje.append(
                                "Error de autenticación del usuario CTI.\nExcepción:\n");
                        mensaje.append(mensajeError);
                        break;
                    case "server not found":
                        mensaje.append(
                                "Ocurrió un error al abrir el canal de comunicación con el servidor.");
                        mensaje.append(
                                "\nRevice la configuración del proveedor CTI.\nExcepción:\n");
                        mensaje.append(mensajeError);
                        break;
                    default:
                        mensaje.append(
                                "Ocurrió un error al abrir el canal de comunicación con el servidor.");
                        mensaje.append("\nExcepción:\n");
                        mensaje.append(mensajeError);
                        break;
                }
            }
            LOGGER.error(mensaje.toString());
            Platform.exit();
            System.exit(0);
        } catch (JtapiPeerUnavailableException ex) {
            StringBuilder stringBuilder = new StringBuilder(Short.MAX_VALUE);
            String mensajeError = ex.getMessage();
            stringBuilder.append("El objeto Peer no está disponible.");
            stringBuilder.append("\nExcepción:\n");
            stringBuilder.append(mensajeError);
        } catch (IOException ex) {
            StringBuilder stringBuilder = new StringBuilder(Short.MAX_VALUE);
            stringBuilder.append(
                    "Un error IO ocurrió al intentar abrir un canal con el servidor.");
            stringBuilder.append("\nExcepción:\n");
            stringBuilder.append(ex.getMessage());
        } catch (Exception ex) {
            mensaje = new StringBuilder(Short.MAX_VALUE);
            mensaje.append(
                    "El archivo de configuración de conexión no está accesible.");
            mensaje.append(ex.getMessage());
        }

        if (null != mensaje) {
            mostrarMensaje(mensaje.toString());
        }

        return conexionAbierta;
    }

    /**
     * Only one instance of this application may be running at any time. The
     * application uses a lock file to make sure that there is not already
     * another instance running.
     *
     * @return true if no other instance is running already
     */
    public static boolean comprobarInstancia() {
        boolean esInstanciaUnica = false;
        RandomAccessFile randomFile = null;
        try {
            String strCurrDir = new File(".").getAbsolutePath();
            randomFile = new RandomAccessFile(strCurrDir + "/ctiavaya.bat",
                    "rw");
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage());
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        if (randomFile != null) {
            channel = randomFile.getChannel();
            try {
                if (channel.tryLock() != null) {
                    esInstanciaUnica = true;
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
        } else {
            esInstanciaUnica = true;
        }

        return esInstanciaUnica;
    }

    public static void reiniciarCanal() {
        try {
            // Disconnect from the JTAPI Provider
            administradorLlamada.apagar();

            // Release the lock
            if (channel != null) {
                channel.close();
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    public static void cerrarAplicacion() {
        reiniciarCanal();
        System.exit(0);
    }

    public static void mostrarMensaje(String mensaje) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Atentción");
        alert.setHeaderText("A ocurrido un error.");
        alert.setContentText(mensaje);

        alert.showAndWait();
    }
}
