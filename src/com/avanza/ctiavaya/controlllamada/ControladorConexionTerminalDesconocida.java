/**
 * TerminalConnectionUnknownHandler is executed when an
 * TerminalConnectionUnknown event is received from the AE Services.
 */
package com.avanza.ctiavaya.controlllamada;

import com.avanza.ctiavaya.controlllamada.IEstadoLlamada.EstadoConexion;
import com.avanza.ctiavaya.controlllamada.IEstadoLlamada.EstadoLlamada;
import javax.telephony.Call;
import javax.telephony.Connection;
import javax.telephony.TerminalConnection;
import javax.telephony.callcontrol.CallControlTerminalConnectionEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ControladorConexionTerminalDesconocida implements
        IControladorEvento {

    private static final Logger LOGGER = LogManager.getLogger(
            ControladorConexionTerminalDesconocida.class);

    public CallControlTerminalConnectionEvent evento;

    public ControladorConexionTerminalDesconocida(
            CallControlTerminalConnectionEvent evento) {
        this.evento = evento;
    }

    @Override
    public void ejecutar(AdministradorLlamada administradorLlamada) {

        TerminalConnection terminalConnection = evento.getTerminalConnection();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("TerminalConnectionUnknownHandler for "
                    + terminalConnection.getTerminal().getName());
        }

        Call llamada = evento.getCall();
        long idLlamada = UtilidadLlamada.getIdLlamada(llamada);
        String extensionACD = UtilidadLlamada.getExtensionACDLlamada(llamada);
        String ucid = UtilidadLlamada.getUCID(llamada);
        String extension = terminalConnection.getTerminal().getName();

        // The CallControlTerminalConnectionEvent may be received during the
        // setup of a conference call.  In this case, use it to update the UI.
        switch (evento.getCallControlCause()) {
            case CallControlTerminalConnectionEvent.CAUSE_CONFERENCE:
                // Inform the UI that there is an attempt to add a new party to
                // the call.
                Connection[] conn = llamada.getConnections();

                if (conn != null && conn.length >= UtilidadLlamada.MIN_CONFERENCE_CONN) {

                    administradorLlamada.actualizarLlamada(idLlamada,
                            extensionACD, ucid, EstadoConexion.AGREGANDO_TERCERO,
                            EstadoLlamada.ACTIVA, null, null, extension);
                }
                break;
        }
    }
}
