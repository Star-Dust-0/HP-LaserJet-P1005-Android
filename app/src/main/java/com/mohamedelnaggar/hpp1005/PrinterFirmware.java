package com.mohamedelnaggar.hpp1005;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/* Shared HP P1005 firmware check and load logic.
   Used by the app auto-prepare, the manual setup wizard, and the system print
   service, so every path verifies FWVER the same way and never reloads a
   printer that already reports firmware. */
final class PrinterFirmware {
    static final int STATUS_ERROR = 0;
    static final int STATUS_LOADED = 1;
    static final int STATUS_MISSING = 2;

    private PrinterFirmware() {
    }

    static final class Check {
        final String detail;
        final int status;

        Check(int i, String str) {
            this.status = i;
            this.detail = str;
        }
    }

    /* Progress callbacks so each caller can show the step in its own UI. */
    interface Listener {
        void onChecking();
        void onSending();
        void onVerifying();
        void onPermissionNeeded(UsbDevice device);
    }

    interface AbortCheck {
        boolean aborted();
    }

    /* Thrown when the caller aborts the wait (for example a cancelled job). */
    static final class CancelledException extends Exception {
        CancelledException() {
            super("cancelled");
        }
    }

    /* Reads the IEEE-1284 device ID over the printer-class interface and
       reports whether it carries FWVER. */
    static Check check(Context context, UsbManager usbManager) {
        UsbDevice findPrinter = UsbPrinterIO.findPrinter(usbManager);
        if (findPrinter == null) {
            return new Check(STATUS_ERROR, context.getString(R.string.text_401));
        }
        if (!usbManager.hasPermission(findPrinter)) {
            return new Check(STATUS_ERROR, context.getString(R.string.text_403));
        }
        UsbDeviceConnection usbDeviceConnection = null;
        try {
            try {
                usbDeviceConnection = usbManager.openDevice(findPrinter);
            } catch (Exception e) {
                return new Check(STATUS_ERROR, e.getClass().getSimpleName() + ": " + e.getMessage());
            }
            if (usbDeviceConnection == null) {
                return new Check(STATUS_ERROR, context.getString(R.string.text_404));
            }
            int i = -1;
            for (int i2 = 0; i2 < findPrinter.getInterfaceCount(); i2++) {
                if (findPrinter.getInterface(i2).getInterfaceClass() == 7) {
                    i = findPrinter.getInterface(i2).getId();
                    break;
                }
            }
            if (i < 0) {
                return new Check(STATUS_ERROR, context.getString(R.string.text_405));
            }
            byte[] bArr = new byte[1024];
            int controlTransfer = usbDeviceConnection.controlTransfer(161, 0, 0, i, bArr, 1024, 3000);
            if (controlTransfer < 2) {
                return new Check(STATUS_ERROR, context.getString(R.string.text_406) + controlTransfer + ").");
            }
            String str = new String(bArr, 2, Math.min(Math.max(0, (((bArr[0] & 255) << 8) | (bArr[1] & 255)) - 2), controlTransfer - 2), StandardCharsets.US_ASCII);
            return str.contains("FWVER:") ? new Check(STATUS_LOADED, str) : new Check(STATUS_MISSING, str);
        } finally {
            if (usbDeviceConnection != null) {
                usbDeviceConnection.close();
            }
        }
    }

    /* Sends the built-in sihpp1005 firmware over the bulk OUT endpoint.
       Returns the number of bytes transferred. */
    static int send(Context context, UsbManager usbManager, UsbDevice usbDevice) throws Exception {
        InputStream openRawResource = context.getResources().openRawResource(R.raw.sihpp1005);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bArr = new byte[8192];
        while (true) {
            int read = openRawResource.read(bArr);
            if (read == -1) {
                break;
            }
            byteArrayOutputStream.write(bArr, 0, read);
        }
        openRawResource.close();
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        if (byteArray.length == 0) {
            throw new IllegalStateException(context.getString(R.string.text_425));
        }
        UsbDeviceConnection openDevice = usbManager.openDevice(usbDevice);
        if (openDevice == null) {
            throw new IllegalStateException(context.getString(R.string.text_416));
        }
        UsbInterface usbInterface = null;
        boolean claimed = false;
        try {
            for (int i = 0; i < usbDevice.getInterfaceCount(); i++) {
                UsbInterface candidate = usbDevice.getInterface(i);
                if (candidate.getInterfaceClass() == 7) {
                    usbInterface = candidate;
                    break;
                }
            }
            if (usbInterface == null) {
                throw new IllegalStateException(context.getString(R.string.text_001));
            }
            claimed = openDevice.claimInterface(usbInterface, true);
            if (!claimed) {
                throw new IllegalStateException(context.getString(R.string.text_002));
            }
            UsbEndpoint usbEndpoint = null;
            for (int i2 = 0; i2 < usbInterface.getEndpointCount(); i2++) {
                UsbEndpoint endpoint = usbInterface.getEndpoint(i2);
                if (endpoint.getDirection() == 0 && endpoint.getType() == 2) {
                    usbEndpoint = endpoint;
                    break;
                }
            }
            if (usbEndpoint == null) {
                throw new IllegalStateException(context.getString(R.string.text_003));
            }
            int i3 = 0;
            while (i3 < byteArray.length) {
                int bulkTransfer = openDevice.bulkTransfer(usbEndpoint, byteArray, i3, Math.min(16384, byteArray.length - i3), 10000);
                if (bulkTransfer <= 0) {
                    throw new IllegalStateException(context.getString(R.string.text_417) + i3 + context.getString(R.string.text_016) + byteArray.length + context.getString(R.string.text_418) + bulkTransfer + ").");
                }
                i3 += bulkTransfer;
            }
            return i3;
        } finally {
            if (claimed && usbInterface != null) {
                try {
                    openDevice.releaseInterface(usbInterface);
                } catch (Exception unused) {
                }
            }
            openDevice.close();
        }
    }

    /* Ensures the printer is prepared: if it already reports FWVER it is
       returned as-is (never re-flashed); otherwise the built-in firmware is
       sent and the printer is polled until it re-enumerates and reports
       FWVER. Returns the device handle to use afterwards, which can differ
       from the one passed in because the printer re-enumerates when the
       firmware loads. Throws Exception with a localized message on failure,
       or CancelledException when abortCheck fires.
       Callers must hold UsbPrinterIO.JOB_LOCK. */
    static UsbDevice ensureReady(Context context, UsbManager usbManager, UsbDevice usbDevice, Listener listener, AbortCheck abortCheck) throws Exception {
        if (abortCheck != null && abortCheck.aborted()) {
            throw new CancelledException();
        }
        if (listener != null) {
            listener.onChecking();
        }
        Thread.sleep(700L);
        Check initial = check(context, usbManager);
        if (initial.status == STATUS_LOADED) {
            return usbDevice;
        }
        if (initial.status == STATUS_ERROR) {
            throw new IllegalStateException(initial.detail);
        }
        if (listener != null) {
            listener.onSending();
        }
        send(context, usbManager, usbDevice);
        if (listener != null) {
            listener.onVerifying();
        }
        String lastError = null;
        for (int i = 0; i < 20; i++) {
            if (abortCheck != null && abortCheck.aborted()) {
                throw new CancelledException();
            }
            Thread.sleep(2000L);
            UsbDevice current = UsbPrinterIO.findPrinter(usbManager);
            if (current == null) {
                continue;
            }
            if (!usbManager.hasPermission(current)) {
                if (listener != null) {
                    listener.onPermissionNeeded(current);
                }
            } else {
                Check poll = check(context, usbManager);
                if (poll.status == STATUS_LOADED) {
                    return current;
                }
                lastError = poll.status == STATUS_MISSING ? context.getString(R.string.text_462) : poll.detail;
            }
        }
        throw new IllegalStateException(lastError == null ? context.getString(R.string.text_462) : lastError);
    }
}
