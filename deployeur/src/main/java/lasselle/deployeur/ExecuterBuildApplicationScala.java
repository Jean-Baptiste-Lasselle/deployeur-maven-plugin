package lasselle.deployeur;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
//                                     F‌ileRepositoryBuilder
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import lasselle.ssh.operations.elementaires.JiblExec;
import lasselle.ssh.operations.elementaires.JiblExecSansFin;

//import lasselle.ssh.operations.elementaires.JiblExec;
/**
 * Une recette de Build du code source de l'application Scala, à exécuter après avoir exécuté le goal [deployeur:provision-scala] cf. {@see MonterCibleDeploiementScala}
 * 
 * ********************************************************************************************************************************
 * Récaitualtif des paramètres:
 * 
 * ********************************************************************************************************************************
 * 
 * => @Parameter(alias = "repertoire-code-scala", property = "repertoire-code-scala", required = true, defaultValue = "scala")
 * => @Parameter(alias = "nom-conteneur-docker-srv-scala", property = "nom-conteneur-docker-srv-scala", required = false)
 * => @Parameter(alias = "ip-cible-srv-scala", property = "ip-cible-srv-scala", required = true)
 * => @Parameter(alias = "no-port-cible-srv-scala", property = "no-port-cible-srv-scala", required = true)
 * => @Parameter(alias = "ops-lx-user", property = "ops-lx-user", defaultValue = "lauriane", required = true)
 * => @Parameter(alias = "ops-lx-pwd", property = "ops-lx-pwd", defaultValue = "lauriane", required = true)
 * => @Parameter(alias = "url-repo-git-app-scala", property = "url-repo-git-app-scala", required = true, defaultValue = "https://github.com/Jean-Baptiste-Lasselle/siteweb-usinelogicielle.com")
 * => @Parameter(alias = "nom-repo-git-app-scala", property = "nom-repo-git-app-scala", defaultValue = "siteweb-usinelogicielle.com")
 * => @Parameter(alias = "url-repo-git-deploiements", property = "url-repo-git-deploiements", defaultValue = "https://github.com/Jean-Baptiste-Lasselle/deploiement-usine-logicielle.com")
 * => @Parameter(alias = "nom-repo-git-deploiements", property = "nom-repo-git-deploiements", defaultValue = "deploiement-usine-logicielle.com")
 * => @Parameter(alias = "ops-git-username", property = "ops-git-username", required = true)
 * => @Parameter(alias = "ops-git-userpwd", property = "ops-git-userpwd", required = true)
 * 
 * 
 * ********************************************************************************************************************************
 *  <repertoire-code-scala></repertoire-code-scala>
 *  <nom-conteneur-docker-srv-scala></nom-conteneur-docker-srv-scala>  (optionnel, pas de valeur par défaut, ainsi, si pointe vers null, alors cela signifie que le déplpoiement ne doit pas se faire dans un conteneur).
 *  <ip-cible-srv-scala></ip-cible-srv-scala>
 *  <no-port-cible-srv-scala></no-port-cible-srv-scala>
 *  <ops-lx-user></ops-lx-user>
 *  <ops-lx-pwd></ops-lx-pwd>
 *  <ops-git-username></ops-git-username>
 *  <ops-git-userpwd></ops-git-userpwd> n'existe plus
 *  <ops-scm-git-username></ops-scm-git-username>
 *  <ops-scm-git-username-pwd></ops-scm-git-pwd> n'existe plus
 *  <url-repo-git-app-scala></url-repo-git-app-scala>
 *  <nom-repo-git-app-scala></nom-repo-git-app-scala>
 *  <url-repo-git-deploiements></url-repo-git-deploiements>
 *  <nom-repo-git-deploiements></nom-repo-git-deploiements>
 *  
 * 
 * 
 * 
 * 
 * 
 * 
 * ********************************************************************************************************************************
 * 
 * 
 * @author Jean-Baptiste Lasselle
 *
 */
@Mojo(name = "build-scala-app")
public class ExecuterBuildApplicationScala extends AbstractMojo {

	/**
	 * ********************************************************************************************************************************
	 * Les paramètres du goal maven
	 * ********************************************************************************************************************************
	 */

	@Parameter(alias = "repertoire-code-scala", property = "repertoire-code-scala", required = true, defaultValue = "scala")
	String NOM_REP_BUILD_COURANTScala = null;

	/**
	 * Pas de valeur par défaut, ainsi, si pointe vers null, alors cela signifie que le déplpoiement ne doit pas se faire dans un conteneur. 
	 */
	@Parameter(alias = "nom-conteneur-docker-srv-scala", property = "nom-conteneur-docker-srv-scala", required = false)
	String nomConteneurDocker = null;
	@Parameter(alias = "ip-cible-srv-scala", property = "ip-cible-srv-scala", required = true)
	String adresseIPcibleDeploiement = null;
	@Parameter(alias = "no-port-cible-srv-scala", property = "no-port-cible-srv-scala", required = true)
	String numeroPortSrvScala = null;

	/**
	 * L'opérateur système qui va procéder aux opérations dans la cible de
	 * déploiement
	 */
	@Parameter(alias = "ops-lx-user", property = "ops-lx-user", defaultValue = "lauriane", required = true)
	String ops_lx_username = null;
	@Parameter(alias = "ops-lx-pwd", property = "ops-lx-pwd", defaultValue = "lauriane", required = true)
	String ops_lx_userpwd = null;

	/**
	 * Ce plugin permet de déployer une application scala dont le code source se
	 * trouve versioné par le repo de'URL
	 * {@see DeploiementScala#URL_REPO_CODE_SOURCE_APP_SCALA}
	 * 
	 * Le nom de ce repo est {@see DeploiementScala#NOM_REPO_CODE_SOURCE_APP_SCALA}
	 */
	@Parameter(alias = "url-repo-git-app-scala", property = "url-repo-git-app-scala", required = true, defaultValue = "https://github.com/Jean-Baptiste-Lasselle/siteweb-usinelogicielle.com")
	private String URL_REPO_CODE_SOURCE_APP_SCALA;
	@Parameter(alias = "nom-repo-git-app-scala", property = "nom-repo-git-app-scala", defaultValue = "siteweb-usinelogicielle.com")
	private String NOM_REPO_CODE_SOURCE_APP_SCALA; // lauriane-deploiement

	/**
	 * Ce plugin utilise un collaborateur: un repo GIT qui permet le transfert de
	 * l'artefact à déployer avec Github. Un repo Gitlab interne peut aussi être
	 * employé.
	 */
	@Parameter(alias = "url-repo-git-deploiements", property = "url-repo-git-deploiements", defaultValue = "https://github.com/Jean-Baptiste-Lasselle/deploiement-usine-logicielle.com")
	private String URL_REPO_GIT_ASSISTANT_DEPLOIEMENTS;
	/**
	 * TODO: Pourra être déprécié et on préfèrera l'utilsiation  de {@see DeploiementScala#repertoireAppScalaDsCible }
	 */
	@Parameter(alias = "nom-repo-git-deploiements", property = "nom-repo-git-deploiements", defaultValue = "deploiement-usine-logicielle.com")
	private String NOM_REPO_GIT_ASSISTANT_DEPLOIEMENTS; // lauriane-deploiement

	/**
	 * L'opérateur git qui va procéder aux opérations sur le repo (assistant) de deploiement
	 */
	@Parameter(alias = "ops-git-username", property = "ops-git-username", required = true)
	String ops_git_username = null;
	/**
	 * N'est pas un paramètre: il ne DOIT PAS figurer dans les pom.xml, au risque de
	 * versionner un mot de passe, tros gros risque de sécurité pour els utilisateurs.
	 */
//	@Parameter(alias = "ops-git-userpwd", property = "ops-git-userpwd", required = true)
	String ops_git_userpwd = null;
	
	/**
	 * L'opérateur git qui va procéder aux opérations sur le repo de versionning du code source de l'application Scala
	 */
	@Parameter(alias = "ops-scm-git-username", property = "ops-scm-git-username", required = true)
	String ops_scm_git_username = null;
	/**
	 * N'est pas un paramètre: il ne DOIT PAS figurer dans les pom.xml, au risque de
	 * versionner un mot de passe, tros gros risque de sécurité pour els utilisateurs.
	 */
//	@Parameter(alias = "ops-git-userpwd", property = "ops-git-userpwd", required = false)
	String ops_scm_git_userpwd = null;

	/**
	 * ********************************************************************************************************************************
	 * Quelques références vers des répertoires du projet lui-même, afin de pouvoir
	 * faire les opérations comme souhaité
	 * ********************************************************************************************************************************
	 */
	// @Parameter(readonly = true, .... defaultValue = "mavaleur" .... etc...)
	@Parameter(defaultValue = "${project.basedir}")
	private String cheminRacineProjet;
	@Parameter(defaultValue = "${project.basedir}/scala")
	private String repertoireScala = this.cheminRacineProjet + this.NOM_REP_BUILD_COURANTScala;

	
	@Parameter(defaultValue = "${project.build.directory}")
	private String cheminRepBuildMaven = null;

	/**
	 * Le répertoire dans lequel le code sclala est déployé dans la cible de déploiement
	 */
	@Parameter(alias = "repertoire-deploiement-scala", property = "repertoire-deploiement-scala", required = true)
	private String repertoireAppScalaDsCible;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" +++++++++++++++  BUILD APPLICATION SCALA	+++++++++++++++++++ ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" +++	         CHECK UP DES VALEURS PARAMETRES            +++");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" +++	VALEUR adresseIPcibleDeploiement: " + this.adresseIPcibleDeploiement + " ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" +++	VALEUR this.ops_lx_username: " + this.ops_lx_username + " ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" +++	VALEUR this.ops_lx_userpwd: " + this.ops_lx_userpwd + " ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		
		String URI_REPO_RECETTES = "https://github.com/Jean-Baptiste-Lasselle/lauriane";
		String NOM_REP_BUILD_COURANTOperations1 = "$HOME/builds-app-scala";
		String defintionENV = "export NOM_REP_BUILD_COURANT=$HOME/builds-app-scala/$(date +\"%d-%m-%Y-%HHeures%Mmin%SSec\") ";
		String commandeDeBuild = defintionENV + "&& rm -rf $NOM_REP_BUILD_COURANT && mkdir -p $NOM_REP_BUILD_COURANT && git clone " + URI_REPO_RECETTES  + " $NOM_REP_BUILD_COURANT && cd $NOM_REP_BUILD_COURANT && sbt package; ";
		JiblExec.executeCetteCommande(commandeDeBuild, adresseIPcibleDeploiement, this.ops_lx_username, this.ops_lx_userpwd);
		JiblExec.executeCetteCommande(" echo \"Petit Test Variables ENV avec JSch JIBL (les variables d'environnement ne devraient pas survivire à la fermeture de session SSH): [NOM_REP_BUILD_COURANT=$NOM_REP_BUILD_COURANT]\"", adresseIPcibleDeploiement, this.ops_lx_username, this.ops_lx_userpwd);
//		JiblExec.executeCetteCommande("git clone " + URI_REPO_RECETTES  + " $NOM_REP_BUILD_COURANT;" , adresseIPcibleDeploiement, this.ops_lx_username, this.ops_lx_userpwd);

//		JiblExec.executeCetteCommande(" cd $NOM_REP_BUILD_COURANT && sbt package; ", adresseIPcibleDeploiement, this.ops_lx_username, this.ops_lx_userpwd);


		
	}
	
}
