/**
 * The application worker thread. The duty of this thread is to wait for an
 * Event Transaction to be available in the application event queue. Once a
 * Transaction has been put in the queue by the JTAPI Client thread (see:
 * CallControlTerminalConnectionProxy), the WorkerThread receives a
 * notification, removes the Transaction from the queue and processes it.
 */
package com.avanza.ctiavaya.controlllamada;

import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HiloTransacciones extends Thread {

    private static final Logger LOGGER = LogManager.getLogger(HiloTransacciones.class);

    private final AdministradorLlamada administradorLlamada;
    private final Queue<TransaccionEvento> colaTransaccionEventos;

    public HiloTransacciones(AdministradorLlamada administradorLlamada,
            Queue<TransaccionEvento> colaTransaccionEventos) {
        super("CTIAvaya");
        this.administradorLlamada = administradorLlamada;
        this.colaTransaccionEventos = colaTransaccionEventos;
    }

    @Override
    public void run() {
        TransaccionEvento transaction = null;
        while (true) {
            // Wait until able to get a lock on the transaction queue
            synchronized (colaTransaccionEventos) {

                // This thread gets transactions, one at a time, from the  
                // queue.  If the queue is empty, sleep until a transaction
                // is added (see: AdministradorLlamada.encolarTransaccion()).
                while (null == (transaction = colaTransaccionEventos.poll())) {
                    try {
                        colaTransaccionEventos.wait();
                    } catch (InterruptedException e) {
                        // In the case of this exception the application 
                        // needs to continue to monitor the event queue. 
                    }
                }
            }

            // At this point the application has at least one transaction to process.
            while (transaction.tieneMas()) {
                // Process each event in the transaction, one at a time, in the
                // order they were queued.
                controlarEventoLlamada(transaction.siguiente());
            }
        }
    }

    /**
     * Execute the Event handler.
     *
     * @param evento a call event to process
     */
    private void controlarEventoLlamada(final IControladorEvento evento) {
        try {
            evento.ejecutar(administradorLlamada);
        } catch (Exception e) {
            String error = "Exception while handling the Call Events.";
            LOGGER.error(error, e);
            Mensaje mensaje = new Mensaje(error, Mensaje.TipoMensaje.ERROR);
            administradorLlamada.notificar(mensaje);
        }
    }
}
