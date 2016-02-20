package com.avanza.ctiavaya.controlllamada;

import com.avanza.ctiavaya.comunicacion.InterfazJTAPI;
import com.avanza.ctiavaya.controlllamada.IEstadoLlamada.EstadoConexion;
import com.avanza.ctiavaya.controlllamada.IEstadoLlamada.EstadoLlamada;
import com.avanza.ctiavaya.excepcion.ExcepcionProveedorNoIniciado;

import com.avaya.jtapi.tsapi.LucentCallInfo;

import java.io.IOException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Queue;

import javax.telephony.Call;
import javax.telephony.JtapiPeerUnavailableException;
import javax.telephony.ProviderUnavailableException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author michelo.vacca
 */
public class AdministradorLlamada extends Observable {

    // Agent details
    private AvayaCallCenterExtension agente;

    // Keep a list of all active calls
    private final HashMap<Long, DetalleLlamada> listaLlamadas;

    // listHeldDevices is used to keep track of which calls are on hold.  This 
    // is needed to be able to interpret TerminalConnectionTalking events.
    private final HashMap<Long, Call> listaConexionesEnEspera;

    // jtapiInterface is used to communicate with the JTAPI API.
    private InterfazJTAPI interfazJTAPI;

    // log4j logger
    private static final Logger LOGGER = LogManager.getLogger(
            AdministradorLlamada.class);

    // The application internal event queue.  This object acts also as a
    // synchronisation point between the JTAPI event delivery thread and the
    // application worker thread.
    private final Queue<TransaccionEvento> colaTransacciones;

    /**
     * Class Constructor
     *
     * @throws JtapiPeerUnavailableException
     * @throws IOException
     * @throws ProviderUnavailableException
     */
    public AdministradorLlamada() throws ProviderUnavailableException,
            JtapiPeerUnavailableException, IOException {
        super();
        interfazJTAPI = new InterfazJTAPI(this);
        listaLlamadas = new HashMap<>();
        listaConexionesEnEspera = new HashMap<>();
        // Create the event queue and the worker thread which will process
        // each event in turn
        colaTransacciones = new LinkedList<>();
        new HiloTransacciones(this, colaTransacciones).start();
    }

    /*
     * (non-Javadoc) Saves events in the transaction queue one by one for further
     * processing on the application worker thread (see: constructor).
     * 
     * @see javax.telephony.CallObserver#callChangedEvent(javax.telephony.
     * events.CallEv[])
     */
    public void encolarTransaccion(TransaccionEvento evento) {
        if (null != evento) {

            // JTAPI event delivery thread should not be used by the application to
            // process an event. One good reason is to avoid potential deadlock or
            // data corruption when the application calls a JTAPI method while
            // running in the JTAPI event thread. The other reason is possible 
            // performance degradation since the JTAPI implementation owns 
            // the thread.
            synchronized (colaTransacciones) {
                colaTransacciones.add(evento);
                // JTAPI issues a notification before it releases the lock of the
                // transaction queue.  This notification wakes up the application 
                // worker thread which takes the transaction and processes it.
                colaTransacciones.notifyAll();
            }
        }
    }

    /**
     * Notify the UI of an error
     *
     * @param mensaje
     */
//    public void mostrarMensaje(Mensaje mensaje) {
//        notificar(mensaje);
//    }
    /**
     * Sets the reference to the agente object & values of lucent agente ID &
     * device ID.
     *
     * @param agente
     */
    public void setAgente(AvayaCallCenterExtension agente) {
        this.agente = agente;
    }

    /**
     * The Agent has answered the call using the Answer button on the UI. Queue
     * this request for processing.
     *
     * @param idLlamada
     */
    public void agenteContesta(long idLlamada) {
        TransaccionEvento transaccionEvento = new TransaccionEvento(idLlamada);
        transaccionEvento.agregar(new ControladorAgenteContesta(idLlamada));
        encolarTransaccion(transaccionEvento);
    }

    /**
     * The Agent has disconnected from the call using the Drop button on the UI.
     * Queue this request for processing.
     *
     * @param idLlamada
     */
    public void agenteCuelga(long idLlamada) {
        TransaccionEvento transaccionEvento = new TransaccionEvento(idLlamada);
        transaccionEvento.agregar(new ControladorAgenteCuelga(idLlamada));
        encolarTransaccion(transaccionEvento);
    }

    /**
     * Add new call object to Map
     *
     * @param llamada
     * @param idDestino
     */
    public void agregarNuevaLlamada(Call llamada, String idDestino) {
        long idLlamada = UtilidadLlamada.getIdLlamada(llamada);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("New Call added.  ID = " + idLlamada);
        }

        if (!tieneLlamada(idLlamada)) {
            DetalleLlamada detalleLlamada = new DetalleLlamada(llamada,
                    idDestino);
            listaLlamadas.put(idLlamada, detalleLlamada);
        }
    }

    /**
     * Notify subscribers of a change in the call status.
     *
     * @param mensaje
     */
    public void notificar(Mensaje mensaje) {
        setChanged();
        super.notifyObservers(mensaje);
    }

    /**
     * Notify subscribers (the UI) that the status of the call has changed.
     * Also, update or remove the Call Detail record if necessary.
     *
     * @param idLlamada
     * @param extensionACD
     * @param ucid
     * @param estadoConexion
     * @param estadoLlamada
     * @param idDestino
     * @param idOrigen
     * @param idTercero
     */
    public void actualizarLlamada(long idLlamada, String extensionACD,
            String ucid, EstadoConexion estadoConexion,
            EstadoLlamada estadoLlamada, String idDestino, String idOrigen,
            String idTercero) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("updateCall() - EstadoLlamada = " + estadoLlamada);
        }

        Mensaje mensaje = new Mensaje();
        mensaje.setTipo(Mensaje.TipoMensaje.INFO);
        mensaje.setObjeto(new ActualizacionControlConexion(estadoConexion,
                estadoLlamada, idLlamada, extensionACD, ucid, idDestino,
                idOrigen, idTercero));
        notificar(mensaje);

        // May need to remove the call block if the call is finished. Also,
        // may need to update the Conference state of the call
        if (tieneLlamada(idLlamada)) {
            DetalleLlamada detalleLlamada = getDetalleLlamada(idLlamada);

            if (EstadoConexion.NULA == estadoConexion
                    || EstadoConexion.TRANSFERIDA == estadoConexion) {
                quitarDetalleLlamada(idLlamada);
            } else if (EstadoConexion.EN_CONFERENCIA == estadoConexion) {
                detalleLlamada.setLlamadaEnConferencia(true);
            } else if (EstadoConexion.TERCERO_CUELGA == estadoConexion) {
                detalleLlamada.setLlamadaEnConferencia(false);
            }
        }
    }

    /**
     * Clears the list of held devices and list of calls. Called when the UI is
     * shut down.
     */
    private void limpiarListaLlamadas() {
        if (!listaConexionesEnEspera.isEmpty()) {
            listaConexionesEnEspera.clear();
        }
        if (!listaLlamadas.isEmpty()) {
            listaLlamadas.clear();
        }
        agente = null;
    }

    /**
     * Shuts down the connection to the provider
     */
    public void apagar() {
        limpiarListaLlamadas();

        try {
            interfazJTAPI.limpiarCanal();
            interfazJTAPI = null;
        } catch (ExcepcionProveedorNoIniciado e) {
            LOGGER.error("Error al cerrar la conexión del proveedor.", e);
        }
    }

    public void agregarConexionEnEspera(Call llamada) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Add Held Device.  ID = " + UtilidadLlamada.
                    getIdLlamada(llamada));
        }

        listaConexionesEnEspera.put(UtilidadLlamada.getIdLlamada(llamada),
                llamada);
    }

    public void quitarConexionesEnEspera(long idLlamada) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Remove Held Device.  ID = " + idLlamada);
        }

        if (tieneConexionEnEspera(idLlamada)) {
            listaConexionesEnEspera.remove(idLlamada);
        }
    }

    public boolean tieneConexionEnEspera(long idLlamada) {
        return listaConexionesEnEspera.containsKey(idLlamada);
    }

    public boolean tieneLlamada(long idLlamada) {
        return listaLlamadas.containsKey(idLlamada);
    }

    public DetalleLlamada getDetalleLlamada(long idLlamada) {
        return listaLlamadas.get(idLlamada);
    }

    /**
     * Remove a call from the map
     *
     * @param idLlamada
     */
    public void quitarDetalleLlamada(long idLlamada) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Remove Call.  ID = " + idLlamada);
        }

        if (listaLlamadas.containsKey(idLlamada)) {
            listaLlamadas.remove(idLlamada);
        }
    }

    public String getIdTerminalAgente() {
        return agente.getTerminalAgente().getName();
    }

    public String getIdAgente() {
        return agente.getDetalleAgente().getIdAgente();
    }

    public int getNumeroLlamadas() {
        return listaLlamadas.size();
    }

    public AvayaCallCenterExtension getAgente() {
        return agente;
    }

    /**
     * An Agent can be reached using a Hunt Group ID, Agent ID or Device ID.
     * This method will always return the Device ID.
     *
     * @param llamada
     * @param idTerminal - This may actually be the Hunt Group ID or Agent ID
     * @return
     */
    public String getIdTerminal(Call llamada, String idTerminal) {
        LOGGER.info("LucentCallInfo: " + (llamada instanceof LucentCallInfo));
        String huntGroup = UtilidadLlamada.getExtensionACDLlamada(llamada);
        LOGGER.info(
                "skill: " + huntGroup + " :: deviceId: " + idTerminal + " :: agentId: " + getIdAgente());
        if ((huntGroup != null && idTerminal.equals(huntGroup))
                || idTerminal.equals(getIdAgente())) {
            return getIdTerminalAgente();
        }

        return idTerminal;
    }

    /**
     *
     * @return The interface to the JTAPI.
     * @throws JtapiPeerUnavailableException
     * @throws IOException
     * @throws ProviderUnavailableException
     */
    public InterfazJTAPI getInterfazJTAPI() throws ProviderUnavailableException,
            JtapiPeerUnavailableException, IOException {
        if (null == interfazJTAPI) {
            interfazJTAPI = new InterfazJTAPI(this);
        }
        return this.interfazJTAPI;
    }
}
