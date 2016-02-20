package com.avanza.ctiavaya.ui;

import com.avanza.ctiavaya.controlllamada.AdministradorLlamada;
import com.avanza.ctiavaya.controlllamada.AvayaCallCenterExtension;
import com.avanza.ctiavaya.excepcion.ExcepcionExtensionConAgente;
import com.avanza.ctiavaya.excepcion.ExcepcionProveedorNoIniciado;

import com.avaya.jtapi.tsapi.TsapiInvalidArgumentException;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;

import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import java.util.ResourceBundle;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.telephony.InvalidArgumentException;
import javax.telephony.InvalidStateException;
import javax.telephony.JtapiPeerUnavailableException;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.ProviderUnavailableException;
import javax.telephony.ResourceUnavailableException;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * FXML Controller class
 *
 * @author michelo.vacca
 */
public class ControladorInicioSesion implements Initializable, EscenaControlada,
        EventHandler {

    private static final Logger LOGGER = LogManager.getLogger(
            ControladorInicioSesion.class);

    private static final String EVENTO_MINIMIZAR = "minimizar";
    private static final String EVENTO_CERRAR = "cerrar";
    private static final String EVENTO_INICIAR = "iniciar";

    private String nombreUsuario;
    private String contrasenia;
    private String extension;
    private String numeroAgente;
    private String contraseniaAgente;

    private Label lblCerrar;
    private Label lblMinimizar;
    @FXML
    private TextField txtUsuario;
    @FXML
    private PasswordField txtContrasenia;
    @FXML
    private TextField txtExtension;
    @FXML
    private TextField txtNumeroAgente;
    @FXML
    private Button btnIniciarSesion;
    @FXML
    private HBox hbToolBox;
    @FXML
    private ImageView imgLogo;

    private CTIAvaya ctiAvaya;
    private AdministradorLlamada administradorLlamada;
    private AvayaCallCenterExtension avayaCallCenterExtension;

    private void autenticar() {
        if (validarEntradas()) {
            if (null == CTIAvaya.channel) {
                CTIAvaya.comprobarInstancia();
            }
            if (autenticar(extension, numeroAgente, contraseniaAgente)) {
                Stage stage = (Stage) btnIniciarSesion.getScene().getWindow();
                stage.fireEvent(new WindowEvent(stage,
                        WindowEvent.WINDOW_CLOSE_REQUEST));
            }
        }
    }
    
    private boolean autenticar(String extension, String numeroAgente,
            String contraseniaAgente) {
        boolean autenticado = false;
        try {
            avayaCallCenterExtension = new AvayaCallCenterExtension(extension,
                    numeroAgente, null, 0, administradorLlamada);
            ctiAvaya.setAvayaCallCenterExtension(avayaCallCenterExtension);
            autenticado = true;
        } catch (ExcepcionProveedorNoIniciado | ExcepcionExtensionConAgente ex) {
            StringBuilder stringBuilder = new StringBuilder(Short.MAX_VALUE);
            stringBuilder.append(ex.getMessage());
            CTIAvaya.mostrarMensaje(stringBuilder.toString());
        } catch (ProviderUnavailableException ex) {
            StringBuilder stringBuilder = new StringBuilder(Short.MAX_VALUE);
            stringBuilder.append(
                    "La instancia del provedor CTI no está disponible.\n");
            stringBuilder.append(
                    "Revice que los parámetros de conexión CTI en el archivo de propiedades sean correctos.\n");
            stringBuilder.append("Mensaje de excepción:\n");
            stringBuilder.append(ex.getMessage());

            CTIAvaya.mostrarMensaje(stringBuilder.toString());
        } catch (InvalidArgumentException ex) {
            StringBuilder stringBuilder = new StringBuilder(Short.MAX_VALUE);

            if (ex instanceof TsapiInvalidArgumentException) {
                stringBuilder.append(
                        "Por favor revice la extensión ingresada e intente de nuevo.\n");
            } else {
                stringBuilder.append(
                        "Error al registrar la extensión. Por favor intente de nuevo.\n");
            }

            stringBuilder.append("Mensaje de excepción:\n");
            stringBuilder.append(ex.getMessage());

            CTIAvaya.mostrarMensaje(stringBuilder.toString());
        } catch (InvalidStateException ex) {
            StringBuilder stringBuilder = new StringBuilder(Short.MAX_VALUE);

            stringBuilder.append(
                    "La extensión está en un estado inválido. Por favor intente de nuevo.\n");
            stringBuilder.append("Mensaje de excepción:\n");
            stringBuilder.append(ex.getMessage());

            CTIAvaya.mostrarMensaje(stringBuilder.toString());
        } catch (ResourceUnavailableException ex) {
            StringBuilder stringBuilder = new StringBuilder(Short.MAX_VALUE);

            stringBuilder.append(
                    "La extensión no está disponible.\n Verifique la conficuración en el Comunication Manager e intente de nuevo.\n");
            stringBuilder.append("Mensaje de excepción:\n");
            stringBuilder.append(ex.getMessage());

            CTIAvaya.mostrarMensaje(stringBuilder.toString());
        } catch (MethodNotSupportedException ex) {
            StringBuilder stringBuilder = new StringBuilder(Short.MAX_VALUE);

            stringBuilder.append(
                    "Error al observar los eventos de telefonía.\n");
            stringBuilder.append("Mensaje de excepción:\n");
            stringBuilder.append(ex.getMessage());

            CTIAvaya.mostrarMensaje(stringBuilder.toString());
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
        }
        return autenticado;
    }

    private boolean validarEntradas() {
        boolean esValida = false;

        extension = txtExtension.getText();
        numeroAgente = txtNumeroAgente.getText();

        if (null == extension
                || "".equals(extension)) {
            CTIAvaya.mostrarMensaje("Por favor ingrese la extensión.");
            txtExtension.requestFocus();
        } else if (null == numeroAgente
                || "".equals(numeroAgente)) {
            CTIAvaya.mostrarMensaje("Por favor ingrese el número de agente.");
            txtNumeroAgente.requestFocus();
        } else {
            esValida = true;
        }

        return esValida;
    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        lblMinimizar = GlyphsDude.createIconLabel(FontAwesomeIcon.MINUS, "",
                "15", "12", ContentDisplay.CENTER);
        lblMinimizar.setOnMouseClicked(this);
        lblMinimizar.setUserData(EVENTO_MINIMIZAR);
        hbToolBox.getChildren().add(lblMinimizar);

        lblCerrar = GlyphsDude.createIconLabel(FontAwesomeIcon.CLOSE, "", "15",
                "12", ContentDisplay.CENTER);
        lblCerrar.setOnMouseClicked(this);
        lblCerrar.setUserData(EVENTO_CERRAR);
        hbToolBox.getChildren().add(lblCerrar);

        txtExtension.textProperty().addListener(new CampoNumerico());
        txtNumeroAgente.textProperty().addListener(new CampoNumerico());

        btnIniciarSesion.setUserData(EVENTO_INICIAR);
        btnIniciarSesion.setOnAction(this);

        BufferedImageTranscoder bufferedImageTranscoder = new BufferedImageTranscoder();

        try (InputStream archivo = getClass().getResourceAsStream(
                "/com/avanza/ctiavaya/recursos/logo_ffffff_632-714_186-862.svg")) {
            TranscoderInput transcoderInput = new TranscoderInput(archivo);
            try {
                bufferedImageTranscoder.transcode(transcoderInput, null);
                // Use WritableImage if you want to further modify the image (by using a PixelWriter)
                Image imagen = SwingFXUtils.toFXImage(bufferedImageTranscoder.
                        getBufferedImage(), null);
                imgLogo.setImage(imagen);
            } catch (TranscoderException ex) {
                LOGGER.error("Error al cargar el logo.", ex);
            }
        } catch (IOException ex) {
            LOGGER.error("Error al cargar el logo.", ex);
        }
    }

    @Override
    public void setEscenaPadre(ControladorEscena escenaPadre) {
    }

    @Override
    public void setCTIAvaya(CTIAvaya ctiAvaya) {
        this.ctiAvaya = ctiAvaya;
    }

    @Override
    public void handle(Event evento) {
        Node control = (Node) evento.getSource();
        String accion = control.getUserData().toString();

        switch (accion) {
            case EVENTO_MINIMIZAR:
                ctiAvaya.getStagePrincipal().setIconified(true);
                break;
            case EVENTO_CERRAR:
                CTIAvaya.cerrarAplicacion();
                break;
            case EVENTO_INICIAR:
                autenticar();
                break;
        }
    }
}
