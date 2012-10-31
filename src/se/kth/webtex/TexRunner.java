package se.kth.webtex;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;

import se.kth.sys.util.lang.SystemCommandHandler;

/**
 * Run TeX and DVIpng to generate the PNG images.
 */
public class TexRunner {
    /**
     * The maximum resolution to generate images for. The resolution is specified as an integer
     * 0-10, which is used to look up a certain image resolution to use with the dvipng command.
     */
    public static int MAX_RESOLUTION = 10;
    public static int MIN_RESOLUTION = 0;
    public static int DEFAULT_RESOLUTION = 1;

    /**
     * The image format suffix.
     */
    public static String IMAGE_SUFFIX = ".png";

    private static String DVI_COMMAND = "dvipng -T tight --depth -bg Transparent -D %s -o %s %s";
    private static String TEX_COMMAND = "latex -interaction nonstopmode -no-shell-escape -output-comment '' -output-directory %s %s";
    private static int[] RESOLUTIONS = {100, 119, 141, 168, 200, 238, 283, 336, 400, 476, 566};

    private File dir;

    public TexRunner(String servletPath) {
        this.dir = new File(servletPath + File.separator + "tmp" + File.separator + "tex");
        dir.mkdirs();
    }

    public void create(String expression, int resolution, Cache cache) throws IOException, ServletException {
        String fileName = temporaryFile();

        try {
            createTexFile(fileName, expression);
            String output = runTex(fileName);
            int depth = runDvi(fileName, resolution);

            cache.put(expression, resolution, depth, new File(fileName + IMAGE_SUFFIX), output);
        } catch (IOException e) {
            throw new ServletException("An error occuring when running 'tex'.", e);
        } catch (InterruptedException e) {
            throw new ServletException("An error occuring when running 'tex'.", e);
        } finally {
            removeTemporaryFiles(fileName);
        }
    }	

    private String temporaryFile() throws IOException {
        File tmpFile = File.createTempFile("webtex", "", dir);
        return tmpFile.getAbsolutePath();
    }

    private void removeTemporaryFiles(String nameWithoutSuffix) {
        File file = new File(nameWithoutSuffix + ".tex");
        file.delete();
        file = new File(nameWithoutSuffix + ".log");
        file.delete();
        file = new File(nameWithoutSuffix + ".aux");
        file.delete();
        file = new File(nameWithoutSuffix + ".dvi");
        file.delete();
        file = new File(nameWithoutSuffix);
        file.delete();
    }

    private void createTexFile(String fileName, String expression) throws IOException {
        PrintWriter texFile = new PrintWriter(new FileWriter(fileName + ".tex"));
        texFile.println("\\documentclass[fleqn]{minimal}");
        texFile.println("\\usepackage{mathtools}");
        texFile.println("\\usepackage[displaymath,active,textmath,tightpage]{preview}");
        texFile.println("\\begin{document}");
        texFile.println("\\(" + expression + "\\)");
        texFile.println("\\end{document}");
        texFile.close();
    }

    private String runTex(String fileName) throws IOException, InterruptedException {
        String output = null;
        String command = String.format(TEX_COMMAND, dir, fileName + ".tex");
        SystemCommandHandler tex = new SystemCommandHandler(command.split(" "));
        tex.setDirectory(dir);
        tex.enableStdOutStore();
        tex.executeAndWait();
        if (tex.getExitCode() != 0) {
            output = getErrorMessage(tex);
        }
        return output;
    }

    private String getErrorMessage(SystemCommandHandler tex) throws IOException {
        String output = null;
        boolean errorMessage = false;

        for (String line : tex.getStdOutStore()) {
            if (line.matches("!.*")) {
                output = line + " ";
                errorMessage = true;
            } else if (errorMessage) {
                output += line;
                errorMessage = false;
            }
        }
        return output;
    }

    private int runDvi(String fileName, int resolution) throws IOException, InterruptedException, ServletException {
        int depth = 0;

        String[] command = String.format(DVI_COMMAND, 
                Integer.toString(RESOLUTIONS[resolution]), 
                fileName + IMAGE_SUFFIX,
                fileName + ".dvi").split(" ");
        SystemCommandHandler dvi = new SystemCommandHandler(command);
        dvi.setDirectory(dir);
        dvi.enableStdOutStore();
        dvi.executeAndWait();
        if (dvi.getStdOutIOException() != null)
            throw new ServletException("An IO error occuring when running 'dvi'.", dvi.getStdOutIOException());
        if (dvi.getStdErrIOException() != null)
            throw new ServletException("An IO error occuring when running 'dvi'.", dvi.getStdErrIOException());
        if (dvi.getExitCode() != 0)
            throw new ServletException("Command 'dvi' failed during execution.");

        for (String output : dvi.getStdOutStore()) {
            String[] split = output.split("=|]");
            if (split.length > 1) {
                depth = Integer.parseInt(split[1]);
            }
        }

        return depth;
    }
}
