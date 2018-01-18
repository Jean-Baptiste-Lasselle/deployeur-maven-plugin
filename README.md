# deployeur-maven-plugin
un plugin maven pour déployer une appli web java jee dans une cible déploiement docker/tomcat

## Comment utiliser le plugin?

# 1. construire une infrastruture cible de déploiement

Pour cela, il vous suffit d'utiliser:
https://github.com/Jean-Baptiste-Lasselle/lauriane
en suivant simplement les intructions https://github.com/Jean-Baptiste-Lasselle/lauriane/blob/master/ModeDemploi.pdf
Vous noterez les valeurs des paramètres suivants, (cf. ModeDemploi.pdf, et "monter-cible-deploiement.sh") :

* nom-conteneur-docker-srv-jee
* ip-cible-srv-jee
* no-port-cible-srv-jee
* et bien d'autres paramètres qui seront à utilsier pour la configuration du deployeur-maven-plugin


# 2. Utiliser le plugin "deployeur-maven-plugin" pour déployer l'application web exemple

 - Avant d'exécuter le build maven de l'application web exemple, il
   nous faut donc le plugin "deployeur-maven-plugin" dans notre
   repo maven local. 
   Pour cela, nous allons cloner le code source du plugin, builder le plugin en l'installant dans le repo maven local:
   
              git clone https://github.com/Jean-Baptiste-Lasselle/deployeur-maven-plugin.git
              mvn clean install -up -U -f deployeur-maven-plugin/deployeur/pom.xml
   
   Il faut enfin savoir que ce plugin, pour réaliser les déploiements d'applications web jee, utilise deux éléments:
    * un repo git de "déploiement": dans ce repo, on versionne simplement le fichier \*.war à déployer
    * un répertoire local à la machine exécutant l'IDE, ce répertoire ne devant pas être utilisé par votre IDE, il sera utilisé comme un repo lolcal git par le plugin maven, afin de faire des commit &&  push des wars, à chaque build.
      
   
 - Sur votre poste de dev., faîtes un git clone du repo contenant l'application web exemple à déployer:

              git clone https://github.com/Jean-Baptiste-Lasselle/lauriane-deployeur-test.git

 - Puis, avec Eclipse, ou votre IDE, importez le projet maven "Existing Maven Project...", pour créer un projet eclipse
dans votre IDE.

              Utilisateurs d'Eclipse: faîtes un "Maven Update".

      
 - Avant d'exécuter le build maven de l'application web exemple, il
   nous faut enfin éditer la configuration du plugin dans le fichier:
   
             ./lauriane-deployeur-test/jiblWebappTest/pom.xml
   
 - Exécutez ensuite, avec eclipse, le build maven de l'application web exemple:

              mvn clean install -up -U -f ./lauriane-deployeur-test/jiblWebappTest/pom.xml

Dans ce fichier, la configuration typique contient les éléments suivants:

   <plugin>
	<groupId>lasselle</groupId>
	<artifactId>deployeur</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<executions>
		<execution>
			<phase>install</phase>
			<goals>
				<goal>deploie</goal>
			</goals>
			<configuration>
			
				<!-- topologie de la cible de déploiement -->
				
				<!--  COMPOSANT SRV JEE -->
				<!-- Cette configuration permet de définir quel est le 
				     serveur jee dans lequel déployer le war buildé.
				-->
				<nom-conteneur-docker-srv-jee>ciblededeploiement-composant-srv-jee</nom-conteneur-docker-srv-e>
				<ip-cible-srv-jee>192.168.1.149</ip-cible-srv-jee>
				<no-port-cible-srv-jee>12546</no-port-cible-srv-jee>
	
				<!--  COMPOSANT SGBDR -->
    
				<!-- Cette configuration pourrait être utilisée pour mettre la BDD dans un état particulier, 					     souhaité pour le déploiement puis laisser un script permettant de faire revenir la BDD 
				     dans son état initial, après la fin des tests.
				-->
				<!-- Si le script de retour à l'état initial n'existe pas, il est créé en
				     utilisant mysqldump -->
				<!-- Si le script de retour à l'état initial existe, il est exécuté pour retour à
				     l'état initial avant déploiement -->
				<!-- <nom-conteneur-docker-sgbdr>ciblededeploiement-composant-sgbdr</nom-conteneur-docker-sgbdr> -->
				<!-- <ip-cible-sgbdr>192.168.1.149</ip-cible-sgbdr> -->
				<!-- <no-port-cible-sgbdr>4466</no-port-cible-sgbdr> -->
				<!-- Utilisateur Linux opérateur du plugin -->
				<lx-user>lauriane</lx-user>
				<lx-pwd>lauriane</lx-pwd>
				<!-- repo git assistant du plugin -->
				<url-repo-git-deploiements>https://github.com/Jean-Baptiste-Lasselle/lauriane-deploiement.git</url-repo-git-deploiements>
				<nom-repo-git-deploiements>lauriane-deploiement</nom-repo-git-deploiements>
				<git-username>Jean-Baptiste-Lasselle</git-username>
				<git-userpwd>***************</git-userpwd>
				<!-- 
				vous devez choisir un répertoire qui pourra être librement utilisé par le plugin maven
				 -->
				<repertoire-repo-git-local>C:\moi\mes_repos_git\tempmvnautobuild2</repertoire-repo-git-local>
						
			</configuration>
	</execution>
    </executions>
