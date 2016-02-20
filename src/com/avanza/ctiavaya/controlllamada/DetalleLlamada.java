/**
 * This class encapsulates Call related details to be stored during the life of
 * a call.
 */
package com.avanza.ctiavaya.controlllamada;

import javax.telephony.Call;

public final class DetalleLlamada {

    private final Call llamada;
    private final String idDestinoLlamada;
    private boolean llamadaEnConferencia;

    /**
     * Constructor
     *
     * @param llamada
     * @param idDestinoLlamada
     */
    public DetalleLlamada(Call llamada, String idDestinoLlamada) {
        super();

        this.llamada = llamada;
        llamadaEnConferencia = false;
        this.idDestinoLlamada = idDestinoLlamada;
    }

    /**
     * @return the value of member variable idDestinoLlamada
     */
    public String getIdDestinoLlamada() {
        return idDestinoLlamada;
    }

    /**
     * @return Object of Call class stored as member variable
     */
    public Call getLlamada() {
        return llamada;
    }

    /**
     * @return the value of member variable isCallConference
     */
    public boolean getLlamadaEnConferencia() {
        return llamadaEnConferencia;
    }

    /**
     * @param llamadaEnConferencia the isCallConference to set
     */
    public void setLlamadaEnConferencia(boolean llamadaEnConferencia) {
        this.llamadaEnConferencia = llamadaEnConferencia;
    }
}
