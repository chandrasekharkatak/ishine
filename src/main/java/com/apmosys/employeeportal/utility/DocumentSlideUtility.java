package com.apmosys.employeeportal.utility;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.*;

public class DocumentSlideUtility {

    public static String convertDocumentToSlides(String baseStoragePath, String relativePath, Integer contentId) {

        try {

            Path filePath = Paths.get(baseStoragePath)
                    .resolve(relativePath)
                    .toAbsolutePath()
                    .normalize();

            String ext = getExtension(filePath.toString());

            Path parentDir = filePath.getParent();

            // Create slides-{contentId} directory directly under the parent
            Path slidesDir = parentDir.resolve("slides-" + contentId);

            if (!Files.exists(slidesDir)) {
                Files.createDirectories(slidesDir);
            }

            if (ext.equalsIgnoreCase("ppt") || ext.equalsIgnoreCase("pptx")) {
                convertPPTToImages(filePath.toString(), slidesDir.toString());
            }

            if (ext.equalsIgnoreCase("pdf")) {
                convertPDFToImages(filePath.toString(), slidesDir.toString());
            }

            return slidesDir.toString();

        } catch (Exception e) {
            throw new RuntimeException("Slide conversion failed", e);
        }
    }

    private static void convertPPTToImages(String pptPath, String outputDir) throws Exception {

        FileInputStream fis = new FileInputStream(pptPath);
        XMLSlideShow ppt = new XMLSlideShow(fis);

        Dimension pgsize = ppt.getPageSize();

        int index = 1;

        for (XSLFSlide slide : ppt.getSlides()) {

            BufferedImage img = new BufferedImage(
                    pgsize.width,
                    pgsize.height,
                    BufferedImage.TYPE_INT_RGB
            );

            Graphics2D graphics = img.createGraphics();

            graphics.setPaint(Color.white);
            graphics.fill(new Rectangle(pgsize));

            slide.draw(graphics);

            File output = new File(outputDir + "/slide-" + index + ".png");

            ImageIO.write(img, "png", output);

            index++;
        }

        ppt.close();
    }

    private static void convertPDFToImages(String pdfPath, String outputDir) throws Exception {

        PDDocument document = PDDocument.load(new File(pdfPath));

        PDFRenderer renderer = new PDFRenderer(document);

        int pages = document.getNumberOfPages();

        for (int i = 0; i < pages; i++) {

            BufferedImage image = renderer.renderImageWithDPI(i, 300);

            File output = new File(outputDir + "/page-" + (i + 1) + ".png");

            ImageIO.write(image, "png", output);
        }

        document.close();
    }

    private static String getExtension(String filename) {

        int index = filename.lastIndexOf(".");

        if (index == -1) return "";

        return filename.substring(index + 1);
    }
}