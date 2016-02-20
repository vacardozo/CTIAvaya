/**
 * ConnectionAlertingHandler is executed when an Alerting event is received from
 * the AE Services.
 */
package com.avanza.ctiavaya.controlllamada;

import com.avanza.ctiavaya.controlllamada.IEstadoLlamada.EstadoConexion;
import com.avanza.ctiavaya.controlllamada.IEstadoLlamada.EstadoLlamada;

import javax.telephony.Call;
import javax.telephony.callcontrol.CallControlConnectionEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ControladorConexionTimbrando implements IControladorEvento {

    private static final Logger LOGGER = LogManager.getLogger(
            ControladorConexionTimbrando.class);

    public CallControlConnectionEvent evento;

    public ControladorConexionTimbrando(CallControlConnectionEvent evento) {
        this.evento = evento;
    }

    @Override
    public void ejecutar(AdministradorLlamada administradorLlamada) {

        String extension = evento.getConnection().getAddress().getName();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("ConnectionAlertingHandler for " + extension);
        }

        EstadoConexion estadoConexion = EstadoConexion.DESCONOCIDA;
        Call llamada = evento.getCall();
        long idLlamada = UtilidadLlamada.getIdLlamada(llamada);
        String extensionACD = UtilidadLlamada.getExtensionACDLlamada(llamada);
        String ucid = UtilidadLlamada.getUCID(llamada);

        String tercero = evento.getCalledAddress().getName();
        String destino = null;
        String origen = null;

        switch (evento.getCallControlCause()) {

            case CallControlConnectionEvent.CAUSE_CONFERENCE:
                LOGGER.info("ConnectionAlertingHandler_CAUSE_CONFERENCE");
                // Blind Conference
                // The alerting device is the called party.
                destino = evento.getConnection().getAddress().getName();
                origen = administradorLlamada.getIdTerminal(llamada, evento.
                        getCallingAddress().getName());
                estadoConexion = EstadoConexion.CONFERENCIA_TIMBRANDO;
                break;

            case CallControlConnectionEvent.CAUSE_TRANSFER:
                LOGGER.info("ConnectionAlertingHandler_CAUSE_TRANSFER");
                // Blind Transfer 
                // Take the device that is not transferring as the calling device
                // and take the alerting device as the called party.
                origen = administradorLlamada.getIdTerminal(llamada, evento.
                        getCalledAddress().getName());
                destino = evento.getConnection().getAddress().getName();
                tercero = extension;

                // The new status of the Agent's Connection depends on who initiated 
                // the call
                if (destino.equals(administradorLlamada.getIdTerminalAgente())) {
                    estadoConexion = EstadoConexion.TIMBRANDO;
                } else {
                    estadoConexion = EstadoConexion.INICIADA;
                }
                break;
            default:
                LOGGER.info("ConnectionAlertingHandler_DEFAULT");
                // Normal two party call
                destino
                        = administradorLlamada.getIdTerminal(llamada,
                                evento.getCalledAddress().getName());
                origen = administradorLlamada.getIdTerminal(llamada, evento.
                        getCallingAddress().getName());

                // The new status of the Agent's Connection depends on who initiated 
                // the call
                if (destino.equals(administradorLlamada.getIdTerminalAgente())) {
                    estadoConexion = EstadoConexion.TIMBRANDO;
                } else {
                    estadoConexion = EstadoConexion.INICIADA;
                }

                // Add the new call to the List of Calls
                administradorLlamada.agregarNuevaLlamada(llamada, destino);

                break;
        }

        // Update the UI with the new call status
        administradorLlamada.actualizarLlamada(idLlamada, extensionACD, ucid,
                estadoConexion, EstadoLlamada.ACTIVA, destino, origen, tercero);
    }
}
