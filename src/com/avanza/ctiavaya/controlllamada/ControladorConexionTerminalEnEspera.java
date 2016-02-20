/**
 * TerminalConnectionHeldHandler is executed when an Held event is received from
 * the AE Services.
 */
package com.avanza.ctiavaya.controlllamada;

import com.avanza.ctiavaya.controlllamada.IEstadoLlamada.EstadoConexion;
import com.avanza.ctiavaya.controlllamada.IEstadoLlamada.EstadoLlamada;

import javax.telephony.Call;
import javax.telephony.TerminalConnection;
import javax.telephony.callcontrol.CallControlTerminalConnectionEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ControladorConexionTerminalEnEspera implements IControladorEvento {

    private static final Logger LOGGER = LogManager.getLogger(ControladorConexionTerminalEnEspera.class);

    public CallControlTerminalConnectionEvent evento;

    public ControladorConexionTerminalEnEspera(
            CallControlTerminalConnectionEvent evento) {
        this.evento = evento;
    }

    @Override
    public void ejecutar(AdministradorLlamada administradorLlamada) {

        TerminalConnection terminalConnection = evento.getTerminalConnection();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("TerminalConnectionHeldHandler for "
                    + terminalConnection.getTerminal().getName());
        }

        Call llamada = evento.getCall();

        long idLLamada = UtilidadLlamada.getIdLlamada(llamada);
        String extensionACD = UtilidadLlamada.getExtensionACDLlamada(llamada);
        String ucid = UtilidadLlamada.getUCID(llamada);
        String extensionEspera = terminalConnection.getTerminal().getName();

        // Add this call to the list of calls which have a party on Hold
        administradorLlamada.agregarConexionEnEspera(llamada);

        // Update the UI
        administradorLlamada.actualizarLlamada(idLLamada, extensionACD, ucid, EstadoConexion.EN_ESPERA, EstadoLlamada.ACTIVA,
                extensionEspera, null, null);
    }
}
