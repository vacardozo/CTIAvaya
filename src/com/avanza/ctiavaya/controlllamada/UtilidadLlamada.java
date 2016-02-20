/**
 * This class contains several Static utility methods which are used by the Call
 * Manager and Handler classes.
 */
package com.avanza.ctiavaya.controlllamada;

import javax.telephony.Call;
import javax.telephony.callcenter.ACDAddress;

import com.avaya.jtapi.tsapi.ITsapiCallIDPrivate;
import com.avaya.jtapi.tsapi.LucentCallInfo;
import com.avaya.jtapi.tsapi.LucentV5CallInfo;
import com.avaya.jtapi.tsapi.LucentV7CallInfo;
import com.avaya.jtapi.tsapi.V7DeviceHistoryEntry;

public class UtilidadLlamada {

    public static final int MIN_CONFERENCE_CONN = 3;
    public static final int MIN_CONN = 2;

    /**
     * Retrieve an ACD address associated with the call
     *
     * @param llamada to get an ACD address
     * @return ACD address name if call has associated ACD address, otherwise
     * null
     */
    public static String getExtensionACDLlamada(Call llamada) {
        String nombreACDAddress = null;
        if (llamada instanceof LucentCallInfo) {
            LucentCallInfo lucentCall = (LucentCallInfo) llamada;
            ACDAddress extensionACD = lucentCall.getDeliveringACDAddress();
            if (null != extensionACD) {
                nombreACDAddress = extensionACD.getName();
            }
        }

        return nombreACDAddress;
    }

    /**
     * Retrieve an event cause from the device history for provided call object
     *
     * @param llamada to check
     * @return An event cause code or -1 if value cannot be retrieved
     */
    public static short getCausaEventoLlamada(Call llamada) {
        short causa = -1;
        if (llamada instanceof LucentV7CallInfo) {
            LucentV7CallInfo lucentCall = (LucentV7CallInfo) llamada;

            V7DeviceHistoryEntry[] deviceHistory = lucentCall
                    .getDeviceHistory();

            if (null != deviceHistory && null != deviceHistory[0]) {
                causa = deviceHistory[0].getEventCause();
            }
        }
        return causa;
    }

    /**
     * Return the call ID of a call
     *
     * @param llamada
     * @return Call Identifier
     */
    public static long getIdLlamada(Call llamada) {
        long idLlamada = 0;
        if (null != llamada) {
            idLlamada = ((ITsapiCallIDPrivate) llamada).getTsapiCallID();
        }
        return idLlamada;
    }

    /**
     * Retrieve old call identifier from provided call The term 'old call'
     * refers to the call that was in progress before a transfer or conference.
     *
     * @param llamada
     * @return Old call identifier or 0
     */
    public static long getIdLlamadaAnterior(Call llamada) {
        long idLlamadaAnterior = 0;
        if (llamada instanceof LucentV7CallInfo) {
            LucentV7CallInfo lucentCall = (LucentV7CallInfo) llamada;

            V7DeviceHistoryEntry[] deviceHistory = lucentCall
                    .getDeviceHistory();

            try {
                if (0 != deviceHistory.length && null != deviceHistory[0].
                        getOldConnectionID()) {
                    idLlamadaAnterior = deviceHistory[0].getOldConnectionID().
                            getCallID();
                }
            } catch (RuntimeException e) {
                // Do not need to do anything here.
            }
        }

        return idLlamadaAnterior;
    }

    /**
     * Retrieve old call device identifier from provided call. The term 'old
     * call' refers to the call that was in progress before a transfer or
     * conference.
     *
     * @param llamada
     * @return Old call device identifier or null
     */
    public static String getExtensionAnterior(Call llamada) {
        String idDispositivoAnterior = null;
        if (llamada instanceof LucentV7CallInfo) {

            LucentV7CallInfo lucentCall = (LucentV7CallInfo) llamada;

            V7DeviceHistoryEntry[] deviceHistory
                    = lucentCall.getDeviceHistory();

            try {
                idDispositivoAnterior = deviceHistory[0].getOldDeviceID();
            } catch (RuntimeException e) {
                // Do not need to do anything here.
            }
        }
        return idDispositivoAnterior;
    }

    public static String getUCID(Call llamada) {
        String ucid = "";
        if (null != llamada) {
            ucid = ((LucentV5CallInfo) llamada).getUCID();
        }
        return ucid;
    }
}
