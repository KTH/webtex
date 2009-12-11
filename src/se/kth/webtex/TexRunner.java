package se.kth.webtex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;


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
	
	private static String DVI_COMMAND = "dvipng -depth -D %s -o %s %s";
	private static String TEX_COMMAND = "tex -fmt secplain -interaction nonstopmode --output-comment '' -output-directory %s %s";
	private static int[] RESOLUTIONS = {100, 119, 141, 168, 200, 238, 283, 336, 400, 476, 566};
	
	private String dir;
	private String texFormats;
	
	public TexRunner(String servletPath) {
		this.dir = servletPath + File.separator + "tmp" + File.separator + "tex";
		new File(dir).mkdirs();
		this.texFormats = servletPath + File.separator + "WEB-INF" + File.separator + "secsty";
	}
	
	public void create(String expression, int resolution, Cache cache) throws IOException {
		String fileName = temporaryFile();
		
		try {
			createTexFile(fileName, expression);
			String output = runTex(fileName);
			int depth = runDvi(fileName, resolution);

			cache.put(expression, resolution, depth, new File(fileName + IMAGE_SUFFIX), output);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			removeTemporaryFiles(fileName);
		}
	}	
	
	private String temporaryFile() throws IOException {
		File tmpFile = File.createTempFile("webtex", "", new File(dir));
		return tmpFile.getAbsolutePath();
	}
	
	private void removeTemporaryFiles(String nameWithoutSuffix) {
		File file = new File(nameWithoutSuffix + ".tex");
		file.delete();
		file = new File(nameWithoutSuffix + ".log");
		file.delete();
		file = new File(nameWithoutSuffix + ".dvi");
		file.delete();
		file = new File(nameWithoutSuffix);
		file.delete();
	}
	
	private void createTexFile(String fileName, String expression) throws IOException {
		PrintWriter texFile = new PrintWriter(new FileWriter(fileName + ".tex"));
		texFile.println("\\_par\\_secure_hbox");
		texFile.println("$" + expression + "$");
		texFile.println("\u0000");
		texFile.println("\\_end");
		texFile.close();
	}

	private String runTex(String fileName) throws IOException, InterruptedException {
		String output = null;
		String command = String.format(TEX_COMMAND, dir, fileName + ".tex");
		ProcessBuilder pb = new ProcessBuilder(Arrays.asList(command.split(" ")));
		Map<String, String> env = pb.environment();
		env.put("TEXFORMATS", texFormats + File.pathSeparator);
		Process tex = pb.start();

		tex.waitFor();
		if (tex.exitValue() != 0) {
			output = getErrorMessage(tex);
		}
		return output;
	}

	private String getErrorMessage(Process tex) throws IOException {
		BufferedReader texOutput = new BufferedReader(new InputStreamReader(tex.getInputStream()));
		String output = "";
		boolean errorMessage = false;
		String line;

		while ((line = texOutput.readLine()) != null) {
			if (line.matches("!.*")) {
				errorMessage = true;
			} else if (line.matches(".*see the transcript file for additional information.*")) {
				errorMessage = false;
			}
			if (errorMessage) {
				output += line + "\n";
			}
		}
		return output;
	}

	private int runDvi(String fileName, int resolution) throws IOException, InterruptedException {
		int depth = 0;
		String output;
		
		String command = String.format(DVI_COMMAND, 
				Integer.toString(RESOLUTIONS[resolution]), 
				fileName + IMAGE_SUFFIX,
				fileName + ".dvi");
		Process dvi = Runtime.getRuntime().exec(command);

		BufferedReader dviOutput = new BufferedReader(new InputStreamReader(dvi.getInputStream()));
		while ((output = dviOutput.readLine()) != null) {
			String[] split = output.split("=|]");
			if (split.length > 1) {
				depth = Integer.parseInt(split[1]);
			}
		}

		dvi.waitFor();
		return depth;
	}
}
