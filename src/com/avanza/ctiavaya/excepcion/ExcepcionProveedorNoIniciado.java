package com.avanza.ctiavaya.excepcion;

/**
 *
 * @author michelo.vacca
 */
public class ExcepcionProveedorNoIniciado extends Exception {
    private StringBuilder mensaje;

    public ExcepcionProveedorNoIniciado() {
        mensaje = new StringBuilder(Short.MAX_VALUE);
        mensaje.append("El proveedor no ha sido iniciado.\n");
    }
    
    public ExcepcionProveedorNoIniciado(Throwable causa) {
        super(causa);
    }
    
    @Override
    public String getMessage() {
        mensaje.append(super.getMessage());
        return mensaje.toString();
    }
}