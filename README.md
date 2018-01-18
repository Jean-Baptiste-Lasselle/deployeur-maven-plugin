# deployeur-maven-plugin
un plugin maven pour déployer une appli web java jee dans une cible déploiement docker/tomcat

# Comment utiliser le plugin?

# # 1. construire une infrastruture cible de déploiement

Pour cela, il vous suffit d'utiliser:
https://github.com/Jean-Baptiste-Lasselle/lauriane
en suivant simplement les intructions https://github.com/Jean-Baptiste-Lasselle/lauriane/blob/master/ModeDemploi.pdf
Vous noterez les valeurs des paramètres suivants, (cf. ModeDemploi.pdf) :
adresse IP 

# # 3. Utiliser le plugin "deployeur-maven-plugin" pour déployer l'application web exemple

 - Avant d'exécuter le build maven de l'application web exemple, il
   nous faut donc le plugin "deployeur-maven-plugin" dans notre
   repo maven local. 
   Pour cela, nous allons cloner le code source du plugin, builder le plugin en l'installant dans le repo maven local:
   
              git clone https://github.com/Jean-Baptiste-Lasselle/deployeur-maven-plugin.git
              mvn clean install -up -U -f deployeur-maven-plugin/deployeur/pom.xml
      
 - Sur votre poste de dev., faîtes un git clone du repo contenant l'application web exemple à déployer:

              git clone https://github.com/Jean-Baptiste-Lasselle/lauriane-deployeur-test.git

 - Puis, avec Eclipse, ou votre IDE, importez le projet maven "Existing Maven Project...", pour créer un projet eclipse
dans votre IDE.

              Utilisateurs d'Eclipse: faîtes un "Maven Update".

      
 - Avant d'exécuter le build maven de l'application web exemple, il
   nous faut enfin éditer la configuration du plugin dans le fichier:
   
             ./lauriane-deployeur-test/jiblWebappTest/pom.xml
   
 - Exécutez ensuite, avec eclipse, le build maven de l'application web exxemple:

              mvn clean install -up -U -f ./lauriane-deployeur-test/jiblWebappTest/pom.xml






