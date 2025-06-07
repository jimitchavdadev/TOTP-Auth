// ============================================================================
// QR Code Service
// ============================================================================
package com.totp.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.totp.util.ConsoleColors;

public class QRCodeService {

    public void displayQRCode(String text, String secret) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 45, 45);

        System.out.println("\n" + "=".repeat(bitMatrix.getWidth() + 4));

        for (int y = 0; y < bitMatrix.getHeight(); y++) {
            System.out.print("= ");
            for (int x = 0; x < bitMatrix.getWidth(); x++) {
                if (bitMatrix.get(x, y)) {
                    System.out.print(ConsoleColors.BLACK_BG + "  " + ConsoleColors.RESET);
                } else {
                    System.out.print(ConsoleColors.WHITE_BG + "  " + ConsoleColors.RESET);
                }
            }
            System.out.println(" =");
        }

        System.out.println("=".repeat(bitMatrix.getWidth() + 4));
        System.out.println("\n" + ConsoleColors.YELLOW + "📱 Secret Key (manual entry): " + ConsoleColors.RESET);
        System.out.println("If QR scan fails, enter this key manually: " + ConsoleColors.CYAN + secret + ConsoleColors.RESET);
    }
}
