package net.martenscs.feature.p2.advice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * Goal which creates p2.inf file.
 * 
 * @goal touch
 * 
 * @phase process-sources
 */
@Mojo(name = "touch", threadSafe = true, requiresDependencyResolution = ResolutionScope.TEST)
public class P2AdviceMojo extends AbstractMojo {

	/**
	 * Location of the file.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;

	/**
	 * @parameter
	 * @required
	 */
	private List<Advice> p2inf;
	/**
	 * The maven project.
	 * 
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	public void execute() throws MojoExecutionException {

		Properties props = new Properties();
		Properties build_properties = new Properties();
		File parentDir = outputDirectory.getParentFile();
		File f = new File(parentDir, Advice.BUILD_PROPS);
		read(build_properties, f);

		int i = 0;
		for (Advice advice : p2inf) {
			advice.setUnit(i);
			advice.addRequires(props);
			advice.addUnits(props);
			i++;
		}
		if (project.getPackaging().equals(Advice.ECLIPSE_PLUGIN)) //$NON-NLS-1$
			parentDir = new File(parentDir.getAbsoluteFile() + Advice.METAINF); //$NON-NLS-1$
		else
			addToBuildProperty(build_properties);
		File touch = new File(parentDir, Advice.ADVICE_FILE);
		store(props, touch);
		store(build_properties, f);

	}

	protected void store(Properties properties, File file)
			throws MojoExecutionException {
		// Write the properties file back out
		FileOutputStream output = null;
		try {
			output = new FileOutputStream(file);
			properties.store(output, null);
		} catch (final IOException ioe) {
			final String message = Messages
					.getString("P2AdviceMojo.ERROR_MESS_WRITE") + file; //$NON-NLS-1$
			throw new MojoExecutionException(message);
		} finally {
			if (null != output) {
				try {
					output.close();
				} catch (final IOException ioe) {

				}
			}
		}
	}

	protected void addToBuildProperty(Properties properties) {
		if (properties.containsKey(Advice.BIN_INC)
				&& !properties.getProperty(Advice.BIN_INC).contains(
						Advice.ADVICE_FILE)) {
			String value = properties.getProperty(Advice.BIN_INC);
			properties.setProperty(Advice.BIN_INC, value + Advice.DELIM
					+ Advice.ADVICE_FILE); //$NON-NLS-1$
		}
	}

	protected void read(Properties properties, File file)
			throws MojoExecutionException {
		try {
			properties.load(new FileInputStream(file));
		} catch (IOException e) {
			final String message = Messages
					.getString("P2AdviceMojo.ERROR_MESS_READ") + file; //$NON-NLS-1$
			throw new MojoExecutionException(message);
		}
	}
}
