package lasselle.deployeur;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import lasselle.ssh.operations.elementaires.JiblExec;
import lasselle.ssh.operations.elementaires.JiblScpTo;
//import org.apache.maven.model.Model;
import lasselle.ssh.operations.elementaires.JiblSftp;


@Mojo( name = "deploie")
public class JiblMojo1 extends AbstractMojo {
	
	/**
	 * LEs paramètres du goal maven
	 */
	@Parameter(alias = "nom-conteneur-docker", property = "nom-conteneur-docker", required = true)
	String nomConteneurDocker= null;
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
	private String nomFichierWAR;
	
	private File fichierWAR = new File(cheminProjet + "/" + nomFichierWAR);
	
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
		System.out.println(" +++	VALEUR cheminFichierWAR: " + this.nomFichierWAR + " ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" +++	VALEUR cheminProjet + \"/\" + cheminFichierWAR: " + cheminProjet + "/" + nomFichierWAR + " ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		
		JiblExec.executeCetteCommande("echo \"ah ok d'addaccord\">> voyons.voir ", adresseIPcibleDeploiement, SSHusername, SSHuserpwd);
		
		copierFihierWarVersCible();
		System.out.println(" +++	COPIE FAITE DANS CONTENEUR ");
		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		executeLeDeploiement();
		System.out.println(" +++	DEPLOIEMENT TERMINE DANS CONTENEUR ");
		
	}
	
	private void copierFihierWarVersCible() {
		JiblScpTo.faisCopie(this.cheminProjet + "/" + this.nomFichierWAR, this.nomFichierWAR, adresseIPcibleDeploiement, SSHusername, SSHuserpwd);
//		JiblSftp.faisCopie(this.cheminProjet + "/" + this.nomFichierWAR, this.nomFichierWAR, adresseIPcibleDeploiement, SSHusername, SSHuserpwd);
		
	}
	private void executeLeDeploiement() {
		
		JiblExec.executeCetteCommande("sudo docker cp "+ this.nomFichierWAR + " " + this.nomConteneurDocker + ":/usr/local/tomcat/webapps", adresseIPcibleDeploiement, SSHusername, SSHuserpwd);
		JiblExec.executeCetteCommande("sudo docker restart " + this.nomConteneurDocker, adresseIPcibleDeploiement, SSHusername, SSHuserpwd);
		
	}
	

}
