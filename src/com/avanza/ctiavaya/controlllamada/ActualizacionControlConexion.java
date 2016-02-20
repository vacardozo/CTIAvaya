/**
 * This class is a wrapper to hold the data sent from the CallManager to the UI
 * whenever a noteworthy event is received from the AE Services.
 */
package com.avanza.ctiavaya.controlllamada;

import com.avanza.ctiavaya.controlllamada.IEstadoLlamada.EstadoConexion;
import com.avanza.ctiavaya.controlllamada.IEstadoLlamada.EstadoLlamada;

import com.google.gson.annotations.Expose;

public class ActualizacionControlConexion {

    @Expose
    private final EstadoConexion estadoConexion;
    @Expose
    private final EstadoLlamada estadoLlamada;
    @Expose
    private final long idLlamada;
    @Expose
    private final String extensionACD;
    @Expose
    private final String ucid;
    @Expose
    private final String destino;
    @Expose
    private final String origen;
    @Expose
    private final String tercero;

    public ActualizacionControlConexion(EstadoConexion estadoConexion, EstadoLlamada estadoLlamada,
            long idLlamada, String extensionACD, String ucid, String destino,
            String origen, String tercero) {
        this.estadoConexion = estadoConexion;
        this.estadoLlamada = estadoLlamada;
        this.idLlamada = idLlamada;
        this.extensionACD = extensionACD;
        this.ucid = ucid;
        this.destino = destino;
        this.origen = origen;
        this.tercero = tercero;
    }

    public EstadoConexion getEstadoConexion() {
        return estadoConexion;
    }

    public EstadoLlamada getEstadoLlamada() {
        return estadoLlamada;
    }

    public long getIdLlamada() {
        return idLlamada;
    }

    public String getExtensionACD() {
        return extensionACD;
    }

    public String getUcid() {
        return ucid;
    }

    public String getDestino() {
        return destino;
    }

    public String getOrigen() {
        return origen;
    }

    public String getTercero() {
        return tercero;
    }
}
