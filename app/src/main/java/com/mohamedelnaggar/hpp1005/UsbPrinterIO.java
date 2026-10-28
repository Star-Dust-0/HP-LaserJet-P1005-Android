package com.mohamedelnaggar.hpp1005;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import java.io.File;
import java.io.FileInputStream;

/**
 * Shared HP LaserJet P1005 USB transport used by the app UI (MainActivity)
 * and the system print service (HpPrintService). One source of truth for
 * device identity and the XQX stream write.
 */
public final class UsbPrinterIO {
    public static final int HP_VENDOR_ID = 0x03F0;
    public static final int P1005_PRODUCT_ID = 0x3D17;

    private UsbPrinterIO() {}

    /** Process-wide fair lock held for an ENTIRE print job (all pages), by
     * both the in-app print pass and the print-service executor, so two jobs
     * can never interleave page streams on the one physical printer. */
    public static final java.util.concurrent.locks.ReentrantLock JOB_LOCK =
            new java.util.concurrent.locks.ReentrantLock(true);

    public static boolean isP1005(UsbDevice device) {
        return device != null && device.getVendorId() == HP_VENDOR_ID
                && device.getProductId() == P1005_PRODUCT_ID;
    }

    public static UsbDevice findPrinter(UsbManager usbManager) {
        if (usbManager == null) return null;
        for (UsbDevice device : usbManager.getDeviceList().values()) {
            if (isP1005(device)) return device;
        }
        return null;
    }

    /**
     * Streams an XQX file to the printer over the USB bulk OUT endpoint.
     * Returns the total bytes accepted by the USB stack. A successful return
     * means the printer accepted the bytes, not that paper physically exited.
     */
    // One physical printer: the in-app print thread and the print-service
    // executor must never interleave XQX streams on the USB endpoint.
    public static synchronized int sendXqx(UsbManager usbManager, File file, UsbDevice printer) throws Exception {
        UsbDeviceConnection connection = null;
        UsbInterface printerInterface = null;
        boolean claimed = false;
        try {
            connection = usbManager.openDevice(printer);
            if (connection == null) throw new IllegalStateException("تعذّر على أندرويد فتح طابعة USB.");
            UsbEndpoint outEndpoint = null;
            for (int i = 0; i < printer.getInterfaceCount(); i++) {
                UsbInterface candidate = printer.getInterface(i);
                if (candidate.getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER) {
                    printerInterface = candidate;
                    break;
                }
            }
            if (printerInterface == null) throw new IllegalStateException("لم يُعثر على واجهة من فئة الطابعات USB.");
            claimed = connection.claimInterface(printerInterface, true);
            if (!claimed) throw new IllegalStateException("تعذّر حجز واجهة الطابعة.");
            for (int i = 0; i < printerInterface.getEndpointCount(); i++) {
                UsbEndpoint endpoint = printerInterface.getEndpoint(i);
                if (endpoint.getDirection() == UsbConstants.USB_DIR_OUT
                        && endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                    outEndpoint = endpoint;
                    break;
                }
            }
            if (outEndpoint == null) throw new IllegalStateException("لم يُعثر على منفذ bulk OUT.");
            byte[] buffer = new byte[16 * 1024];
            int sentTotal = 0;
            try (FileInputStream input = new FileInputStream(file)) {
                int read;
                while ((read = input.read(buffer)) != -1) {
                    int offset = 0;
                    while (offset < read) {
                        int sent = connection.bulkTransfer(outEndpoint, buffer, offset,
                                read - offset, 10000);
                        if (sent <= 0) throw new IllegalStateException("توقّف نقل USB بعد " + sentTotal + " بايت (result=" + sent + ").");
                        sentTotal += sent;
                        offset += sent;
                    }
                }
            }
            return sentTotal;
        } finally {
            if (connection != null) {
                if (claimed && printerInterface != null) {
                    try { connection.releaseInterface(printerInterface); } catch (Exception ignored) {}
                }
                connection.close();
            }
        }
    }
}
