/**
 * TerminalConnectionTalkingHandler is executed when an
 * TerminalConnectionTalking event is received from the AE Services.
 */
package com.avanza.ctiavaya.controlllamada;

import com.avanza.ctiavaya.controlllamada.IEstadoLlamada.EstadoConexion;
import com.avanza.ctiavaya.controlllamada.IEstadoLlamada.EstadoLlamada;
import javax.telephony.Call;
import javax.telephony.TerminalConnection;
import javax.telephony.callcontrol.CallControlTerminalConnectionEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ControladorConexionTerminalHablando implements IControladorEvento {

    private static final Logger LOGGER = LogManager.getLogger(
            ControladorConexionTerminalHablando.class);

    public CallControlTerminalConnectionEvent evento;

    public ControladorConexionTerminalHablando(
            CallControlTerminalConnectionEvent evento) {
        this.evento = evento;
    }

    @Override
    public void ejecutar(AdministradorLlamada administradorLlamada) {

        TerminalConnection termConn = evento.getTerminalConnection();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("TerminalConnectionTalkingHandler for "
                    + termConn.getTerminal().getName());
        }

        Call llamada = evento.getCall();
        // The TerminalConnectionTalking is received at the same time as the
        // ConnectionEstablished - this case is not of interest to us.  It is also 
        // received when a terminal goes off Hold.  In this case, update the UI.
        long idLLamada = UtilidadLlamada.getIdLlamada(llamada);
        String extensionACD = UtilidadLlamada.getExtensionACDLlamada(llamada);
        String ucid = UtilidadLlamada.getUCID(llamada);
        if (administradorLlamada.tieneConexionEnEspera(idLLamada)) {
            String extensionRetomada = termConn.getTerminal().getName();

            administradorLlamada.
                    actualizarLlamada(idLLamada, extensionACD, ucid,
                            EstadoConexion.EN_ESPERA, EstadoLlamada.ACTIVA,
                            extensionRetomada, null, null);

            administradorLlamada.quitarConexionesEnEspera(idLLamada);
        }
    }
}
