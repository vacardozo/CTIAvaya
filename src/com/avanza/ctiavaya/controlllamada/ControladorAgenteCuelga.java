/**
 * AgentDisconnectHandler is executed when the Agent drops a call from the
 * AgentViewer UI.  If the Agent's terminal is in a valid state, it sends a
 * Disconnect request to the Provider.
 */
package com.avanza.ctiavaya.controlllamada;

import javax.telephony.Call;
import javax.telephony.Connection;
import javax.telephony.InvalidStateException;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.PrivilegeViolationException;
import javax.telephony.ResourceUnavailableException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ControladorAgenteCuelga implements IControladorEvento {

    private static final Logger LOGGER = LogManager.getLogger(
            ControladorAgenteCuelga.class);

    // CallId of the call
    public long idLlamada;

    public ControladorAgenteCuelga(long idLlamada) {
        this.idLlamada = idLlamada;
    }

    /**
     *
     * @param administradorLlamada
     */
    @Override
    public void ejecutar(AdministradorLlamada administradorLlamada) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("AgentDisconnectHandler");
        }

        // Get the Connection of the Agent
        Connection conexion = getConnParaDesconectar(idLlamada,
                administradorLlamada);

        // If the agent's terminal was in a call, disconnect
        try {
            if (null != conexion) {
                conexion.disconnect();
            }
        } catch (PrivilegeViolationException | ResourceUnavailableException |
                MethodNotSupportedException | InvalidStateException e) {
            String error = "Exception Occured. Cannot drop the call: " + e.getMessage();
            if (administradorLlamada.getNumeroLlamadas() != 0) {
                error =                        "Cannot drop a held call, retrieve the call first.";
            }
            LOGGER.error(error, e);
            Mensaje mensaje = new Mensaje(error, Mensaje.TipoMensaje.ERROR);
            administradorLlamada.notificar(mensaje);
        }
    }

    /**
     * Checks if the agent connection is in CONNECTED, ALERTING, INPROGRESS or
     * FAILED state.
     *
     * @return connection
     */
    private Connection getConnParaDesconectar(long idLlamada,
            AdministradorLlamada administradorLlamada) {

        Connection conexion = null;

        Call llamada = administradorLlamada.getDetalleLlamada(idLlamada).
                getLlamada();

        // Make sure the call is active
        if (null != llamada) {

            // Find the Agent's Connection and make sure it is active
            Connection[] conexiones = llamada.getConnections();
            if (null != conexiones) {
                for (Connection conexion1 : conexiones) {
                    if (conexion1.getAddress().getName().equals(
                            administradorLlamada.getIdTerminalAgente())
                            && (Connection.CONNECTED == conexion1.getState()
                            || Connection.ALERTING == conexion1.getState()
                            || Connection.INPROGRESS == conexion1.getState()
                            || Connection.FAILED == conexion1.getState())) {
                        conexion = conexion1;
                    }
                }
            }
        }
        return conexion;
    }
}
