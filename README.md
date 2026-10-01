# Delivery System — Course Project

Modular monolith built with **Java 25**, **Spring Boot 4.1**, **JavaFX 25**, **Maven** and **MySQL**.

This repository is the starting point for the *Desarrollo de Software 2* course project. It contains:

- `core-module` — shared kernel (domain building blocks, persistence base, JavaFX shell)
- `user-module` — reference implementation of a complete CRUD module
- `catalog-module`, `order-module`, `delivery-module`, `payment-module` — modules developed by four student teams
- `delivery-system` — desktop application that assembles all modules

## Linux (Ubuntu) setup

The project requires **JDK 25** (both `java` and `javac`). Installing only the JRE is not enough, because Maven needs `javac` to compile.

### 1. Install OpenJDK 25

```bash
sudo apt update
sudo apt install openjdk-25-jdk
```

### 2. Make Java 25 the default version

If you have other JDKs installed (e.g. 21 or 27), select the `java-25-openjdk-amd64` entry for both tools:

```bash
sudo update-alternatives --config java
sudo update-alternatives --config javac
```

Or set them directly without the interactive menu:

```bash
sudo update-alternatives --set java  /usr/lib/jvm/java-25-openjdk-amd64/bin/java
sudo update-alternatives --set javac /usr/lib/jvm/java-25-openjdk-amd64/bin/javac
```

### 3. Configure `JAVA_HOME`

The Maven wrapper uses `JAVA_HOME`. Add it to `~/.bashrc` so it is loaded in every terminal (run this only once to avoid duplicate lines):

```bash
echo 'export JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64' >> ~/.bashrc
echo 'export PATH="$JAVA_HOME/bin:$PATH"' >> ~/.bashrc
source ~/.bashrc
```

### 4. Verify the installation

```bash
java --version
javac --version
echo $JAVA_HOME
```

`java` and `javac` must both report version `25.x`, and `JAVA_HOME` must print `/usr/lib/jvm/java-25-openjdk-amd64`.

### 5. Give execute permission to the Maven wrapper

After cloning, `mvnw` may not be executable (`Permission denied`). From the repository root, run:

```bash
chmod +x core-module/mvnw user-module/mvnw catalog-module/mvnw order-module/mvnw \
         delivery-module/mvnw payment-module/mvnw delivery-system/mvnw
```

Or, from inside each module folder:

```bash
chmod +x mvnw
```

## Quick start

```bash
# 1. Install the shared kernel and reference module
cd core-module && ./mvnw install -DskipTests
cd ../user-module && ./mvnw install -DskipTests

# 2. Install the team modules (placeholders for now)
cd ../catalog-module && ./mvnw install -DskipTests
cd ../order-module && ./mvnw install -DskipTests
cd ../delivery-module && ./mvnw install -DskipTests
cd ../payment-module && ./mvnw install -DskipTests

# 3. Run the desktop application in memory (no database needed)
cd ../delivery-system
./mvnw javafx:run -Pmemory
```

> On Windows use `mvnw.cmd` if Git Bash is not available.

## Windows users

If you are on Windows and the wrapper fails with:

```
'powershell' is not recognized as an internal or external command
Cannot start maven from wrapper
```

Add PowerShell to your terminal session before running `mvnw.cmd`:

```cmd
set PATH=C:\Windows\System32\WindowsPowerShell\v1.0;%PATH%
```

Or, if you are using PowerShell:

```powershell
$env:PATH = "C:\Windows\System32\WindowsPowerShell\v1.0;" + $env:PATH
```

Then use `mvnw.cmd` instead of `./mvnw`:

```cmd
cd core-module && mvnw.cmd install -DskipTests
cd ..\user-module && mvnw.cmd install -DskipTests
cd ..\catalog-module && mvnw.cmd install -DskipTests
cd ..\order-module && mvnw.cmd install -DskipTests
cd ..\delivery-module && mvnw.cmd install -DskipTests
cd ..\payment-module && mvnw.cmd install -DskipTests

cd ..\delivery-system
mvnw.cmd javafx:run -Pmemory
```

## Full documentation

See [`delivery-system/README.md`](delivery-system/README.md) for:

- Team assignment and module scope
- Prerequisites and installation instructions
- How to run with MySQL
- Architecture and conventions every team must follow
- GitLab CI configuration
