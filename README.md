MPAnalyzer-nitro
==========

A multilingual edition of [MPAnalyzer](https://github.com/YoshikiHigo/MPAnalyzer) powered by [nitron](https://github.com/Durun/nitron)

## Requirements
- JDK >=11

## How to run
1. Prepare JAR file
    - Build manually as [How to build](#how-to-build)
    - Or [download JAR file here](https://github.com/Durun/MPAnalyzer/releases/tag/v0.1-SNAPSHOT)
    
1. Place the config files

   Copy [config directory](https://github.com/Durun/nitron/tree/master/config) into the same directory with `MPAnalyzer.jar`
   . As below
    ```
    WorkingDir/
    ├ config/
    └ MPAnalyzer.jar
    ```

1. Run the JAR file
    ```
    java -ea -Xmx8g -jar MPAnalyzer.jar [COMMAND] [OPTIONS]
    ```


## Command reference
### changes
- *Command:* `changes`
- *Options:*
    - `-gitrepo` The repository directory to analyze
    - `-db` Database file to save data
    - `-lang` Language of the source code
    - `-soft` Name of repository
    - `-thd` (optional: default=`1`) Number of threads to use
- *Flags:*
    - `-n` Normalizing variables and literals for mining 
- *Usage:* `changes -gitrepo your/repo -db your.repo.db -lang java -soft example -thd 8 -n`

### patterns
- *Command:* `patterns`
- *Options:*
    - `-db` Database file to save data
    - `-thd` (optional: default=`1`) Number of threads to use
- *Flags:*
    - `-a` Use all changes to make change patterns
- *Usage:* `patterns -db your.repo.db -a`

for detail:
- [CPAConfig.java](https://github.com/Durun/MPAnalyzer/blob/master/src/main/java/yoshikihigo/cpanalyzer/CPAConfig.java)
- [JarEntryPoint.java](https://github.com/Durun/MPAnalyzer/blob/master/src/main/java/yoshikihigo/cpanalyzer/JarEntryPoint.java)


## How to build
<a name="how-to-build"></a>
1. Build [nitron](https://github.com/Durun/nitron) to MavenLocal
    ```shell
    git clone --recursive https://github.com/Durun/nitron.git
    cd nitron
    ./gradlew publishToMavenLocal
    ```
1. Build MPAnalyzer
    ```shell
    git clone --recursive https://github.com/Durun/MPAnalyzer.git
    cd MPAnalyzer
    gradle shadowJar
    ```
