/**
 * ConnectionInitiatedHandler is executed when an Initiated event is received
 * from the AE Services.
 */
package com.avanza.ctiavaya.controlllamada;

import javax.telephony.callcontrol.CallControlConnectionEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ControladorConexionIniciada implements IControladorEvento {

    private static final Logger LOGGER = LogManager.getLogger(ControladorConexionIniciada.class);

    public CallControlConnectionEvent event = null;

    public ControladorConexionIniciada(CallControlConnectionEvent event) {
        this.event = event;
    }

    @Override
    public void ejecutar(AdministradorLlamada callmanager) {

        String connName = event.getConnection().getAddress().getName();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("ConnectionInitiatedHandler for " + connName);
        }

        // The Agent state has probably changed, have this change propagated
        // to the UI
        callmanager.getAgente().consultarEstadoAgente();
    }
}
