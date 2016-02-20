package com.avanza.ctiavaya.controlllamada;

import java.util.Observer;

import javax.telephony.callcenter.Agent;

/**
 * This Interface defines the AgentStates enum and notify method which is
 implemented by the AgentStateUI for receiving the EstadoAgente change
 notification
 */
public interface IEstadoAgente extends Observer {

    enum EstadoAgente {

        DESCONOCIDO(Agent.UNKNOWN),
        CONECTADO(Agent.LOG_IN),
        DESCONECTADO(Agent.LOG_OUT),
        DISPONIBLE(Agent.READY),
        NO_DISPONIBLE(Agent.NOT_READY),
        OCUPADO(Agent.BUSY),
        TRABAJO_NO_TERMINADO(Agent.WORK_NOT_READY),
        TRABAJO_TERMINADO(Agent.WORK_READY),
        DESCONEXION_MANUAL(8);

        private final int estado;

        /**
         * Sets the agent state
         *
         * @param estado
         */
        private EstadoAgente(int estado) {
            this.estado = estado;
        }

        /**
         * Gets the state of the agent
         *
         * @return
         */
        public int getEstado() {
            return this.estado;
        }

        /**
         * To get the state of the agent
         *
         * @param estado
         * @return agent state
         */
        public static EstadoAgente getEstadoAgente(int estado) {
            EstadoAgente estadoAgente = null;
            for (EstadoAgente tempState : EstadoAgente.values()) {
                if (tempState.getEstado() == estado) {
                    estadoAgente = tempState;
                    break;
                }
            }
            return estadoAgente;
        }
    }
}
