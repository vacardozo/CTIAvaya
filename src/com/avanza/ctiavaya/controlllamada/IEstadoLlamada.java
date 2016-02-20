package com.avanza.ctiavaya.controlllamada;

/**
 * This interface defines the Call/ConnectionState for a Call and a notify()
 * method implemented by the AgentState UI for notification of the change in
 * Call/ConnectionState of the Call.
 */
import java.util.Observer;

import javax.telephony.Call;
import javax.telephony.Connection;

/**
 * This interface defines the Call/Connection State change notifications that an
 * observer (AgentStateUI) needs to extend.
 */
public interface IEstadoLlamada extends Observer {

    enum EstadoConexion {

        NINGUNA(-1),
        NULA(0),
        INICIADA(Connection.INPROGRESS),
        TIMBRANDO(Connection.ALERTING),
        CONECTADA(Connection.CONNECTED),
        EN_ESPERA(4),
        EN_CONFERENCIA(5),
        TRANSFERIDA(6),
        DESCONOCIDA(Connection.UNKNOWN),
        REANUDADA(8),
        TERCERO_CUELGA(9),
        AGREGANDO_TERCERO(10),
        CONFERENCIA_INICIADA(11),
        CONFERENCIA_TIMBRANDO(12);

        private final int estado;

        /**
         * Sets connection estado
         *
         * @param estado
         */
        private EstadoConexion(int estado) {
            this.estado = estado;
        }

        /**
         * To get the connection estado
         *
         * @return connection estado
         */
        public int getEstado() {
            return this.estado;
        }
        
        public static EstadoConexion getEstadoConexion(int estado) {
            EstadoConexion estadoConexion = NULA;
            for (EstadoConexion valor : EstadoConexion.values()) {
                if(valor.getEstado() == estado) {
                    estadoConexion = valor;
                    break;
                }
            }
            return estadoConexion;
        }
    }

    enum EstadoLlamada {

        INVALIDA(Call.INVALID),
        IDLE(Call.IDLE),
        ACTIVA(Call.ACTIVE);

        private final int estado;

        /**
         * Sets the call estado
         *
         * @param estado
         */
        private EstadoLlamada(int estado) {
            this.estado = estado;
        }

        /**
         * To get the call estado
         *
         * @return call estado
         */
        public int getEstado() {
            return this.estado;
        }
        
        public static EstadoLlamada getEstadoLlamada(int estado) {
            EstadoLlamada callState = null;
            for (EstadoLlamada valor : EstadoLlamada.values()) {
                if(valor.getEstado() == estado) {
                    callState = valor;
                    break;
                }
            }
            return callState;
        }
    }
}
