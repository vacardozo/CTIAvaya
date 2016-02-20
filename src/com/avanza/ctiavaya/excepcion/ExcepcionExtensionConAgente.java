package com.avanza.ctiavaya.excepcion;

/**
 *
 * @author michelo.vacca
 */
public class ExcepcionExtensionConAgente extends Exception {

    private final String plantillaMensaje;
    private StringBuilder mensaje;

    private ExcepcionExtensionConAgente() {
        this.plantillaMensaje = "El Agente %s es registrado en la extensión %s.";
        mensaje = new StringBuilder(Short.MAX_VALUE);
    }

    public ExcepcionExtensionConAgente(String idAgente, String extension) {
        this();
        mensaje.append(String.format(plantillaMensaje, idAgente, extension));
    }

    public ExcepcionExtensionConAgente(Throwable causa) {
        super(causa);
        this.plantillaMensaje = "Agent %s is Logged In at the extension %s.";
    }

    @Override
    public String getMessage() {
        if (null != super.getMessage()) {
            mensaje.append(super.getMessage());
        }
        return mensaje.toString();
    }
}
