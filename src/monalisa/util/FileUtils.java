/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

import monalisa.resources.ResourceManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class FileUtils {

    private static final Logger LOGGER = LogManager.getLogger(FileUtils.class);

    /**
     * Extracts a resource into a temporary file.
     *
     * @param resource The path to the resource, relative to the
     * {@code org/monalisa/resources} directory.
     * @param prefix A prefix to use for the temporary file.
     * @param suffix A suffix to use for the temporary file.
     * @return Returns a file instance for the temporary file.
     * @throws IOException
     */
    public static File extractResource(String resource, String prefix,
            String suffix) throws IOException {
        LOGGER.debug("Extracting resource '" + resource + "'");
        URL resURL
                = ResourceManager.instance().getResourceUrl(resource);
        if (resURL == null) {
            LOGGER.error("Could not find resource '" + resource + "', throwing exception");
            throw new FileNotFoundException();
        }
        LOGGER.debug("Successfully extracted resource '" + resource + "'");
        return extractResource(resURL, prefix, suffix);
    }

    /**
     * Extracts a resource into a temporary file.
     *
     * @param resource A URL to the resource to extract.
     * @param prefix A prefix to use for the temporary file.
     * @param suffix A suffix to use for the temporary file.
     * @return Returns a file instance for the temporary file.
     * @throws IOException
     */
    public static File extractResource(URL resource, String prefix,
            String suffix) throws IOException {
        LOGGER.debug("Extracting resource '" + resource.toString() + "'");
        File file = File.createTempFile(prefix, suffix);

        InputStream resStream = null;
        FileOutputStream tmpFileStream = null;
        try {
            resStream = resource.openStream();
            tmpFileStream = new FileOutputStream(file);

            byte buffer[] = new byte[1024];
            int i = 0;
            while ((i = resStream.read(buffer)) != -1) {
                tmpFileStream.write(buffer, 0, i);
            }
            LOGGER.debug("Successfully extracted resource '" + resource.toString() + "'");
        } finally {
            if (resStream != null) {
                resStream.close();
            }
            if (tmpFileStream != null) {
                tmpFileStream.close();
            }
        }

        return file;
    }

    public static File copyFile(File source, File target) throws IOException {
        LOGGER.debug("Copying file '" + source.getPath() + "' to target '" + target.getPath() + "'");
        if (target.isDirectory()) {
            target = new File(target, source.getName());
        }

        if (source.equals(target)) {
            return target;
        }

        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(source);
            fos = new FileOutputStream(target);

            byte buffer[] = new byte[1024];
            int i = 0;
            while ((i = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, i);
            }
            LOGGER.debug("Successfully copied file '" + source.getPath() + "' to target '" + target.getPath() + "'");
        } finally {
            if (fis != null) {
                fis.close();
            }
            if (fos != null) {
                fos.close();
            }
        }

        return target;
    }

    /**
     * Read a whole file as-is into a string.
     *
     * @param file The file to read.
     * @return A string with the whole file contents.
     * @throws FileNotFoundException Thrown if the file doesn't exist.
     */
    public static String read(File file) throws FileNotFoundException {
        LOGGER.debug("Reading file '" + file.getName() + "' to string as is");
        String contents;
        try (Scanner scanner = new Scanner(file)) {
            scanner.useDelimiter("\\Z");
            contents = "";
            if (scanner.hasNext()) {
                contents = scanner.next();
            }
        }
        LOGGER.debug("Successfully read file '" + file.getName() + "' to string as is");
        return contents;
    }

    /**
     * Returns the file name extension of {@code file}, if available. If the
     * file ends on a dot ({@code .}), the empty string is returned. If no
     * extension exists, {@code null} is returned instead.
     *
     * @param file The file object.
     * @return A string with the file extension, or {@code null} if it doesn't
     * exist.
     */
    public static String getExtension(File file) {
        String filename = file.getName();
        int pos = filename.lastIndexOf('.');

        return pos == -1 ? null : filename.substring(pos + 1);
    }

    public static File getTempDir() {
        return new File(System.getProperty("java.io.tmpdir"));
    }
}
