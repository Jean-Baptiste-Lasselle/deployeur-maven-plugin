package lasselle.deployeur;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import lasselle.ssh.operations.elementaires.JiblExec;
/**
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
 * ********************************************************************************************************************************
 * 
 * 
 * @author ezy
 *
 */
@Mojo(name = "deploie-app-scala")
public class DeploiementScala extends AbstractMojo {

	/**
	 * ********************************************************************************************************************************
	 * Les paramètres du goal maven
	 * ********************************************************************************************************************************
	 */

	@Parameter(alias = "repertoire-code-scala", property = "repertoire-code-scala", required = true, defaultValue = "scala")
	String nomRepertoireScala = null;

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
	 * Ce plugin permet de déployer uen application scala dont le code source se
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
	private String URL_REPO_GIT_ASSISTANT;
	@Parameter(alias = "nom-repo-git-deploiements", property = "nom-repo-git-deploiements", defaultValue = "deploiement-usine-logicielle.com")
	private String NOM_REPO_GIT_ASSISTANT; // lauriane-deploiement

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
	String ops_git_scm_userpwd = null;

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
	private String repertoireScala = this.cheminRacineProjet + this.nomRepertoireScala;

	
	@Parameter(defaultValue = "${project.build.directory}")
	private String cheminRepBuildMaven = null;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		this.demanderMotDePasseRepoGitCodeSource();
		this.demanderMotDePasseRepoGitAssistantDeploieemnts();
		
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" +++++++++++++++++++	DEPLIEMENT SCALA	+++++++++++++++++++ ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" +++	         CHECK UP DES VALEURS PARAMETRES            +++");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" +++	VALEUR adresseIPcibleDeploiement: " + this.adresseIPcibleDeploiement + " ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		
		
		/**
		 * 1. Initialiser le positionnement de version du code source de l'app. dans le répertoire {@see DeploiementScala#repertoireScala}
		 */
		this.initialiserCodeSource();

		/**
		 * 3. Le repo est maitenant initialisé, je fais le commit & push vers le repo de code source 
		 */
		this.faireCommitAndPushCodeSource();
		/**
		 * Je fais le commit and push vers le repo assistant du déploiement
		 *       
		 */
		this.faireCommitAndPushDeploiement();
		
		/**
		 * 5. Avec JSch je fais le git clone dans cible de déploiement, (et s'il faut je redemarre sbt,mais à priori non) 
		 */
		// je détruis, le repo, et le re-crrées par git clone.
		// PB DEPENDANCES A TRAITER ++++ >>> Donc compatible Ubuntu Uniquement SAUF SI JE FAIS UNE AUTRE DEPEDANCE UN AUTRE REPO QUI CONTIENT LA PROCEDURE DE DEPLOIEMENT DANS CIBLE DEPLOIEMNT
		JiblExec.executeCetteCommande("rm -rf ./"+ this.NOM_REPO_GIT_ASSISTANT, adresseIPcibleDeploiement, this.ops_git_username, this.ops_git_userpwd);
		// 
		JiblExec.executeCetteCommande("git clone \""+ this.URL_REPO_GIT_ASSISTANT + "\"", adresseIPcibleDeploiement, this.ops_git_username, this.ops_git_userpwd);
		/**
		 * 6. Je fais un petit affichage récapitulatif
		 * 
		 */
		this.afficherRecapitulatif();
		
	}
	/**
	 * Pour ne pas écrire de mot de passe dans la configuration du plugin
	 * TODO: évolution qui permet au développeur de gérer les credentials utilisés par le plugin, et d'intégrer cette gestion à des outils de gestions globaux système.
	 * @return 
	 * @throws MojoExecutionException lorsque le mot de passe saisi est null ou la chaîne de caractères vide
	 */
	private void demanderMotDePasseRepoGitAssistantDeploieemnts() throws MojoExecutionException {
//		if (ops_git_scm_userpwd != null) { // mais je ne veux PAS que 
//			
//		}
		this.ops_git_userpwd = this.demanderMotDePassePrRepoGit(this.ops_git_username, this.URL_REPO_GIT_ASSISTANT);
	}
	
	/**
	 * Pour ne pas écrire de mot de passe dans la configuration du plugin
	 * TODO: évolution qui permet au développeur de gérer les credentials utilisés par le plugin, et d'intégrer cette gestion à des outils de gestions globaux système.
	 * @return le mot de passe à utiliser 
	 * @throws MojoExecutionException lorsque le mot de passe saisi est null ou la chaîne de caractères vide
	 */
	private void demanderMotDePasseRepoGitCodeSource() throws MojoExecutionException {
		this.ops_git_scm_userpwd = this.demanderMotDePassePrRepoGit(this.ops_scm_git_username, this.URL_REPO_CODE_SOURCE_APP_SCALA);
	}
	
	private String demanderMotDePassePrRepoGit(String username, String URL_DU_REPO) throws MojoExecutionException {
		String motdepasse = null;
		
		motdepasse = JOptionPane.showInputDialog("Quel est le mot de passe de " + "[" + this.ops_git_username + "]" + " pousser sur le repo " + "[" + URL_DU_REPO + "]" + " ?",
				null);
		if (!(motdepasse != null && motdepasse.length() >= 1)) {
			StringBuilder message1 = new StringBuilder();
			String sautLigne = System.getProperty("line.separator");
			
			message1.append("Pour versionner (commit & push) le code source édité");
			message1.append(sautLigne);
			message1.append("dans le repository git " + "[" + URL_DU_REPO + "]");
			message1.append(sautLigne);
			message1.append(" L'utilisateur git: "+ "[" + username + "]" +") ");
			message1.append(sautLigne);
			message1.append(" a saisit un mot de passe null ou de longueur strictement inférieure à 1 ");
			message1.append(sautLigne);
			message1.append("La chaîne de caractère vide et null ne sont pas acceptés par le DEPLOYEUR-MAVEN-PLUGIN ");
			message1.append(sautLigne);
			message1.append("en tant que mot de passe pour une authentification.");
			message1.append(sautLigne);
			
			throw new MojoExecutionException(message1.toString());
		}
		
		
		
		return motdepasse;
		
	}

	/**
	 * 6. Je fais un petit affichage récapitulatif:
	 * 
	 * 			DEPLOIEMENT SCALA TERMINE
	 * 
	 * 			L'application scala est disponible à l'url: [http://etc...etc...]
	 * 
	 * 			Votre code source scala dans le répertoire {@see DeploiementScala#repertoireScala} a été poussé vers son repo de versionning: [this.urlRepoCodeSourceAppScala]
	 * 
	 * 
	 * 
	 */
	private void afficherRecapitulatif() {
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" ++++++++++++++++	DEPLOIEMENT SCALA TERMINE	+++++++++++++++ ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" +++	L'application scala est disponible à l'url: [http://" + this.adresseIPcibleDeploiement + ":" + this.numeroPortSrvScala + "/eventuellementautrechoseNomContexte] ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" +++    Votre code source scala dans le répertoire {@see DeploiementScala#repertoireScala} a été poussé vers son repo de versionning: [this.urlRepoCodeSourceAppScala] ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" +++    L'artefact déployé est la dernière versiond ela branche maester du repo : [" + this.URL_REPO_GIT_ASSISTANT + "] ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		
	}

	private void initialiserCodeSource() throws MojoExecutionException {
		
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" ++++++++++++++++	 INITIALISATION CODE SOURCE  ++++++++++++++ ");
		System.out.println(" ++++++++++++++++	 APPLICATION SCALA   		 ++++++++++++++ ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		/**
		 * 1. Vérifier si un code source versionné se trouve déjà dans le répertoire {@see DeploiementScala#repertoireScala}
		 */
		String sautLigne = System.getProperty("line.separator");
		try {
			this.verifierSiRepoGitPresent();
			// Si c'est le cas, il n'y a rien à faire.
			StringBuilder messageTerminalInitialisationCodeSource =  new StringBuilder();
			messageTerminalInitialisationCodeSource.append("DEPLOYEUR-MAVEN-PLUGIN");
			messageTerminalInitialisationCodeSource.append(sautLigne);
			System.out.println(messageTerminalInitialisationCodeSource);
			return;
		} catch (RepoCodeSourceAbsentException e) { // sinon, je l'initialise en détruisant le répertoire, 
			/** 
			 * 2. Si aucun repo git n'est trouvé, je l'intialise avec le repo de code source que je clone.
			 *    => Je demande à l'utilisateur de confirmer q'il veut que je chekout de tel repo de code source
			 *    => s'il réponds non, alors il a un affichge d'un messge qui propose à l'utilisateur de faire
			 *       le clone lui-même manuellement s'il le souhaite.
			 *    
			 */
			JOptionPane.showInputDialog("Enter username@hostname",
					System.getProperty("user.name") + "@localhost");
			

			
			StringBuilder messageTerminalNePasCloner =  new StringBuilder();
			messageTerminalNePasCloner.append("DEPLOYEUR-MAVEN-PLUGIN");
			messageTerminalNePasCloner.append(sautLigne);
			messageTerminalNePasCloner.append(" - Le répertoire " + "[" + this.repertoireScala + "]" + "");
			messageTerminalNePasCloner.append(sautLigne);
			messageTerminalNePasCloner.append("   ne contient pas de repository git valide.");
			messageTerminalNePasCloner.append(sautLigne);
			messageTerminalNePasCloner.append("   1./ Clonez le repository de versionnning de votre");
			messageTerminalNePasCloner.append(sautLigne);
			messageTerminalNePasCloner.append("   application manuellement dans" + "[" + this.repertoireScala + "]");
			messageTerminalNePasCloner.append(sautLigne);
			messageTerminalNePasCloner.append("   2./ indiquez l'URL du repo Git de votre application scala dans ");
			messageTerminalNePasCloner.append(sautLigne);
			messageTerminalNePasCloner.append("   votre pom.xml, dans la configuration d'exécution de votre plugin, avec");
			messageTerminalNePasCloner.append(sautLigne);
			messageTerminalNePasCloner.append("   la balise <repo-git-app-scala></repo-git-app-scala>  ");
			messageTerminalNePasCloner.append(sautLigne);
			messageTerminalNePasCloner.append("   3./ indiquez le nom  du repo Git de votre application scala dans ");
			messageTerminalNePasCloner.append(sautLigne);
			messageTerminalNePasCloner.append("   votre pom.xml, dans la configuration d'exécution de votre plugin, avec");
			messageTerminalNePasCloner.append(sautLigne);
			messageTerminalNePasCloner.append("   la balise <nom-repo-git-app-scala></nom-repo-git-app-scala>  ");
			messageTerminalNePasCloner.append(sautLigne);
			messageTerminalNePasCloner.append("DEPLOYEUR-MAVEN-PLUGIN");
			messageTerminalNePasCloner.append(sautLigne);
			
			
			StringBuilder confirmezGitClone = new StringBuilder();			
			confirmezGitClone.append(sautLigne);
			confirmezGitClone.append("Votre répertoire [] ne contient pas de repo git initialisé. ");
			confirmezGitClone.append(sautLigne);
			confirmezGitClone.append("[]");
			confirmezGitClone.append(sautLigne);
			confirmezGitClone.append("ne contient pas de repo git initialisé. ");
			confirmezGitClone.append(sautLigne);
			confirmezGitClone.append(sautLigne);
			
			confirmezGitClone.append("Votre pom.xml précise que le repo de code source de votre application scala est :");
			confirmezGitClone.append(sautLigne);
			confirmezGitClone.append("[]");
			confirmezGitClone.append(sautLigne);
			confirmezGitClone.append("Souhaitez-vous cloner le repo [] dans [], pour reprendre le développement de votre application? ");
			confirmezGitClone.append(sautLigne);
			confirmezGitClone.append("Si vous répondez oui, le répertoire  " + "[" + this.repertoireScala + "]" + " sera détruit");
			confirmezGitClone.append(sautLigne);
			confirmezGitClone.append("s'il existe, et re-créé, pour cloner la dernière version de al branche master de " + "[" + this.URL_REPO_CODE_SOURCE_APP_SCALA + "]");
			confirmezGitClone.append(sautLigne);
			
			
			int confirmationGitClone = JOptionPane.showConfirmDialog(null, confirmezGitClone.toString());
			switch (confirmationGitClone) {
				case JOptionPane.YES_OPTION: {
					// faire le git clone
					this.clonerRegpoCodeSourceAppliScala();
					break;
				}
				case JOptionPane.NO_OPTION: {
					// faire un affichage puis interrompe le traitement
//					String sautLigne = System.getProperty("line.spearator");

					
					JOptionPane.showMessageDialog(null, messageTerminalNePasCloner.toString());
					// J'interromps le build en lançant une exception
					/**
					 * throw a MojoExecutionException if the problem makes it impossible
					 * to continue with the build, and use the MojoFailureException otherwise.
					 */
					throw new MojoExecutionException(messageTerminalNePasCloner.toString());
//					return;
//					break;
				}
				case JOptionPane.CANCEL_OPTION: {
					// J'interromps le build en lançant une exception
					/**
					 * throw a MojoExecutionException if the problem makes it impossible
					 * to continue with the build, and use the MojoFailureException otherwise.
					 */
					throw new MojoExecutionException(messageTerminalNePasCloner.toString());
//					break;
				}
				default: {
					// J'interromps le build en lançant une exception
					/**
					 * throw a MojoExecutionException if the problem makes it impossible
					 * to continue with the build, and use the MojoFailureException otherwise.
					 */
					throw new MojoExecutionException(messageTerminalNePasCloner.toString());
//					break;
				}
			}
		}
		
	}
	/**
	 * Réalise le versionning de l'artefact de déploieent (le truc qui est déployé)
	 * dans le repo git assistant des déploieemnts
	 * 
	 * Avec JGit
	 * 4. Puis je copie dans le répertoire "${project.build.directory}/deploiement-scala/compagnon"
	 * 		Pour cela, je fais:
	 *      =>  le init du repo git déploiements app dans "${project.build.directory}/tempmvnautobuild"
	 *      =>  le git add de tous les fichiers dans "${project.build.directory}/tempmvnautobuild"
	 *          pour ce add de tous les fichiers, essayer:
	 *          Git monRepoGitAmoi = blabla...;
	 *          monRepoGitAmoi.add().addFilepattern(".").call();
	 *      =>  le git commit and push vers le repo git déploiements app
	 *      
	 * 
	 * @throws MojoExecutionException 
	 */
	private void faireCommitAndPushDeploiement() throws MojoExecutionException {
		String cheminRepoTemporaire = this.cheminRepBuildMaven + "/tempdeployeurscalawkdir/";
		File repertoireRepoTemporaire = new File(cheminRepoTemporaire);
		// je détruis repertoireRepoTemporaire, et le re-créée
		try {
			if (repertoireRepoTemporaire.exists()) {
				FileUtils.forceDelete(repertoireRepoTemporaire);
			}
		} catch (IOException e2) {
			
			System.out.println(" JIBL + pb au delete du répertoire du repo [" + cheminRepoTemporaire  + "]");
			e2.printStackTrace();
		}
		boolean AETECREE = repertoireRepoTemporaire.mkdirs();
		String msgINFOcreationDirRepo = "";
		if (AETECREE) {
			msgINFOcreationDirRepo = " JIBL + le Repertoire de repo a été créé ";
		} else {
			msgINFOcreationDirRepo = "JIBL + le Repertoire de repo N'A PAS été créé";
		}
		System.out.println(msgINFOcreationDirRepo );

		// Le repo Git assistant des déploiements
		Git repoGitDeploiementsAppliScala = null;
		// repCodeSrcScala
		// => GIT CLONE
//		String URLduREPO = this.URL_REPO_GIT_ASSISTANT;
		try {
			CloneCommand cloneCommand = Git.cloneRepository();
			cloneCommand.setDirectory(repertoireRepoTemporaire);
			cloneCommand.setURI(this.URL_REPO_GIT_ASSISTANT);
			cloneCommand.setCredentialsProvider(
					new UsernamePasswordCredentialsProvider(this.ops_git_username, this.ops_git_userpwd));
			repoGitDeploiementsAppliScala = cloneCommand.call();
			// monrepogit = Git.init().setDirectory(repoDIR).call();
		} catch (IllegalStateException e) {
			System.out.println(" ERREUR AU GIT INIT DANS  \"" + this.repertoireScala + "\" ");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			System.out.println(" ERREUR AU GIT INIT  DANS  \"" + this.repertoireScala + "\" ");
			// TODO Auto-generated catch bloc
			e.printStackTrace();
		}
		
		// => COPIE ARTEFACT DEPLOIEMENT DANS REPO LOCAL : je copie le
		//    source scala édité dans [this.repertoireScala], vers le
		//    répertoire  repertoireRepoTemporaire
		File aCopier = new File(this.repertoireScala + "/");
		
		try {
			boolean preserveFileDate = true;
			org.apache.commons.io.FileUtils.copyDirectory(aCopier, repertoireRepoTemporaire, preserveFileDate);
		} catch (IOException e) {
		    e.printStackTrace();
		}
		
		// => git pull
		
		// => GIT ADD : je fais le add de tous les fichiers
		try { /// pour ajouter tous les fichiers (soit disant)
			DirCache index = repoGitDeploiementsAppliScala.add().addFilepattern(".").call();
		} catch (GitAPIException e1) {
			throw new MojoExecutionException(" Problème à l'ajout (git add) des fichiers au versionning dans le repo local " + "[" + this.repertoireScala + "]" + ", avant de faire le commit and push du code source de l'application vers son repo de versionning de code source.", e1);
		}
		
		// => GIT COMMIT : je fais le commit
		try {
			StringBuilder messageDeCommit = new StringBuilder();
			messageDeCommit.append("Commit du  deployeur-maven-plugin, pour déploiement de l'application ");
			messageDeCommit.append("[" + this.URL_REPO_CODE_SOURCE_APP_SCALA + "]");
			messageDeCommit.append(" déploiement réalisé par l'utilisateur linux \" "+ this.ops_git_username + "\" dans la cible de déploiement.");
			RevCommit commit = repoGitDeploiementsAppliScala.commit().setMessage(messageDeCommit.toString() ).call();
		} catch (GitAPIException e1) {
			// TODO Auto-generated catch block
			throw new MojoExecutionException(" Problème au COMMIT dans le repo local "+ "[" + this.repertoireScala + "]", e1);
		}
		
		// => GUT PUSH :  je pousse vers le repo distant
		Iterable<PushResult> resultatsPush = null;
		try {
			resultatsPush = repoGitDeploiementsAppliScala.push().setCredentialsProvider( new UsernamePasswordCredentialsProvider( this.ops_git_username, this.ops_git_userpwd ) ).call();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			throw new MojoExecutionException(" Problème au PUSH du repo local " + "[" + this.repertoireScala + "]" + " vers " + "[" + this.URL_REPO_CODE_SOURCE_APP_SCALA + "]", e);
		}
		PushResult pushResult = resultatsPush.iterator().next();
		org.eclipse.jgit.transport.RemoteRefUpdate.Status status = pushResult.getRemoteUpdate( "refs/heads/master" ).getStatus();
		
		
		System.out.println(" +++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" +++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" +++++++++++++   RESULTAT DU PUSH vers : " + "[" + this.URL_REPO_GIT_ASSISTANT + "]" +" +++++++++++++ ");
		System.out.println(" +++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" +++++++++++++   code retour du PUSH : " + status.toString());
		System.out.println(" +++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" +++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" +++++++++++++++++++++++++++++++++++++++++++++++ ");
	}
	/**
	 * Réalise le commit and push du code source édité, veeers le repo de code source de l'application
	 * @throws MojoExecutionException
	 */
	private void faireCommitAndPushCodeSource() throws MojoExecutionException {
		File repertoireDeTravail = new File(this.repertoireScala);
		Git repoCodeSrcAppScala = null;
		// Je fais le git init, s'il un repo est déjà intiailisé et que nosu sommes en
		// train de travailler, alors aucune modification n'est apportée au repo local git
		// Simplement fait pour récupérer une référence Java sur le repository local
		try {
			repoCodeSrcAppScala = Git.init().setDirectory(repertoireDeTravail).call();
		} catch (IllegalStateException e2) {
//			e2.printStackTrace();
			throw new MojoExecutionException(" Problème à l'intiialisation du repo local " + "[" + this.repertoireScala + "]" +  ", avant de faire le commit and push du code source de l'application vers son repo de versionning de code source.", e2);
		} catch (GitAPIException e2) {
//			e2.printStackTrace();
			throw new MojoExecutionException(" Problème à l'intiialisation du repo local "+ "[" + this.repertoireScala + "]" + ", avant de faire le commit and push du code source de l'application vers son repo de versionning de code source.", e2);
		}
		// => je fais le add du fichier war
		try { /// pour ajouter tous les fichiers (soit disant)
			DirCache index = repoCodeSrcAppScala.add().addFilepattern(".").call();
		} catch (GitAPIException e1) {
			throw new MojoExecutionException(" Problème à l'ajout (git add) des fichiers au versionning dans le repo local "+ "[" + this.repertoireScala + "]" + ", avant de faire le commit and push du code source de l'application vers son repo de versionning de code source.", e1);
		}
		// => je fais le commit

//		monrepogit.remoteSetUrl(URLduREPO);
//		monrepogit.remoteSetUrl()
		try {
			StringBuilder messageDeCommit = new StringBuilder();
			messageDeCommit.append("Commit du  deployeur-maven-plugin, pour déploiement de l'application ");
			messageDeCommit.append("[" + this.URL_REPO_CODE_SOURCE_APP_SCALA + "]");
			messageDeCommit.append(" déploiement réalisé par l'utilisateur linux \" "+ this.ops_git_username + "\" dans la cible de déploiement.");
			RevCommit commit = repoCodeSrcAppScala.commit().setMessage(messageDeCommit.toString() ).call();
		} catch (GitAPIException e1) {
			// TODO Auto-generated catch block
			throw new MojoExecutionException(" Problème au COMMIT dans le repo local "+ "[" + this.repertoireScala + "]", e1);
		}
		// => je clone le repo git "https://github.com/Jean-Baptiste-Lasselle/lauriane-deploiement.git", dans la VM
		
		
		Iterable<PushResult> resultatsPush = null;
		try {
			resultatsPush = repoCodeSrcAppScala.push().setCredentialsProvider( new UsernamePasswordCredentialsProvider( this.ops_git_username, this.ops_git_userpwd ) ).call();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			throw new MojoExecutionException(" Problème au PUSH du repo local " + "[" + this.repertoireScala + "]" + " vers " + "[" + this.URL_REPO_CODE_SOURCE_APP_SCALA + "]", e);
		}
		PushResult pushResult = resultatsPush.iterator().next();
		org.eclipse.jgit.transport.RemoteRefUpdate.Status status = pushResult.getRemoteUpdate( "refs/heads/master" ).getStatus();
		
		
		System.out.println(" +++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" +++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" +++++++++++++   RESULTAT DU PUSH vers : " + "[" + this.URL_REPO_CODE_SOURCE_APP_SCALA + "]" +" +++++++++++++ ");
		System.out.println(" +++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" +++++++++++++   code retour du PUSH : " + status.toString());
		System.out.println(" +++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" +++++++++++++++++++++++++++++++++++++++++++++++ ");
		System.out.println(" +++++++++++++++++++++++++++++++++++++++++++++++ ");
	}

	/**
	 * TODO: rterminer l'inmplémentation, celle là doit juste faire le clone
	 */
	private void clonerRegpoCodeSourceAppliScala() {
		// --
		File repCodeSrcScala = new File(this.repertoireScala);
//		String cheminRepo = this.cheminRepoGitLocalDeTravail;
//		File repoDIR = new File(cheminRepo);
		// je le détruis, et le re-créée
		try {
			if (repCodeSrcScala.exists()) {
				FileUtils.forceDelete(repCodeSrcScala);
			}
		} catch (IOException e2) {
			System.out.println(" JIBL + pb au delete initla du répertoire du repo [" + this.repertoireScala + "]");
			e2.printStackTrace();
		}
		boolean AETECREE = repCodeSrcScala.mkdirs();
		String msgINFOcreationDirRepo = "";
		if (AETECREE) {
			msgINFOcreationDirRepo = " JIBL + le Repertoire de repo a été créé ";
		} else {
			msgINFOcreationDirRepo = "JIBL + le Repertoire de repo N'A PAS été créé";
		}

		System.out.println(msgINFOcreationDirRepo);
		// --
		
		
		
		// LE REPO
		Git repoGitAppliScala = null;
//		repCodeSrcScala
		// GIT INIT // NON, UN GIT CLONE AU DEPART
		String URLduREPO = this.URL_REPO_CODE_SOURCE_APP_SCALA;
		try {
			CloneCommand cloneCommand = Git.cloneRepository();
			cloneCommand.setDirectory(repCodeSrcScala);
			cloneCommand.setURI(URLduREPO);
			cloneCommand.setCredentialsProvider(
					new UsernamePasswordCredentialsProvider(this.ops_git_username, this.ops_git_userpwd));
			repoGitAppliScala = cloneCommand.call();
			// monrepogit = Git.init().setDirectory(repoDIR).call();
		} catch (IllegalStateException e) {
			System.out.println(" ERREUR AU GIT INIT DANS  \"" + this.repertoireScala + "\" ");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			System.out.println(" ERREUR AU GIT INIT  DANS  \"" + this.repertoireScala + "\" ");
			// TODO Auto-generated catch bloc
			e.printStackTrace();
		}

		
	}

	/**
	 * ----------------------------------------------------------------------------------
	 * Classes d'Exceptions utilisées pour l'initilisatgiond u code source à éditer.
	 * ----------------------------------------------------------------------------------
	 *
	 */
	
	
	private static class RepoAbsentException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2366627317074190637L;

		public RepoAbsentException() {
			super();
			// TODO Auto-generated constructor stub
		}

		public RepoAbsentException(String message, Throwable cause, boolean enableSuppression,
				boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
			// TODO Auto-generated constructor stub
		}

		public RepoAbsentException(String message, Throwable cause) {
			super(message, cause);
			// TODO Auto-generated constructor stub
		}

		public RepoAbsentException(String message) {
			super(message);
			// TODO Auto-generated constructor stub
		}

		public RepoAbsentException(Throwable cause) {
			super(cause);
			// TODO Auto-generated constructor stub
		}

	}

	/**
	 * Levée dans le cas où mon plugin ne trouve pas le repo de déploiement
	 * compagnon
	 * 
	 * @author Jean-Baptiste Lasselle
	 *
	 */
	private static class RepoDeploiementAbsentException extends RepoAbsentException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -2248874288255295215L;
		private static String MESSAGE = "Le repo de déploiement n'est pas précisé dans la configuration d'exécution du goal <goal>deploie-app-scala</goal> dans votre pom.xml";

		public RepoDeploiementAbsentException() {
			super();
			// TODO Auto-generated constructor stub
		}

		public RepoDeploiementAbsentException(String message, Throwable cause, boolean enableSuppression,
				boolean writableStackTrace) {
			super(MESSAGE, cause, enableSuppression, writableStackTrace);
			// TODO Auto-generated constructor stub
		}

		public RepoDeploiementAbsentException(String message, Throwable cause) {
			super(MESSAGE, cause);
			// TODO Auto-generated constructor stub
		}

		public RepoDeploiementAbsentException(String message) {
			super(MESSAGE);
			// TODO Auto-generated constructor stub
		}

		public RepoDeploiementAbsentException(Throwable cause) {
			super(cause);
			// TODO Auto-generated constructor stub
		}

	}

	/**
	 * Levée dans le cas où mon plugin ne trouve pas le repo de code source scala
	 * dans {@see DeploiementScala#repertoireScala}
	 * 
	 * @author Jean-Baptiste Lasselle
	 *
	 */
	public static class RepoCodeSourceAbsentException extends RepoAbsentException {

		private static String MESSAGE = "Le repo de code source Scala n'a pas été touvé dans le répertoire que vous avez précisé dans votre pom.xml, pour configurer le DEPLOYEUR plugin dans la balise <repertoire-code-scala></repertoire-code-scala>.";

		public RepoCodeSourceAbsentException() {
			super();
			// TODO Auto-generated constructor stub
		}

		public RepoCodeSourceAbsentException(String message, Throwable cause, boolean enableSuppression,
				boolean writableStackTrace) {
			super(MESSAGE, cause, enableSuppression, writableStackTrace);
			// TODO Auto-generated constructor stub
		}

		public RepoCodeSourceAbsentException(String message, Throwable cause) {
			super(MESSAGE, cause);
			// TODO Auto-generated constructor stub
		}

		public RepoCodeSourceAbsentException(String message) {
			super(MESSAGE);
			// TODO Auto-generated constructor stub
		}

		public RepoCodeSourceAbsentException(Throwable cause) {
			super(cause);
			// TODO Auto-generated constructor stub
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = -1263975271738160187L;

	}

	/**
	 * Perrmet de vérifer que le repo git du code source de l'application scala est
	 * bien intialisé dans le répertoire {@see DeploiementScala#repertoireScala}
	 */
	private void verifierSiRepoGitPresent() throws RepoCodeSourceAbsentException {
		// si le répertoire ne contient pas de répertoire ".git", alors exception
		File repertoirePointGit = new File(this.repertoireScala + "/.git/");
		if (!(repertoirePointGit.exists() && repertoirePointGit.isDirectory())) {
			throw new RepoCodeSourceAbsentException();
		}

		/**
		 * si la commande "git status" renvoie :
		 * 
		 * fatal: Not a git repository (or any of the parent directories): .git
		 * 
		 * alors le répertoire ne contient pas de répertoire ".git", alors exception
		 */

		//

	}


}
