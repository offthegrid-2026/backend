package com.event.otg_backend.helpers.ticket;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class TicketPdfGenerator {

    private static final int WIDTH = 900;
    private static final int HEIGHT = 500;

    private TicketPdfGenerator(){}

    public static byte [] generate(Long userId, String fullName, byte[] qrPng) throws IOException {
        BufferedImage canvas = renderTicketImage(userId, fullName, qrPng);
        return toPdf(canvas);
    }

    private static BufferedImage renderTicketImage(Long userId, String fullName, byte[] qrPng) throws IOException{

        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        int centerX = WIDTH/2;
        g.setColor(Color.BLACK);

        //ID number
        g.setFont(new Font("SansSerif", Font.BOLD, 28));
        drawCentered(g, "ID No: " + userId, centerX, 70);

        //Name
        g.setFont(new Font("SansSerif", Font.PLAIN, 24));
        drawCentered(g, fullName, centerX, 115);

        //QR
        BufferedImage qr = ImageIO.read(new ByteArrayInputStream(qrPng));
        int qrSize = 260;
        g.drawImage(qr, centerX - qrSize / 2, 150, qrSize, qrSize, null);

        g.dispose();
        return img;
    }

    private static void drawCentered(Graphics2D g, String text, int centerX, int y){
        int w = g.getFontMetrics().stringWidth(text);
        g.drawString(text, centerX - w/2, y);
    }

    private static byte[] toPdf(BufferedImage image) throws IOException {
        try (PDDocument doc = new PDDocument()){
            PDPage page = new PDPage(new PDRectangle(image.getWidth(),  image.getHeight()));
            doc.addPage(page);

            PDImageXObject pdImage = LosslessFactory.createFromImage(doc, image);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)){
                cs.drawImage(pdImage, 0, 0, image.getWidth(), image.getHeight());
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

}
