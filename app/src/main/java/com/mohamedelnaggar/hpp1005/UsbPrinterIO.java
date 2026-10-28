package com.mohamedelnaggar.hpp1005;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.locks.ReentrantLock;

/* loaded from: classes.dex */
public final class UsbPrinterIO {
    public static final int HP_VENDOR_ID = 1008;
    public static final ReentrantLock JOB_LOCK = new ReentrantLock(true);
    public static final int P1005_PRODUCT_ID = 15639;

    private UsbPrinterIO() {
    }

    public static boolean isP1005(UsbDevice usbDevice) {
        return usbDevice != null && usbDevice.getVendorId() == 1008 && usbDevice.getProductId() == 15639;
    }

    public static UsbDevice findPrinter(UsbManager usbManager) {
        if (usbManager == null) {
            return null;
        }
        for (UsbDevice usbDevice : usbManager.getDeviceList().values()) {
            if (isP1005(usbDevice)) {
                return usbDevice;
            }
        }
        return null;
    }

    public static synchronized int sendXqx(UsbManager usbManager, File file, UsbDevice usbDevice) throws Exception {
        UsbDeviceConnection connection = null;
        UsbInterface printerInterface = null;
        boolean claimed = false;
        try {
            connection = usbManager.openDevice(usbDevice);
            if (connection == null) {
                throw new IllegalStateException("Android could not open the USB printer.");
            }
            for (int i = 0; i < usbDevice.getInterfaceCount(); i++) {
                UsbInterface candidate = usbDevice.getInterface(i);
                if (candidate.getInterfaceClass() == 7) {
                    printerInterface = candidate;
                    break;
                }
            }
            if (printerInterface == null) {
                throw new IllegalStateException("No USB printer-class interface was found.");
            }
            claimed = connection.claimInterface(printerInterface, true);
            if (!claimed) {
                throw new IllegalStateException("Could not claim the printer interface.");
            }
            UsbEndpoint outEndpoint = null;
            for (int i = 0; i < printerInterface.getEndpointCount(); i++) {
                UsbEndpoint endpoint = printerInterface.getEndpoint(i);
                if (endpoint.getDirection() == 0 && endpoint.getType() == 2) {
                    outEndpoint = endpoint;
                    break;
                }
            }
            if (outEndpoint == null) {
                throw new IllegalStateException("No bulk OUT endpoint was found.");
            }
            byte[] buffer = new byte[16384];
            int sentTotal = 0;
            FileInputStream input = new FileInputStream(file);
            try {
                while (true) {
                    int read = input.read(buffer);
                    if (read == -1) {
                        break;
                    }
                    int offset = 0;
                    while (offset < read) {
                        int sent = connection.bulkTransfer(outEndpoint, buffer, offset, read - offset, 10000);
                        if (sent <= 0) {
                            throw new IllegalStateException("USB transfer stopped after " + sentTotal + " bytes (result=" + sent + ").");
                        }
                        sentTotal += sent;
                        offset += sent;
                    }
                }
            } finally {
                input.close();
            }
            return sentTotal;
        } finally {
            if (connection != null) {
                if (claimed && printerInterface != null) {
                    try {
                        connection.releaseInterface(printerInterface);
                    } catch (Exception unused) {
                    }
                }
                connection.close();
            }
        }
    }
}
