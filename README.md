# Java Multi-Module Project Template

Dieses Repository ist ein **Template** für ein Java/Kotlin-Multi‑Modul‑Projekt mit Gradle (Kotlin DSL).  
Es liefert eine klare Modulstruktur und einen schnellen Einstieg für neue Projekte.


---

## Voraussetzungen

Bitte stelle sicher, dass du Folgendes installiert hast:

- **Git**
- **JDK 21** (z.B. Temurin oder Oracle)
- **IntelliJ IDEA** (empfohlen) oder eine andere IDE

---

## Inhalt des Templates

**Modulübersicht (aus der Projektstruktur):**

- `common` – Gemeinsame Logik, die von anderen Modulen genutzt wird
- `paper` – Modul für Paper‑Server (Minecraft)
- `velocity` – Modul für Velocity‑Proxy
- `buildSrc` – geteilte Gradle‑Logik/Build‑Konventionen

---

## Neues Projekt aus dem Template konfigurieren

### 1) Template klonen oder als neues Repo verwenden

**Variante A – GitHub Template (empfohlen):**

1. Klicke bei dem Template auf **“Use this template”**
2. Erstelle dein neues Repository

**Variante B – manuell klonen:**

```bash
git clone https://github.com/Crystalixs-Network/Java-Multimodule-Template.git
```

## Projekt konfigurieren

Öffne `gradle.properties` und passe die Projekt‑Metadaten an:

- `name` → Plugin‑Name (z.B. `My Cool Plugin`)
- `version` → Version (z.B. `1.0.0`)
- `description` → kurze Beschreibung
- `authors` → Autor(en), getrennt durch Komma

**Beispiel:**

```properties
plugin-name = My Cool Plugin
version     = 1.0.0
description = A cool Paper plugin
authors     = DeinName, TeamName
``` 

## Package & Main‑Klasse anpassen

### 1) Package anpassen

Passe die Java‑Packages an dein Projekt an, z.B. `net.crystalixs.core.paper` bzw `net.crystalixs.core.velocity`

### 2) Main‑Klasse anpassen

Passe den Namen der Main-Klasse an dein Projekt an, z.B. `CorePlugin`. Wichtig ist, dass dieser Name einhtilich genutzt wird.
Er besteht immer aus `<plugin-name>Plugin`, wobei `<plugin-name>` der Wert aus `gradle.properties` ist.

---

## Wichtige Konfiguration (automatisch)

Diese Werte werden automatisch in die `plugin.yml` geschrieben:

- `main` → Hauptklasse (automatisch)
- `authors` → aus `gradle.properties`
- `apiVersion` → 1.21

Analog dazu wird automatisch die `velocity-plugin.json` erzeugt.

---

## Plugin testen

Dieses Template nutzt das Gradle-Plugin `runPaper` und `runVelocity`. Damit kannst du dein Plugin lokal testen, ohne einen eigenen Server aufsetzen zu müssen.
Um den mitgelieferten Server zu nutzen, führe folgenden Befehl aus:

```bash
./gradlew runServer
```

bzw.

```bash
./gradlew runVelocity
```

Nach dem erstmaligen Start musst du die EULA akzeptieren. Starte den Server danach neu.

---

## 🛠 Build & Plugin‑JAR erzeugen

```bash
 ./gradlew build
```

Das fertige Plugin findest du dann hier: `build/libs/<artifact>-<version>.jar`


