/**
 * All events in a Meta event must be received before any can be processed. This
 * class holds all the events associated with a single Meta Event. Each time a
 * xxxCallMetaStarted is received, a new EventTransaction is created. As each
 * Event is received, it is added to the EventTransaction. When a
 * xxxCallMetaProgressEnded is received, the EventTransaction is placed on the
 * Event Queue of the CallManager.
 */
package com.avanza.ctiavaya.controlllamada;

import java.util.ArrayList;
import java.util.List;

public class TransaccionEvento {

    private final List<IControladorEvento> transaccion;
    private int cuenta = 0;
    private long idLlamada;

    public TransaccionEvento() {
        transaccion = new ArrayList<>();
    }

    public TransaccionEvento(final long idLlamada) {
        this();
        this.idLlamada = idLlamada;
    }

    public void agregar(IControladorEvento tarea) {
        transaccion.add(tarea);
    }

    public IControladorEvento siguiente() {
        IControladorEvento controladorEvento = null;
        if (cuenta < transaccion.size()) {
            controladorEvento = transaccion.get(cuenta++);
        }
        return controladorEvento;
    }

    public boolean tieneMas() {
        return cuenta < transaccion.size();
    }

    public long getIdLlamada() {
        return idLlamada;
    }
}
