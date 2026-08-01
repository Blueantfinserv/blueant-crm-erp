package com.blueant_crm_erp.util.file;

import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

/**
 * Utility class for Image operations.
 *
 * Responsibilities:
 * - Read image
 * - Resize image
 * - Compress image
 * - Convert image format
 * - Get image dimensions
 * - Validate image
 *
 * This utility DOES NOT:
 * - Upload image
 * - Store image
 * - Save database records
 *
 * Used By:
 * - User Module (Profile Photo)
 * - Client Module
 * - Service Request Module
 * - KYC Module
 * - Document Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class ImageUtil {

    private ImageUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Reads MultipartFile into BufferedImage.
     */
    public static BufferedImage read(MultipartFile file) throws IOException {

        Objects.requireNonNull(file, "Image file cannot be null.");

        return ImageIO.read(file.getInputStream());
    }

    /**
     * Reads byte array into BufferedImage.
     */
    public static BufferedImage read(byte[] bytes) throws IOException {

        Objects.requireNonNull(bytes);

        return ImageIO.read(new ByteArrayInputStream(bytes));
    }

    /**
     * Resize image.
     */
    public static BufferedImage resize(
            BufferedImage source,
            int width,
            int height) {

        Objects.requireNonNull(source);

        BufferedImage resized =
                new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = resized.createGraphics();

        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        graphics.drawImage(source, 0, 0, width, height, null);

        graphics.dispose();

        return resized;
    }

    /**
     * Create thumbnail.
     */
    public static BufferedImage thumbnail(
            BufferedImage source) {

        return resize(source, 200, 200);
    }

    /**
     * Compress image.
     *
     * Quality:
     * 0.0 -> Lowest
     * 1.0 -> Highest
     */
    public static byte[] compress(
            BufferedImage image,
            float quality) throws IOException {

        Objects.requireNonNull(image);

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        Iterator<ImageWriter> writers =
                ImageIO.getImageWritersByFormatName("jpg");

        if (!writers.hasNext()) {
            throw new IOException("No JPEG writer available.");
        }

        ImageWriter writer = writers.next();

        ImageWriteParam writeParam =
                writer.getDefaultWriteParam();

        writeParam.setCompressionMode(
                ImageWriteParam.MODE_EXPLICIT);

        writeParam.setCompressionQuality(quality);

        try (ImageOutputStream ios =
                     ImageIO.createImageOutputStream(outputStream)) {

            writer.setOutput(ios);

            writer.write(
                    null,
                    new IIOImage(image, null, null),
                    writeParam
            );

            writer.dispose();
        }

        return outputStream.toByteArray();
    }

    /**
     * Convert image to bytes.
     */
    public static byte[] toBytes(
            BufferedImage image,
            String format) throws IOException {

        Objects.requireNonNull(image);

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        ImageIO.write(image, format, outputStream);

        return outputStream.toByteArray();
    }

    /**
     * Returns width.
     */
    public static int width(BufferedImage image) {

        Objects.requireNonNull(image);

        return image.getWidth();
    }

    /**
     * Returns height.
     */
    public static int height(BufferedImage image) {

        Objects.requireNonNull(image);

        return image.getHeight();
    }

    /**
     * Returns true if image is valid.
     */
    public static boolean isValid(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return false;
        }

        try {
            return ImageIO.read(file.getInputStream()) != null;
        } catch (IOException ex) {
            return false;
        }
    }

}