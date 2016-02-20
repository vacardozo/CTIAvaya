/**
 * This class listens for CallControlTerminalConnectionEvents from the AE
 * Services. Call Control eventos for a single Meta evento are sent between a
 * MetaProgressStarted evento and a MetaProgressEnded evento. These Call Control
 * eventos form a single Transaction. The application should not begin to
 * process one Call Control evento until all eventos of the Transaction have
 * been received.
 *
 * Each evento type has an associated Handler class.
 * CallControlTerminalConnectionProxy creates the appropriate Handler class for
 * each received Event and adds it to an TransaccionEvento. Once the Transaction
 * is complete, CallControlTerminalConnectionProxy places the TransaccionEvento
 * on the CallManager queue for processing.
 *
 * Only the eventos that are used by the application have handler classes. Other
 * eventos (eg. terminalConnectionRinging) are received and logged here to show
 * when they appear. Methods such as
 * terminalConnectionRinging(CallControlTerminalConnectionEvent evento) need not
 * be implemented.
 */
package com.avanza.ctiavaya.comunicacion;

import com.avanza.ctiavaya.controlllamada.AdministradorLlamada;
import com.avanza.ctiavaya.controlllamada.UtilidadLlamada;
import com.avanza.ctiavaya.controlllamada.ControladorConexionTimbrando;
import com.avanza.ctiavaya.controlllamada.ControladorConexionDesconectada;
import com.avanza.ctiavaya.controlllamada.ControladorConexionEstablecida;
import com.avanza.ctiavaya.controlllamada.ControladorConexionIniciada;
import com.avanza.ctiavaya.controlllamada.TransaccionEvento;
import com.avanza.ctiavaya.controlllamada.ControladorConexionTerminalEnEspera;
import com.avanza.ctiavaya.controlllamada.ControladorConexionTerminalHablando;
import com.avanza.ctiavaya.controlllamada.ControladorConexionTerminalDesconocida;

import com.avaya.jtapi.tsapi.adapters.CallControlTerminalConnectionListenerAdapter;
import com.avaya.jtapi.tsapi.impl.events.call.SingleCallMetaEventImpl;

import javax.telephony.AddressEvent;
import javax.telephony.Call;
import javax.telephony.CallEvent;
import javax.telephony.ConnectionEvent;
import javax.telephony.callcontrol.CallControlConnectionEvent;
import javax.telephony.callcontrol.CallControlTerminalConnectionEvent;
import javax.telephony.MetaEvent;
import javax.telephony.MultiCallMetaEvent;
import javax.telephony.TerminalConnectionEvent;
import javax.telephony.TerminalEvent;
import javax.telephony.callcenter.ACDAddressEvent;
import javax.telephony.callcenter.ACDAddressListener;
import javax.telephony.callcenter.AgentTerminalEvent;
import javax.telephony.callcenter.AgentTerminalListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProxyConexionTerminalControlLlamada extends CallControlTerminalConnectionListenerAdapter
        implements AgentTerminalListener {

    private static final Logger LOGGER
            = LogManager.getLogger(ProxyConexionTerminalControlLlamada.class);

    private final AdministradorLlamada administradorLlamada;

    private TransaccionEvento transaccionEvento;

    /**
     *
     * @param administradorLlamada
     */
    public ProxyConexionTerminalControlLlamada(
            AdministradorLlamada administradorLlamada) {
        this.administradorLlamada = administradorLlamada;
    }

    @Override
    public void singleCallMetaProgressStarted(MetaEvent evento) {
        if (transaccionEvento != null) {
            LOGGER.error("Error: singleCallMetaProgressStarted received "
                    + "with open transaction");
        }

        SingleCallMetaEventImpl metaEvent = (SingleCallMetaEventImpl) evento;
        Call llamada = metaEvent.getCall();
        long idLlamada = UtilidadLlamada.getIdLlamada(llamada);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("singleCallMetaProgressStarted: callId = " + idLlamada);
        }
        transaccionEvento = new TransaccionEvento(idLlamada);
    }

    @Override
    public void singleCallMetaProgressEnded(MetaEvent evento) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("singleCallMetaProgressEnded");
        }

        if (transaccionEvento != null) {
            administradorLlamada.encolarTransaccion(transaccionEvento);
        } else {
            LOGGER.error("Error: singleCallMetaProgressEnded received "
                    + "without open transaction");
        }
        transaccionEvento = null;
    }

    @Override
    public void multiCallMetaTransferStarted(MetaEvent evento) {
        if (transaccionEvento != null) {
            LOGGER.error("Error: multiCallMetaTransferStarted received "
                    + "with open transaction");
        }

        MultiCallMetaEvent metaEvent = (MultiCallMetaEvent) evento;
        Call llamada = metaEvent.getNewCall();
        long idLlamada = UtilidadLlamada.getIdLlamada(llamada);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("multiCallMetaTransferStarted: callId = " + idLlamada);
        }
        transaccionEvento = new TransaccionEvento(idLlamada);
    }

    @Override
    public void multiCallMetaTransferEnded(MetaEvent evento) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("multiCallMetaTransferEnded");
        }

        if (transaccionEvento != null) {
            administradorLlamada.encolarTransaccion(transaccionEvento);
        } else {
            LOGGER.error("Error: multiCallMetaTransferEnded received "
                    + "without open transaction");
        }
        transaccionEvento = null;
    }

    @Override
    public void multiCallMetaMergeStarted(MetaEvent evento) {
        if (transaccionEvento != null) {
            LOGGER.error("Error: multiCallMetaMergeStarted received "
                    + "with open transaction");
        }

        MultiCallMetaEvent metaEvent = (MultiCallMetaEvent) evento;
        Call llamada = metaEvent.getNewCall();
        long idLlamada = UtilidadLlamada.getIdLlamada(llamada);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("multiCallMetaMergeStarted: callId = " + idLlamada);
        }
        transaccionEvento = new TransaccionEvento(idLlamada);
    }

    @Override
    public void multiCallMetaMergeEnded(MetaEvent evento) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("multiCallMetaMergeEnded");
        }

        if (transaccionEvento != null) {
            administradorLlamada.encolarTransaccion(transaccionEvento);
        } else {
            LOGGER.error("Error: multiCallMetaMergeEnded received "
                    + "without open transaction");
        }
        transaccionEvento = null;
    }

    @Override
    public void singleCallMetaSnapshotStarted(MetaEvent evento) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("singleCallMetaSnapshotStarted");
        }
    }

    @Override
    public void singleCallMetaSnapshotEnded(MetaEvent evento) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("singleCallMetaSnapshotEnded");
        }
    }

    @Override
    public void terminalConnectionHeld(CallControlTerminalConnectionEvent evento) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("  terminalConnectionHeld");
        }

        if (transaccionEvento == null) {
            LOGGER.error("Error: terminalConnectionHeld received "
                    + "without open transaction");

            // Create a new transaction
            long idLlamada = UtilidadLlamada.getIdLlamada(evento.getCall());
            transaccionEvento = new TransaccionEvento(idLlamada);
        }
        transaccionEvento.agregar(
                new ControladorConexionTerminalEnEspera(evento));
    }

    @Override
    public void terminalConnectionTalking(
            CallControlTerminalConnectionEvent evento) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("  terminalConnectionTalking");
        }

        if (transaccionEvento == null) {
            LOGGER.error("Error: terminalConnectionTalking received "
                    + "without open transaction");

            // Create a new transaction
            long idLlamada = UtilidadLlamada.getIdLlamada(evento.getCall());
            transaccionEvento = new TransaccionEvento(idLlamada);
        }
        transaccionEvento.agregar(
                new ControladorConexionTerminalHablando(evento));
    }

    @Override
    public void terminalConnectionUnknown(
            CallControlTerminalConnectionEvent evento) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("  terminalConnectionUnknown");
        }

        if (transaccionEvento == null) {
            LOGGER.error("Error: terminalConnectionUnknown received "
                    + "without open transaction");

            // Create a new transaction
            long idLlamada = UtilidadLlamada.getIdLlamada(evento.getCall());
            transaccionEvento = new TransaccionEvento(idLlamada);
        }
        transaccionEvento.agregar(new ControladorConexionTerminalDesconocida(
                evento));
    }

    @Override
    public void connectionAlerting(CallControlConnectionEvent evento) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("  connectionAlerting");
        }

        if (transaccionEvento == null) {
            LOGGER.error("Error: connectionAlerting received "
                    + "without open transaction");

            // Create a new transaction
            long idLlamada = UtilidadLlamada.getIdLlamada(evento.getCall());
            transaccionEvento = new TransaccionEvento(idLlamada);
        }
        transaccionEvento.agregar(new ControladorConexionTimbrando(evento));
    }

    @Override
    public void connectionInitiated(CallControlConnectionEvent evento) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("  connectionInitiated");
        }

        if (transaccionEvento == null) {
            LOGGER.error("Error: connectionInitiated received "
                    + "without open transaction");

            // Create a new transaction
            long idLlamada = UtilidadLlamada.getIdLlamada(evento.getCall());
            transaccionEvento = new TransaccionEvento(idLlamada);
        }
        transaccionEvento.agregar(new ControladorConexionIniciada(evento));
    }

    @Override
    public void connectionEstablished(CallControlConnectionEvent evento) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("  connectionEstablished");
        }

        if (transaccionEvento == null) {
            LOGGER.error("Error: connectionEstablished received "
                    + "without open transaction");

            // Create a new transaction
            long idLlamada = UtilidadLlamada.getIdLlamada(evento.getCall());
            transaccionEvento = new TransaccionEvento(idLlamada);
        }
        transaccionEvento.agregar(new ControladorConexionEstablecida(evento));
    }

    @Override
    public void connectionDisconnected(CallControlConnectionEvent evento) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("  connectionDisconnected");
        }

        if (transaccionEvento == null) {
            LOGGER.error("Error: connectionDisconnected received "
                    + "without open transaction");

            // Create a new transaction
            long idLlamada = UtilidadLlamada.getIdLlamada(evento.getCall());
            transaccionEvento = new TransaccionEvento(idLlamada);
        }
        transaccionEvento.agregar(new ControladorConexionDesconectada(evento));
    }

    // Unused Methods.  Put here for testing
    @Override
    public void terminalConnectionDropped(
            CallControlTerminalConnectionEvent evento) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("  terminalConnectionDropped");
        }
        evento.getTerminalConnection().getTerminal().getName();
    }

    @Override
    public void connectionUnknown(CallControlConnectionEvent evento) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("  connectionUnknown");
        }
    }

    @Override
    public void connectionFailed(CallControlConnectionEvent evento) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("  connectionFailed");
        }
    }

    @Override
    public void terminalConnectionInUse(
            CallControlTerminalConnectionEvent evento) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("  terminalConnectionInUse");
        }
    }

    @Override
    public void connectionOffered(CallControlConnectionEvent evento) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("  connectionOffered");
        }
    }

    @Override
    public void connectionQueued(CallControlConnectionEvent evento) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("  connectionQueued");
        }
    }

    @Override
    public void terminalConnectionBridged(
            CallControlTerminalConnectionEvent evento) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("  terminalConnectionBridged");
        }
    }

    @Override
    public void terminalConnectionRinging(
            CallControlTerminalConnectionEvent evento) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("  terminalConnectionRinging");
        }
    }

    @Override
    public void connectionDialing(CallControlConnectionEvent evento) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("  connectionDialing");
        }
    }

    @Override
    public void connectionNetworkAlerting(CallControlConnectionEvent evento) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("  connectionNetworkAlerting");
        }
    }

    @Override
    public void connectionNetworkReached(CallControlConnectionEvent evento) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("  connectionNetworkReached");
        }
    }

    @Override
    public void agentTerminalBusy(AgentTerminalEvent event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void agentTerminalLoggedOff(AgentTerminalEvent event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void agentTerminalLoggedOn(AgentTerminalEvent event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void agentTerminalNotReady(AgentTerminalEvent event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void agentTerminalReady(AgentTerminalEvent event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void agentTerminalUnknown(AgentTerminalEvent event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void agentTerminalWorkNotReady(AgentTerminalEvent event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void agentTerminalWorkReady(AgentTerminalEvent event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void terminalListenerEnded(TerminalEvent event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
