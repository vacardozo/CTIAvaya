/**
 * ConnectionDisconnectedHandler is executed when an Disconnect event is
 * received from the AE Services.
 */
package com.avanza.ctiavaya.controlllamada;

import com.avanza.ctiavaya.controlllamada.IEstadoLlamada.EstadoConexion;
import com.avanza.ctiavaya.controlllamada.IEstadoLlamada.EstadoLlamada;

import javax.telephony.Call;
import javax.telephony.Connection;
import javax.telephony.callcontrol.CallControlConnectionEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ControladorConexionDesconectada implements IControladorEvento {

    private static final Logger LOGGER = LogManager.getLogger(
            ControladorConexionDesconectada.class);

    private final CallControlConnectionEvent evento;

    public ControladorConexionDesconectada(CallControlConnectionEvent evento) {
        this.evento = evento;
    }

    @Override
    public void ejecutar(AdministradorLlamada administradorLlamada) {
        String extension = evento.getConnection().getAddress().getName();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("ConnectionDisconnectedHandler for " + extension);
        }
        Call llamada = evento.getCall();
        long idLlamada = UtilidadLlamada.getIdLlamada(llamada);
        String extensionACD = UtilidadLlamada.getExtensionACDLlamada(llamada);
        String ucid = UtilidadLlamada.getUCID(llamada);
        /*
         * We receive CallCtlConnDisconnectedEv in following scenarios,
         * 1.Device drops from the call
         * 2.Call is transfered
         * 3.Conference is established
         */
        switch (evento.getCallControlCause()) {
            case CallControlConnectionEvent.CAUSE_NORMAL:
                controlarCallCtlConnDisconnectedEvent_CausaNormal(evento,
                        administradorLlamada, llamada, idLlamada, extensionACD,
                        ucid);
                break;
            case CallControlConnectionEvent.CAUSE_TRANSFER:
                controlarCallCtlConnDisconnectedEvent_CausaTransferencia(evento,
                        administradorLlamada, llamada, idLlamada, extensionACD,
                        ucid);
                break;
            case CallControlConnectionEvent.CAUSE_CONFERENCE:
                controladorCallCtlConnDisconnectedEvent_CaudaConferencia(evento,
                        administradorLlamada, llamada, idLlamada, extensionACD,
                        ucid);
                break;
            default:
                break;
        }

        administradorLlamada.getAgente().consultarEstadoAgente();
    }

    /**
     * Handle a "Normal" disconnect. That is, a disconnect which is not
     * associated with a Transfer or Conference.
     *
     * @param evento
     * @param administradorLlamada
     */
    private void controlarCallCtlConnDisconnectedEvent_CausaNormal(
            CallControlConnectionEvent evento,
            AdministradorLlamada administradorLlamada, Call llamada,
            long idLlamada, String extensionACD, String ucid) {
        LOGGER.info("ConnectionDisconnectedHandler_NormalCause");

        Connection[] conexiones = llamada.getConnections();

        // If the agent hung up, the dropped device is actually the
        // extension which the agent is logged into.
        String extensionColgada = administradorLlamada.getIdTerminal(llamada,
                evento.getConnection().getAddress().getName());

        administradorLlamada.quitarConexionesEnEspera(UtilidadLlamada.
                getIdLlamada(llamada));

        // If the Agent dropped the call
        if (extensionColgada.equals(administradorLlamada.getIdTerminalAgente())) {

            // The agent dropped out of conference, the call may still be up
            if (null != conexiones && UtilidadLlamada.MIN_CONN <= conexiones.length) {
                administradorLlamada.actualizarLlamada(idLlamada, extensionACD,
                        ucid, EstadoConexion.NULA, EstadoLlamada.ACTIVA, null,
                        null, extensionColgada);
            } else {
                administradorLlamada.actualizarLlamada(idLlamada, extensionACD,
                        ucid, EstadoConexion.NULA, EstadoLlamada.INVALIDA, null,
                        null, extensionColgada);
            }
        } else {
            // The Other Party dropped the call
            administradorLlamada.
                    actualizarLlamada(idLlamada, extensionACD, ucid,
                            EstadoConexion.TERCERO_CUELGA,
                            EstadoLlamada.INVALIDA, null, null, extensionColgada);
        }
    }

    /**
     * Handles the case where one party transfers the call to another.
     *
     * @param evento
     */
    private void controlarCallCtlConnDisconnectedEvent_CausaTransferencia(
            CallControlConnectionEvent evento,
            AdministradorLlamada administradorLlamada, Call llamada,
            long idLlamada, String extensionACD, String ucid) {
        LOGGER.info("ConnectionDisconnectedHandler_TransferCause");

        String droppedDeviceID = administradorLlamada.getIdTerminal(llamada,
                evento.getConnection().getAddress().getName());

        Connection[] conexiones = llamada.getConnections();

        // If there was only one connection in the call, the call has now
        // ended
        if (null == conexiones || UtilidadLlamada.MIN_CONN > conexiones.length) {

            administradorLlamada.
                    actualizarLlamada(idLlamada, extensionACD, ucid,
                            EstadoConexion.NULA, null, null, null,
                            droppedDeviceID);
            return;
        }

        // Retrieve the old call that was transferred
        long idLlamadaAnterior = UtilidadLlamada.getIdLlamadaAnterior(llamada);
        String extensionAnterior = UtilidadLlamada.getExtensionAnterior(llamada);

        /*
         * Determine the device to which the call is transferred.
         * If the call is answered before transfer is completed, it is the stored
         * calledDevice. In the case of blind transfer it is the device which
         * is alerting. 
         */
        String nuevaExtension = null;

        if (administradorLlamada.tieneLlamada(idLlamada)) {
            nuevaExtension = administradorLlamada.getDetalleLlamada(idLlamada).
                    getIdDestinoLlamada();
        } else if (Connection.ALERTING == conexiones[0].getState()) {
            nuevaExtension = conexiones[0].getAddress().getName();
        } else {
            nuevaExtension = conexiones[1].getAddress().getName();
        }

        // Inform the UI that the call is transferring.  This shows the ID of
        // the old call, the device transferring and the device being
        // transferred to.
        administradorLlamada.actualizarLlamada(idLlamadaAnterior, extensionACD,
                ucid, EstadoConexion.TRANSFERIDA, EstadoLlamada.INVALIDA, null,
                nuevaExtension, extensionAnterior);
    }

    /**
     * Handles call control connection disconnected events for conference cause.
     *
     * @param evento
     */
    private void controladorCallCtlConnDisconnectedEvent_CaudaConferencia(
            CallControlConnectionEvent evento,
            AdministradorLlamada administradorLlamada, Call llamada,
            long idLlamada, String extensionACD, String ucid) {
        LOGGER.info("ConnectionDisconnectedHandler_ConferenceCause");

        String droppedDeviceID
                = administradorLlamada.getIdTerminal(llamada,
                        evento.getConnection().getAddress().getName());

        if (droppedDeviceID.equals(administradorLlamada.getIdTerminalAgente())) {
            // The Agent disconnected from the call
            administradorLlamada.
                    actualizarLlamada(idLlamada, extensionACD, ucid,
                            EstadoConexion.NULA, EstadoLlamada.INVALIDA, null,
                            null, droppedDeviceID);
        } else {
            // Another party dropped from the call so it is still up
            administradorLlamada.
                    actualizarLlamada(idLlamada, extensionACD, ucid,
                            EstadoConexion.NULA, EstadoLlamada.ACTIVA, null,
                            null, droppedDeviceID);
        }
    }
}
