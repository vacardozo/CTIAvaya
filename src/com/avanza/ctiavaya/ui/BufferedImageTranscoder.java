package com.avanza.ctiavaya.ui;

import java.awt.image.BufferedImage;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

/**
 *
 * @author michelo.vacca
 */
public class BufferedImageTranscoder extends ImageTranscoder {

    private BufferedImage imagen;
    
    @Override
    public BufferedImage createImage(int ancho, int largo) {
        BufferedImage bufferedImage = new BufferedImage(ancho, largo, BufferedImage.TYPE_INT_ARGB);
        return bufferedImage;
    }

    @Override
    public void writeImage(BufferedImage imagen, TranscoderOutput to) throws
            TranscoderException {
        this.imagen = imagen;
    }

    public BufferedImage getBufferedImage() {
        return imagen;
    }
}