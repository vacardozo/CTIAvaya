package com.avanza.ctiavaya.controlllamada;

import javax.telephony.callcenter.Agent;

/**
 *
 * @author michelo.vacca
 */
public class DetalleAgente {

    private final String idAgente;
    private final String contraseniaAgente;
    private Agent agente;
    // Si se cierra la sesión de agente sin usar la aplicación, fue un cierre de
    // sesión manual.
    private boolean desconexionManual;

    public DetalleAgente(String idAgente, String contraseniaAgente) {
        this.idAgente = idAgente;
        this.contraseniaAgente = contraseniaAgente;
    }

    public String getIdAgente() {
        return idAgente;
    }

    public String getContraseniaAgente() {
        return contraseniaAgente;
    }

    public Agent getAgente() {
        return agente;
    }

    void setAgente(Agent agente) {
        this.agente = agente;
    }

    public boolean isDesconexionManual() {
        return desconexionManual;
    }

    public void setDesconexionManual(boolean desconexionManual) {
        this.desconexionManual = desconexionManual;
    }
}
