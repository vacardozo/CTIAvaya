package com.avanza.ctiavaya.comunicacion;

import com.avanza.ctiavaya.controlllamada.AdministradorLlamada;
import com.avanza.ctiavaya.controlllamada.Mensaje;
import com.avanza.ctiavaya.excepcion.ExcepcionProveedorNoIniciado;
import com.avaya.jtapi.tsapi.TsapiInvalidArgumentException;

import com.avaya.jtapi.tsapi.TsapiPlatformException;
import com.avaya.jtapi.tsapi.adapters.ProviderListenerAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.telephony.Address;
import javax.telephony.InvalidArgumentException;
import javax.telephony.JtapiPeer;
import javax.telephony.JtapiPeerFactory;
import javax.telephony.JtapiPeerUnavailableException;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.Provider;
import javax.telephony.ProviderEvent;
import javax.telephony.ProviderUnavailableException;
import javax.telephony.ResourceUnavailableException;
import javax.telephony.Terminal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author michelo.vacca
 */
public class InterfazJTAPI extends ProviderListenerAdapter {

    private static final String ARCHIVO_PROPIEDADES = "ctiavaya.properties";

    // log4j logger
    private static final Logger LOGGER = LogManager.getLogger(
            InterfazJTAPI.class);

    // Instancia de la API del proveedor de telefonía
    private JtapiPeer apiProveedor;

    // AdministradorLlamada procesa los eventos entrantes y pasa mensajes al
    // usuario.
    private final AdministradorLlamada administradorLlamada;

    // Instancia del proveedor de telefonía.
    private Provider proveedor;

    // Indica si la instancia del proveedor está inicializada.
    private boolean gotProvider;

    // Los valores de las siguientes variables están en el archivo
    // ctiavaya.properties
    private String servicioaes;
    private String usuarioaes;
    private String contraseniaaes;

    // 
    // Store for Terminal Connection Proxies.  There should be one per monitored
    // terminal.  This application monitors only one terminal.
//    private Map<String, CallControlTerminalConnectionProxy> terminalConnectionProxies
//            = new HashMap<>();
    private ProxyConexionTerminalControlLlamada conexionTerminalControlLlamada;

    /**
     * Crea una instancia y abre una conexión con el AE Services.
     *
     * @param administradorLlamada Administrador de llamadas
     * @throws IOException
     * @throws JtapiPeerUnavailableException
     * @throws ProviderUnavailableException
     */
    public InterfazJTAPI(AdministradorLlamada administradorLlamada)
            throws ProviderUnavailableException, JtapiPeerUnavailableException,
            IOException {
        this.administradorLlamada = administradorLlamada;
        abrirCanal();
    }

    /**
     * Este método: 1. Llama a leerArhivoPropiedades() para leer el archivo de
     * propiedades. 2. Recupera el objeto apiProveedor. 3. Obtiene el proveedor
     * usando el servicioaes, usuarioaes y contraseniaaes establecidos en el
     * archivo de propiedades. 4. Resgistra un listener al Provider y así saber
     * cuando el proveedor está listo. ready.
     *
     * @return
     * @throws JtapiPeerUnavailableException
     * @throws ProviderUnavailableException
     * @throws IOException
     */
    private boolean abrirCanal() throws JtapiPeerUnavailableException,
            ProviderUnavailableException, IOException {

        leerArvhivoPropiedades();
        apiProveedor = JtapiPeerFactory.getJtapiPeer(null);

        try {
            proveedor = apiProveedor.getProvider(
                    servicioaes + ";loginID=" + usuarioaes
                    + ";passwd=" + contraseniaaes);
            proveedor.addProviderListener(this);

        } catch (TsapiPlatformException e) {
            throw new ProviderUnavailableException(e.getMessage());
        } catch (ProviderUnavailableException | ResourceUnavailableException |
                MethodNotSupportedException e) {
            throw new ProviderUnavailableException(e.getMessage());
        }

        return (proveedor != null);
    }

    /**
     * Esta función lee el archivo de propiedades y recupera This function reads
     * the properties file and retrieves servicioaes, usuarioaes y
     * contraseniaaes para configuración del proveedor.
     */
    private void leerArvhivoPropiedades() throws IOException {
        Properties propiedades = new Properties();
        String rutaAbsoluta = new File(ARCHIVO_PROPIEDADES).getAbsolutePath();
        propiedades.load(new FileInputStream(rutaAbsoluta));

        servicioaes = propiedades.getProperty("servicioaes").trim();
        usuarioaes = propiedades.getProperty("usuarioaes").trim();
        contraseniaaes = propiedades.getProperty("contraseniaaes").trim();
    }

    /**
     * Este método detiene el monitoreo de la extensión registrada.
     *
     * @param extension
     * @throws com.avanza.ctiavaya.excepcion.ExcepcionProveedorNoIniciado
     */
    public void pararMonitoreo(String extension) throws
            ExcepcionProveedorNoIniciado {
        try {
            if (gotProvider && null != conexionTerminalControlLlamada) {
                proveedor.getTerminal(extension).removeCallListener(
                        conexionTerminalControlLlamada);
            } else {
                throw new ExcepcionProveedorNoIniciado();
            }
        } catch (InvalidArgumentException e) {
            Mensaje mensaje = new Mensaje("Error in Removing the CallListener: ",
                    Mensaje.TipoMensaje.ERROR);
            administradorLlamada.notificar(mensaje);
        }
    }

    /**
     * Este método inicia el monitoreo de la extensión registrada, y envía a la
     * aplicación los eventos de llamada recibidos para esta extensión.
     *
     * @param extension
     * @throws com.avanza.ctiavaya.excepcion.ExcepcionProveedorNoIniciado
     * @throws javax.telephony.ResourceUnavailableException
     * @throws javax.telephony.MethodNotSupportedException
     * @throws com.avaya.jtapi.tsapi.TsapiInvalidArgumentException
     * @throws javax.telephony.InvalidArgumentException
     */
    public void iniciarMonitoreo(String extension) throws
            ExcepcionProveedorNoIniciado, ResourceUnavailableException,
            MethodNotSupportedException, TsapiInvalidArgumentException,
            InvalidArgumentException {
        if (gotProvider) {
            if (conexionTerminalControlLlamada == null) {
                conexionTerminalControlLlamada = new ProxyConexionTerminalControlLlamada(
                        administradorLlamada);
                proveedor.getTerminal(extension).addCallListener(
                        conexionTerminalControlLlamada);
            }
        } else {
            throw new ExcepcionProveedorNoIniciado();
        }
    }

    /**
     * Elimina el Provider y limpia todo lo necesario.
     *
     * @throws com.avanza.ctiavaya.excepcion.ExcepcionProveedorNoIniciado
     */
    public void limpiarCanal() throws ExcepcionProveedorNoIniciado {
        if (gotProvider) {
            proveedor.shutdown();
            proveedor = null;
            gotProvider = false;
            apiProveedor = null;
            conexionTerminalControlLlamada = null;
        } else {
            throw new ExcepcionProveedorNoIniciado();
        }
    }

    /**
     * Retorna un objeto <code>Address</code>, dando el número de extensión.
     *
     * @param numeroExtension número de extensión
     * @return Address El objeto correspondiente a la extensión
     * @throws InvalidArgumentException
     * @throws com.avanza.ctiavaya.excepcion.ExcepcionProveedorNoIniciado
     */
    public Address getExtension(String numeroExtension) throws
            InvalidArgumentException, ExcepcionProveedorNoIniciado {
        Address extension = null;
        if (gotProvider) {
            extension = proveedor.getAddress(numeroExtension);
        } else {
            throw new ExcepcionProveedorNoIniciado();
        }

        return extension;
    }

    /**
     * Retorna un objeto <code>Terminal</code>, dando el número de extensión.
     *
     * @param numeroExtension número de extensión
     * @return Terminal El objeto correspondiente a la extensión
     * @throws InvalidArgumentException
     * @throws com.avanza.ctiavaya.excepcion.ExcepcionProveedorNoIniciado
     */
    public Terminal getTerminal(String numeroExtension)
            throws InvalidArgumentException, ExcepcionProveedorNoIniciado {
        Terminal terminal = null;
        if (gotProvider) {
            terminal = proveedor.getTerminal(numeroExtension);
        } else {
            throw new ExcepcionProveedorNoIniciado();
        }

        return terminal;
    }

    /**
     * Generate an exception to indicate that a request was received before the
     * Provider was initialized.
     *
     * @throws Exception
     */
//    private void throwProviderNotInitializedEx() throws Exception {
//        Exception providerNotInitializedEx = new Exception(
//                " JTAPI  provider not initialized. ");
//
//        throw providerNotInitializedEx;
//    }
    // Los siguientes métodos extienden de <code>ProviderListenerAdapter</code>
    // que implementa la interfaz <code>ProviderListener</code>.
    /**
     * Este evento se lanza cuando el proveedor está en servicio y ya puede ser
     * usado.
     *
     * @param providerEvent
     */
    @Override
    public void providerInService(ProviderEvent providerEvent) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("providerInService");
        }
        gotProvider = true;
    }

    /**
     * Este evento se lanza cuando el proveedor ya no está en servicio e intenta
     * reconectar
     *
     * @param providerEvent
     */
    @Override
    public void providerOutOfService(ProviderEvent providerEvent) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("providerOutOfService");
        }
        gotProvider = false;
        Mensaje mensaje = new Mensaje("Lost connection with the JTAPI Provider",
                Mensaje.TipoMensaje.INFO);
        administradorLlamada.notificar(mensaje);
    }

    @Override
    public void providerEventTransmissionEnded(ProviderEvent event) {
        LOGGER.info("providerEventTransmissionEnded");
    }

    @Override
    public void providerShutdown(ProviderEvent event) {
        LOGGER.info("providerShutdown");
    }
}
