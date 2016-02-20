/**
 * ControladorAgenteContesta is executed when the Agent answers a call from the
 * AgentViewer UI.  It answers the call if the Agent's phone is ringing.
 */
package com.avanza.ctiavaya.controlllamada;

import javax.telephony.Call;
import javax.telephony.Connection;
import javax.telephony.InvalidStateException;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.PrivilegeViolationException;
import javax.telephony.ResourceUnavailableException;
import javax.telephony.TerminalConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author michelo.vacca
 */
public class ControladorAgenteContesta implements IControladorEvento {

    private static final Logger LOGGER = LogManager.getLogger(
            ControladorAgenteContesta.class);

    // CallId of the call
    public long idLlamada;

    public ControladorAgenteContesta(long idLlamada) {
        this.idLlamada = idLlamada;
    }

    /**
     * Handle the case where the user presses the Answer button on the UI.
     *
     * @param administradorLlamada
     */
    @Override
    public void ejecutar(AdministradorLlamada administradorLlamada) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("AgentAnswerHandler");
        }

        // Check that the call is in a state where it can be answered and
        // get the Terminal Connection of the Agent. 
        TerminalConnection conexionTerminal = getTermConnSiContestable(idLlamada,
                administradorLlamada);

        // If the agent's terminal is in a valid state, answer it.
        try {
            if (conexionTerminal != null) {
                conexionTerminal.answer();
            }

        } catch (PrivilegeViolationException | ResourceUnavailableException |
                MethodNotSupportedException | InvalidStateException e) {
            Mensaje mensaje = new Mensaje("Exception Occured. Cannot answer the call: " + e.getMessage(), Mensaje.TipoMensaje.ERROR);
            administradorLlamada.notificar(mensaje);
        }
    }

    /**
     * Checks if the agent terminal connection is in RINGING state
     *
     * @return TerminalConnection
     */
    private TerminalConnection getTermConnSiContestable(long idLlamada,
            AdministradorLlamada administradorLlamada) {
        TerminalConnection conexionTerminal = null;
        Call llamada = administradorLlamada.getDetalleLlamada(idLlamada).
                getLlamada();

        // Make sure the call is active
        if (null != llamada) {

            // Find the Terminal Connection for the Agent and make sure it it Ringing
            Connection[] conexiones = llamada.getConnections();
            if (null != conexiones) {

                // Loop through each connection in the call and look for the Agent's
                // terminal
                boolean encontrado = false;
                for (int i = 0; i < conexiones.length && !encontrado; i++) {
                    if (!conexiones[i].getAddress().getName().equals(
                            administradorLlamada.getIdTerminalAgente())) {

                        // Each connection has one (or more) terminals.  Check them for
                        // the Agent's terminal
                        TerminalConnection conexionesTerminal[] = conexiones[i].
                                getTerminalConnections();
                        for (TerminalConnection conexionTerminal1 : conexionesTerminal) {

                            // If the terminal is the Agent and it is ringing return
                            if (conexionTerminal1.getTerminal().getName().
                                    equals(
                                            administradorLlamada.
                                            getIdTerminalAgente())) {
                                if (TerminalConnection.RINGING == conexionTerminal1.getState()) {
                                    conexionTerminal = conexionTerminal1;
                                    encontrado = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        // Either the Agent was not found or, more likely, it was not in the
        // RINGING state.
        return conexionTerminal;
    }
}
