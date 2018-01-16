package lasselle.deployeur;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
//import org.apache.maven.model.Model;


@Mojo( name = "deploie")
public class JiblMojo1 extends AbstractMojo {
	
	/**
	 * LEs paramètres du goal maven
	 */

	@Parameter(alias = "ip-cible", property = "ip-cible", required = true)
	String adresseIPcibleDeploiement= null;
	@Parameter(alias = "lx-user", property = "lx-user",defaultValue = "lauriane")
	// aller chercher le username et le pwd
	String SSHusername = null;
	@Parameter(alias = "lx-pwd", property = "lx-pwd", defaultValue = "lauriane")
	String SSHuserpwd = null;
	
	/**
	 * Une référence vers le projet lui-même, afind e pouvoir faire mes opérations comme souhaité
	 */
//	@Parameter(readonly = true, defaultValue = "${project}")
//	private MavenProject project;
	@Parameter(defaultValue = "${project.build.directory}")
	private String cheminProjet;
	@Parameter(defaultValue = "${project.artifactId}-${project.version}.war")
	private String cheminFichierWAR;
	
	private File fichierWAR = new File(cheminProjet + "/" + cheminFichierWAR);
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
//		// aller chercher la valeur de l'adresse IP de la cible de déploiement
//		String adresseIPcibleDeploiement= null;
//		// La valeur est 
//		// aller chercher le username et le pwd
//		String SSHusername = null;
//		String SSHuserpwd = null;
		
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" +++++++++++++++++++	MON PLUGIN	+++++++++++++++++++ ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" +++	VALEUR adresseIPcibleDeploiement: " + this.adresseIPcibleDeploiement + " ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" +++	VALEUR SSHusername: " + this.SSHusername + " ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" +++	VALEUR SSHuserpwd: " + this.SSHuserpwd + " ");
		
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" +++	VALEUR cheminProjet: " + this.cheminProjet + " ");
		
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" +++	VALEUR cheminFichierWAR: " + this.cheminFichierWAR + " ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" +++	VALEUR cheminProjet + \"/\" + cheminFichierWAR: " + cheminProjet + "/" + cheminFichierWAR + " ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
				
	}

}
