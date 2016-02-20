/**
 * This class represents an Agent and contains functionality typically performed
 * by an Agent such as log-in/log-out,changing the Agent State etc. This class
 * periodically requests the current Agent state from the Provider. If the state
 * has changed since the previous request, it sends Agent State change
 * notifications to its registered observers.
 */
package com.avanza.ctiavaya.controlllamada;

import com.avanza.ctiavaya.excepcion.ExcepcionExtensionConAgente;
import com.avanza.ctiavaya.excepcion.ExcepcionProveedorNoIniciado;

import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

import javax.telephony.InvalidArgumentException;
import javax.telephony.InvalidStateException;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.ResourceUnavailableException;
import javax.telephony.callcenter.Agent;
import javax.telephony.Address;
import javax.telephony.callcenter.AgentTerminal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.avaya.jtapi.tsapi.LucentAgent;
import com.avaya.jtapi.tsapi.TsapiInvalidArgumentException;
import com.avaya.jtapi.tsapi.TsapiInvalidStateException;
import com.avaya.jtapi.tsapi.TsapiUnableToSendException;
import java.io.IOException;
import javax.telephony.JtapiPeerUnavailableException;
import javax.telephony.ProviderUnavailableException;

public class AvayaCallCenterExtension extends Observable {

    private static final Logger LOGGER = LogManager.getLogger(
            AvayaCallCenterExtension.class);

    private final DetalleAgente detalleAgente;
    // Los valores de las siguientes variables son proporcionados por el usuario.
//    private final String numeroExtension;
//    private final String idAgente;
//    private final String contraseniaAgente;
    private final int modo;

    // Si se cierra la sesión de agente sin usar la aplicación, fue un cierre de
    // sesión manual.
    //private boolean esCierreSesionManual;
    /**
     * Run method of AgentStateTimerInterval keeps checking the current state of
     * the agent.
     */
    private AgentStateTimerInterval timeInterval;

    // To be used to schedule a timer task
    private final Timer timer;

    // Agent data
    private AgentTerminal terminalAgente;
//    private LucentAddress lucentAddr;
//    private LucentTerminal lucentTerm;
//    private LucentV7Agent lucentAgent;
    // Call manager
    private final AdministradorLlamada administradorLlamada;

    /**
     * Class Constructor
     *
     * @param numeroExtension
     * @param idAgente
     * @param contraseniaAgente
     * @param modo 0 = Auto mode, 1 = Manual mode
     * @param administradorLlamada
     * @throws com.avanza.ctiavaya.excepcion.ExcepcionProveedorNoIniciado
     * @throws javax.telephony.ResourceUnavailableException
     * @throws javax.telephony.MethodNotSupportedException
     * @throws javax.telephony.InvalidArgumentException
     * @throws com.avanza.ctiavaya.excepcion.ExcepcionExtensionConAgente
     * @throws javax.telephony.InvalidStateException
     * @throws com.avaya.jtapi.tsapi.TsapiInvalidArgumentException
     * @throws javax.telephony.JtapiPeerUnavailableException
     * @throws java.io.IOException
     */
    public AvayaCallCenterExtension(String numeroExtension, String idAgente,
            String contraseniaAgente, int modo,
            AdministradorLlamada administradorLlamada) throws
            ExcepcionProveedorNoIniciado, ResourceUnavailableException,
            MethodNotSupportedException, InvalidArgumentException,
            ExcepcionExtensionConAgente, InvalidStateException,
            TsapiInvalidArgumentException, ProviderUnavailableException,
            JtapiPeerUnavailableException, IOException {

        detalleAgente = new DetalleAgente(idAgente, contraseniaAgente);
        detalleAgente.setDesconexionManual(true);
        this.administradorLlamada = administradorLlamada;
//        this.numeroExtension = numeroExtension;
//        this.idAgente = idAgente;
//        this.contraseniaAgente = contraseniaAgente;
        this.modo = modo;
//        esCierreSesionManual = true;
        timeInterval = new AgentStateTimerInterval();
        timer = new Timer();

        // Login the agent and start monitoring it
        conexionAgente(numeroExtension);
    }

    /**
     * Starts monitor on the agent terminal
     *
     * @throws ResourceUnavailableException
     * @throws MethodNotSupportedException
     * @throws InvalidArgumentException
     */
    private void iniciarMonitoreoAgente(String numeroExtension) throws
            ExcepcionProveedorNoIniciado, ResourceUnavailableException,
            MethodNotSupportedException, TsapiInvalidArgumentException,
            InvalidArgumentException, ProviderUnavailableException,
            JtapiPeerUnavailableException, IOException {
        administradorLlamada.getInterfazJTAPI().
                iniciarMonitoreo(numeroExtension);
    }

    /**
     * Login function for the agent
     *
     * @throws Exception
     */
    private void conexionAgente(String numeroExtension) throws
            ExcepcionProveedorNoIniciado, ResourceUnavailableException,
            MethodNotSupportedException, TsapiInvalidArgumentException,
            ExcepcionExtensionConAgente, InvalidStateException,
            InvalidArgumentException, ProviderUnavailableException,
            JtapiPeerUnavailableException, IOException {

        // Start monitoring the agent's terminal
        iniciarMonitoreoAgente(numeroExtension);

        // Get address/terminal from the AE Services
//        lucentAddr = (LucentAddress) administradorLlamada.getInterfazJTAPI().
//                getExtension(
//                        detalleAgente.getNumeroExtension());
//        System.out.println(lucentAddr.getDirectoryName());
        terminalAgente = (AgentTerminal) administradorLlamada.
                getInterfazJTAPI().getTerminal(numeroExtension);
        Address extension = administradorLlamada.getInterfazJTAPI().
                getExtension(numeroExtension);
//        lucentTerm = (LucentTerminal) administradorLlamada.getInterfazJTAPI().
//                getTerminal(numeroExtension);

        // Check if any other agent is already logged in at this extension
//        Agent[] agents = lucentTerm.getAgents();
        Agent[] agents = terminalAgente.getAgents();
        if (agents != null && !agents[agents.length - 1].getAgentID().equals(
                detalleAgente.getIdAgente())) {

            throw new ExcepcionExtensionConAgente(agents[agents.length - 1].
                    getAgentID(), numeroExtension);
        }

        // Log the agent on at the terminal
        Agent agente = terminalAgente.addAgent(extension, null, Agent.NOT_READY,
                detalleAgente.getIdAgente(), detalleAgente.
                getContraseniaAgente());
        detalleAgente.setAgente(agente);
//        lucentAgent = (LucentV7Agent) lucentTerm.addAgent(lucentAddr, null,
//                Agent.LOG_IN, modo, idAgente, contraseniaAgente);

        // Start polling the Agent's state
        consultarEstadoAgente();
    }

    /**
     * Logout the agent.
     *
     * @throws InvalidArgumentException
     * @throws InvalidStateException
     */
    public void desconexionAgente() throws InvalidArgumentException,
            InvalidStateException {

        // Agent used the Logoff button so was not manual
        detalleAgente.setDesconexionManual(false);
//        esCierreSesionManual = false;

        // Log the Agent off
        terminalAgente.removeAgent(detalleAgente.getAgente());
//        lucentTerm.removeAgent(lucentAgent);

        // Poll the new Agent state
        consultarEstadoAgente();
    }

    /**
     * Send an Agent State update request from the UI to the AE Services
     *
     * @param nuevoEstado
     * @param estaPendiente - true means the state will not change while the
     * agent is in a call
     * @throws javax.telephony.InvalidArgumentException
     * @throws javax.telephony.InvalidStateException
     */
    public void setEstadoAgente(int nuevoEstado, boolean estaPendiente) throws
            InvalidArgumentException, InvalidStateException {
        try {
            detalleAgente.getAgente().setState(nuevoEstado);
//            lucentAgent.setState(newAgentState, modo, 0, isPending);
        } catch (TsapiInvalidArgumentException e) {
            String error = "This change of state is not allowed";
            LOGGER.error(error, e);
            Mensaje mensaje = new Mensaje(error, Mensaje.TipoMensaje.ERROR);
            notificar(mensaje);
        } catch (TsapiInvalidStateException e) {
            String error = "Agent is not in a valid state to change its state";
            LOGGER.error(error, e);
            Mensaje mensaje = new Mensaje(error, Mensaje.TipoMensaje.ERROR);
            notificar(mensaje);
        }

        // Poll the new Agent state so it can be sent to the UI
        consultarEstadoAgente();
    }

    /**
     * Stop Polling the Agent's state and monitoring the Agent's terminal. This
     * method is called when the application discovers that the agent has logged
     * off.
     */
    public void pararMonitoreroAgente() {
        // Stop listening for Connection and ConnectionTerminal events
        try {
            administradorLlamada.getInterfazJTAPI().pararMonitoreo(
                    terminalAgente.getName());
        } catch (ProviderUnavailableException | JtapiPeerUnavailableException |
                IOException | ExcepcionProveedorNoIniciado e) {
            LOGGER.error("AvayaCallCenterUserAgent.agentStopMonitor() " + e.
                    getMessage());
        }

        // Cancel the Agent State polling timer
        if (timeInterval != null) {
            timeInterval.cancel();
        }
        if (timer != null) {
            timer.cancel();
        }
    }

    /**
     * Notify Subscribers about the agent state change
     *
     * @param estadoAgente
     */
//    private void updateAgent(EstadoAgente estadoAgente) {
//        setChanged();
//        super.notifyObservers(estadoAgente);
//    }
    /**
     * Queries State of the Agent by resetting the timer task Query Now and
     * every 5 seconds afterwards.
     *
     */
    public void consultarEstadoAgente() {
//        try {
        // Cancel the previous timer schedule
        if (timeInterval != null) {
            timeInterval.cancel();
        }

        timeInterval = null;
        timeInterval = new AgentStateTimerInterval();

        // Schedule the timer to query agent state.  This will query Now 
        // and every 5 seconds afterwards.
        timer.schedule(timeInterval, 0, 5 * 1000);

//        } catch (Exception e) {
//            LOGGER.error(
//                    "Exception in AvayaCallCenterUserAgent.queryAgentState(): "
//                    + e.getMessage());
//        }
    }

    public DetalleAgente getDetalleAgente() {
        return detalleAgente;
    }

    /**
     * @return the idAgente
     */
//    public String getIdAgente() {
//        return idAgente;
//    }
    /**
     * @return Lucent Agent
     */
//    public LucentV7Agent getLucentAgent() {
//        return lucentAgent;
//    }
    public AgentTerminal getTerminalAgente() {
        return terminalAgente;
    }

    /**
     * Send a notification to the UI
     *
     * @param msg
     */
    private void notificar(Object mensaje) {
        setChanged();
        super.notifyObservers(mensaje);
    }

    /**
     * The run() method of AgentStateTimerInterval class checks the state of the
     * agent. When agent's state changes, the state is displayed on the text
     * area of the UI.
     */
    private class AgentStateTimerInterval extends TimerTask {

        private int estadoPrevio = LucentAgent.UNKNOWN;

        /**
         * Class Constructor
         */
        public AgentStateTimerInterval() {
        }

        @Override
        public void run() {
            try {
                final int idEstadoActual = detalleAgente.getAgente().getState();
//            final int currentState = lucentAgent.getState();
//            try {
//                if ((currentState == Agent.LOG_IN)
//                        || (currentState != previousState)) {
//                    displayMessage("Estado: " + currentState);
//                }
                // If agent is not in valid state or if there is no change
                // in agent's state then do not update the UI
                final Mensaje mensaje = new Mensaje();

                if (idEstadoActual != estadoPrevio) {
                    if (Agent.LOG_OUT == idEstadoActual) {
                        // If the agent logged off from the extension, inform the UI
                        if (detalleAgente.isDesconexionManual()) {
                            notificar("Agent Manually Logged out");
                            detalleAgente.setDesconexionManual(false);
//                        esCierreSesionManual = false;
                        }
                    }

                    mensaje.setObjeto(detalleAgente);
                    notificar(mensaje);
                    estadoPrevio = idEstadoActual;
                }

                // Update the UI with the new state
//                updateAgent(EstadoAgente.getEstadoAgente(idEstadoActual));
//            } catch (Exception e) {
//                Mensaje mensaje = new Mensaje("Exception in AvayaCallCenterUserAgent.TimerInterval(): ",
//                        Mensaje.TipoMensaje.INFO);
//                notificar(mensaje);
//            }
            } catch (TsapiUnableToSendException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
    }
}
