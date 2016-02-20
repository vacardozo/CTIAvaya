package com.avanza.ctiavaya.ui;

/**
 *
 * @author michelo.vacca
 */
public interface EscenaControlada {
    
    /**
     * Permite establecer el padre ScreenPane
     * @param escenaPadre El padre del controlador.
     */
    public void setEscenaPadre(ControladorEscena escenaPadre);
    
    /**
     * Permite establecer la instancia actual <code>CTIAvaya</code>.
     * @param ctiAvaya Instancia de la aplicación.
     */
    public void setCTIAvaya(CTIAvaya ctiAvaya);
}