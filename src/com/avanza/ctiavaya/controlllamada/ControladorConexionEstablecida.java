/**
 * ConnectionEstablishedHandler is executed when an Established event is
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

import com.avaya.jtapi.tsapi.LucentEventCause;

public class ControladorConexionEstablecida implements IControladorEvento {

    private static final Logger LOGGER = LogManager.getLogger(
            ControladorConexionEstablecida.class);

    private final CallControlConnectionEvent evento;

    public ControladorConexionEstablecida(CallControlConnectionEvent evento) {
        this.evento = evento;
    }

    @Override
    public void ejecutar(AdministradorLlamada administradorLlamada) {
        String extension = evento.getConnection().getAddress().getName();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("ConnectionEstablishedHandler for " + extension);
        }

        Call llamada = evento.getCall();
        long idLlamada = UtilidadLlamada.getIdLlamada(llamada);
        String extensionACD = UtilidadLlamada.getExtensionACDLlamada(llamada);
        String ucid = UtilidadLlamada.getUCID(llamada);
        Connection[] conexiones = llamada.getConnections();

        switch (evento.getCallControlCause()) {
            case CallControlConnectionEvent.CAUSE_NORMAL:
                controlarEstadoNormal(idLlamada, extensionACD, ucid, llamada,
                        administradorLlamada, conexiones,
                        extension);
                break;
            case CallControlConnectionEvent.CAUSE_TRANSFER:
                controlarEstadoTransferencia(idLlamada, extensionACD, ucid,
                        llamada, administradorLlamada, conexiones, extension);
                break;
            case CallControlConnectionEvent.CAUSE_CONFERENCE:
                controlarEstadoConferencia(idLlamada, extensionACD, ucid,
                        llamada,
                        administradorLlamada, conexiones);
                break;
        }

        // The Agent state may have changed, have this change propagated
        // to the UI
        administradorLlamada.getAgente().consultarEstadoAgente();
    }

    public void controlarEstadoNormal(long idLlamada, String extensionACD,
            String ucid, Call llamada, AdministradorLlamada administradorLlamada,
            Connection[] conexiones, String extension) {
        LOGGER.info("ConnectionEstablishedHandler_NormalState");
        String origen = administradorLlamada.getIdTerminal(llamada,
                evento.getCallingAddress().getName());
        String destino = administradorLlamada.getIdTerminal(llamada,
                evento.getCalledAddress().getName());

        // The call could be a Transfer
        if (LucentEventCause.EC_TRANSFER == UtilidadLlamada.
                getCausaEventoLlamada(llamada)) {
            controlarEstadoTransferencia(idLlamada, extensionACD, ucid, llamada,
                    administradorLlamada, conexiones, extension);
        } // This evento could be for a Conference call
        else if (conexiones != null && conexiones.length >= UtilidadLlamada.MIN_CONFERENCE_CONN) {
            controlarEstadoConferencia(idLlamada, extensionACD, ucid, llamada,
                    administradorLlamada, conexiones);
        } else if (destino.equals(extension)) {
            // If this evento is due to the called party answering,
            // inform the UI
            administradorLlamada.
                    actualizarLlamada(idLlamada, extensionACD, ucid,
                            EstadoConexion.CONECTADA, EstadoLlamada.ACTIVA,
                            destino, origen, extension);
        }
        administradorLlamada.agregarNuevaLlamada(llamada, destino);
    }

    /**
     * When a call is transferred, the called & calling devices are with respect
     * to the transfer consult call, rather than the final call. These must be
     * reorganised to generate an accurate output to the UI.
     *
     * @param idLLamada
     * @param extensionACD
     * @param llamada
     * @param ucid
     * @param administradorLlamada
     * @param conexiones
     * @param extension
     */
    public void controlarEstadoTransferencia(long idLLamada, String extensionACD,
            String ucid, Call llamada, AdministradorLlamada administradorLlamada,
            Connection[] conexiones, String extension) {
        LOGGER.info("ConnectionEstablishedHandler_TransferState");
        String origen = null;
        String destino = null;

        if (conexiones != null && conexiones.length > 0) {

            // Get the devices taking part in the call
            origen = conexiones[0].getAddress().getName();
            if (conexiones.length > 1) {
                destino = conexiones[1].getAddress().getName();
            }

            // We can receive Establish events before the call is answered.  If
            // any of the connections are not yet Connected, send an Alerting
            // update to the UI.
            for (Connection conexion : conexiones) {
                if (Connection.CONNECTED != conexion.getState()) {

                    // Figure out which connection is being called and update
                    // The UI
                    if (conexion.getAddress().getName().equals(destino)) {
                        administradorLlamada.actualizarLlamada(idLLamada,
                                extensionACD, ucid, EstadoConexion.TIMBRANDO,
                                EstadoLlamada.ACTIVA, destino, origen, destino);
                    } else {
                        administradorLlamada.actualizarLlamada(idLLamada,
                                extensionACD, ucid, EstadoConexion.TIMBRANDO,
                                EstadoLlamada.ACTIVA, origen, destino, origen);
                    }
                    return;
                }
            }
        }
        administradorLlamada.agregarNuevaLlamada(llamada, destino);

        administradorLlamada.actualizarLlamada(idLLamada, extensionACD, ucid,
                EstadoConexion.CONECTADA, EstadoLlamada.ACTIVA, destino, origen,
                destino);
    }

    /**
     * When a call is Conferenced, update the UI. If necessary, add a New call
     * to the List of Calls. Also remove the old call from the List of Held
     * Calls.
     *
     * @param idLLamada
     * @param extensionACD
     * @param ucid
     * @param llamada
     * @param administradorLlamada
     * @param conexiones
     */
    public void controlarEstadoConferencia(long idLLamada, String extensionACD,
            String ucid, Call llamada, AdministradorLlamada administradorLlamada,
            Connection[] conexiones) {
        LOGGER.info("ConnectionEstablishedHandler_ConferenceState");
        if (conexiones != null && conexiones.length >= UtilidadLlamada.MIN_CONFERENCE_CONN) {

            // Do not do anything until the last extension is active on the call
            for (Connection conexion : conexiones) {
                if (conexion.getState() != Connection.CONNECTED) {
                    return;
                }
            }
            // Get device ID's participating in conference call
            String party1 = conexiones[0].getAddress().getName();
            String party2 = conexiones[1].getAddress().getName();
            String party3 = conexiones[2].getAddress().getName();

            if (!administradorLlamada.tieneLlamada(idLLamada)) {
                // The Agent is moving from one call to a new one.  Remove the 
                // old call from the Map and add the new one
                administradorLlamada.quitarDetalleLlamada(UtilidadLlamada.
                        getIdLlamadaAnterior(llamada));
                String calledDeviceID
                        = administradorLlamada.getIdTerminal(llamada,
                                evento.getCalledAddress().getName());
                administradorLlamada.
                        agregarNuevaLlamada(llamada, calledDeviceID);

            }
            administradorLlamada.
                    actualizarLlamada(idLLamada, extensionACD, ucid,
                            EstadoConexion.EN_CONFERENCIA, EstadoLlamada.ACTIVA,
                            party1, party2, party3);
        }

        // The original call was probably on hold.  Remove it from the hold list.
        administradorLlamada.quitarConexionesEnEspera(UtilidadLlamada.
                getIdLlamadaAnterior(llamada));
    }
}
