package com.avanza.ctiavaya.controlllamada;

/**
 *
 * @author michelo.vacca
 */
public class Mensaje {
    private String descripcion;
    private TipoMensaje tipo;
    private Object objeto;
    
    
    public enum TipoMensaje {

        ERROR("ERROR"),
        INFO("INFO");

        private final String tipo;

        private TipoMensaje(String tipo) {
            this.tipo = tipo;
        }

        public String getTipo() {
            return tipo;
        }
        
        public static TipoMensaje getTipoMensaje(String tipo) {
            TipoMensaje tipoMensaje = null;
            if(null != tipo) {
                for (TipoMensaje valor : TipoMensaje.values()) {
                    if(valor.getTipo().equals(tipo)) {
                        tipoMensaje = valor;
                    }
                }
            }
            return tipoMensaje;
        }
    }

    public Mensaje() {
    }

    public Mensaje(String descripcion, TipoMensaje tipo) {
        this.descripcion = descripcion;
        this.tipo = tipo;
    }
    
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public TipoMensaje getTipo() {
        return tipo;
    }

    public void setTipo(TipoMensaje tipo) {
        this.tipo = tipo;
    }

    public Object getObjeto() {
        return objeto;
    }

    public void setObjeto(Object objeto) {
        this.objeto = objeto;
    }
    
    @Override
    public String toString() {
        return descripcion;
    }
}