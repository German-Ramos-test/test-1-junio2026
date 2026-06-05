# Documentación CI/CD — mini-juego-2d

Guía para desarrolladores DevOps que se incorporan al proyecto. Explica qué hace cada pipeline, cuándo se ejecuta y qué ocurre en cada paso.

---

## Visión general

El proyecto tiene **dos pipelines** gestionados con GitHub Actions:

| Pipeline | Archivo | Cuándo se dispara | Objetivo |
|----------|---------|-------------------|----------|
| **Java CI** | `.github/workflows/ci.yml` | Cualquier push o pull request | Compilar, testear y validar calidad |
| **Release** | `.github/workflows/release.yml` | Push de un tag `v*` o ejecución manual | Generar el ejecutable Windows y publicar el release |

---

## Pipeline 1: Java CI

**Archivo:** [`.github/workflows/ci.yml`](.github/workflows/ci.yml)

### Cuándo se ejecuta

```yaml
on:
  push:
    branches: ["**"]   # cualquier rama
  pull_request:        # cualquier PR abierto
```

Se ejecuta en **todo push** (independientemente de la rama) y en todo pull request. Es la barrera de calidad que impide que código roto llegue a `main`.

### Runner

```yaml
runs-on: ubuntu-latest
```

Corre sobre una máquina virtual Linux efímera proporcionada por GitHub. Se destruye al acabar el job.

### Diagrama de flujo

```
push / PR
    │
    ▼
┌─────────────────────┐
│ 1. Checkout          │  clona el repositorio
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ 2. Set up JDK 21    │  descarga e instala Temurin 21
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ 3. Verify quality   │  mvn -B verify
│    gates            │  (compila → tests → JaCoCo → Checkstyle)
└──────────┬──────────┘
           │
    ┌──────┴──────┐
    │ OK          │ FAIL
    ▼             ▼
  ✅ verde     ❌ rojo — bloquea el merge
```

### Steps en detalle

#### Step 1 — Checkout

```yaml
- name: Checkout
  uses: actions/checkout@v4
```

Descarga el código del commit que disparó el workflow en el runner. Sin este paso, no habría nada que compilar.

---

#### Step 2 — Set up JDK 21

```yaml
- name: Set up JDK 21
  uses: actions/setup-java@v4
  with:
    distribution: temurin   # distribución Eclipse Temurin (OpenJDK)
    java-version: 21        # versión LTS actual del proyecto
    cache: maven            # cachea ~/.m2 entre ejecuciones para acelerar builds
```

- Descarga e instala JDK 21 Temurin en el runner.
- Configura `JAVA_HOME` y el `PATH` automáticamente.
- La opción `cache: maven` guarda la carpeta `~/.m2/repository` entre runs del mismo workflow; los artefactos ya descargados no se vuelven a descargar, reduciendo el tiempo de build varios minutos.

---

#### Step 3 — Verify quality gates

```yaml
- name: Verify quality gates
  run: mvn -B verify
```

`mvn -B verify` ejecuta la fase `verify` del ciclo de vida Maven en modo **batch** (sin color ni interacción). Esta fase incluye, en orden:

| Fase Maven | Qué hace |
|------------|----------|
| `compile` | Compila `src/main/java` con Java 21 |
| `test-compile` | Compila `src/test/java` |
| `test` | Lanza los tests con **maven-surefire-plugin** (JUnit Jupiter) |
| `package` | Genera el JAR en `target/` |
| `verify` | Ejecuta los quality gates configurados en `pom.xml` |

Los quality gates que corren en la fase `verify`:

**JaCoCo — cobertura de código**
- Plugin: `jacoco-maven-plugin` v0.8.12
- Clases evaluadas: `GameEngine` y `GameLogic` (la UI `App` está excluida intencionalmente)
- Umbral mínimo: **70% de líneas cubiertas**
- Si la cobertura cae por debajo del 70%, el build falla con `Rule violated`

**Checkstyle — estilo de código**
- Plugin: `maven-checkstyle-plugin` v3.3.1
- Reglas definidas en [`checkstyle.xml`](checkstyle.xml):
  - Sin tabuladores (usar espacios)
  - Longitud máxima de línea: 140 caracteres
  - Llaves obligatorias en todos los bloques (`NeedBraces`)
  - Sin imports con wildcard (`AvoidStarImport`)
  - Sin bloques vacíos (`EmptyBlock`)
- Se aplica tanto al código de producción como al de test
- Si alguna clase viola una regla, el build falla indicando el archivo y la línea

> **Resultado esperado:** `BUILD SUCCESS`. Si el step falla, el check del PR queda en rojo y no se puede hacer merge hasta corregirlo.

---

## Pipeline 2: Release

**Archivo:** [`.github/workflows/release.yml`](.github/workflows/release.yml)

### Cuándo se ejecuta

```yaml
on:
  push:
    tags:
      - "v*"          # ej: v1.0.0, v0.2.1
  workflow_dispatch:  # también se puede lanzar manualmente desde la UI de GitHub
```

El flujo habitual es:
```
git tag v1.0.0
git push origin v1.0.0
        │
        └─► dispara el workflow Release
```

### Runner

```yaml
runs-on: windows-latest
```

Corre sobre Windows porque el script de empaquetado usa **`jpackage`** para generar un ejecutable nativo `.exe` para Windows. Un runner Linux no puede generar binarios PE.

### Permisos

```yaml
permissions:
  contents: write
```

El job necesita permiso de escritura sobre el contenido del repositorio para poder **crear o actualizar releases** en GitHub.

### Diagrama de flujo

```
push tag v*  /  ejecución manual
        │
        ▼
┌──────────────────────────┐
│ 1. Checkout               │  clona el repo en Windows
└───────────┬──────────────┘
            │
            ▼
┌──────────────────────────┐
│ 2. Set up JDK 21          │  igual que en CI
└───────────┬──────────────┘
            │
            ▼
┌──────────────────────────┐
│ 3. Run verify and         │  ./scripts/package-win.ps1
│    package app image      │  → mvn verify + jpackage
└───────────┬──────────────┘
            │
            ▼
┌──────────────────────────┐
│ 4. Archive app image      │  crea MiniJuego2D-<tag>-win64.zip
└───────────┬──────────────┘
            │
            ▼
┌──────────────────────────┐
│ 5. Create/update Release  │  publica en GitHub Releases
└──────────────────────────┘
```

### Steps en detalle

#### Step 1 — Checkout

Igual que en CI: clona el repositorio en el runner Windows.

---

#### Step 2 — Set up JDK 21

Idéntico al de CI. El cache de Maven también aplica aquí, por lo que si el pipeline de CI ya ha resuelto las dependencias recientemente, este paso será más rápido.

---

#### Step 3 — Run verify and package app image

```yaml
- name: Run verify and package app image
  shell: pwsh
  run: ./scripts/package-win.ps1
```

Ejecuta el script PowerShell [`scripts/package-win.ps1`](scripts/package-win.ps1), que hace tres cosas:

**3.1 — Quality gates** (`mvn clean verify`)  
Repite la verificación de calidad igual que en CI (compilación, tests, JaCoCo, Checkstyle). Así se garantiza que el artefacto que se va a distribuir pasa todos los controles, aunque el workflow de CI ya los haya pasado.

**3.2 — Empaquetado con `jpackage`**  
`jpackage` es una herramienta incluida en el JDK que genera una **app-image**: un directorio autocontenido con el runtime de Java embebido y el ejecutable de la aplicación. El usuario final no necesita tener Java instalado.

```
jpackage \
  --type app-image \
  --name MiniJuego2D \
  --input target \
  --main-jar test-1-junio2026-0.1.1.jar \
  --main-class com.example.App \
  --dest dist
```

Resultado: carpeta `dist/MiniJuego2D/` con el `.exe` y las DLLs del runtime.

---

#### Step 4 — Archive app image

```yaml
- name: Archive app image
  shell: pwsh
  run: |
    if (Test-Path release) { Remove-Item -Recurse -Force release }
    New-Item -ItemType Directory -Path release | Out-Null
    Compress-Archive -Path dist/MiniJuego2D/* `
      -DestinationPath release/MiniJuego2D-${{ github.ref_name }}-win64.zip -Force
```

Comprime la app-image en un ZIP con el nombre del tag, por ejemplo:  
`MiniJuego2D-v1.0.0-win64.zip`

El paso borra previamente la carpeta `release/` si existía, para evitar artefactos residuales de ejecuciones anteriores.

---

#### Step 5 — Create or update GitHub Release

```yaml
- name: Create or update GitHub Release
  uses: softprops/action-gh-release@v2
  with:
    tag_name: ${{ github.ref_name }}
    name: Release ${{ github.ref_name }}
    generate_release_notes: true
    files: |
      release/MiniJuego2D-${{ github.ref_name }}-win64.zip
```

Usa la action `softprops/action-gh-release` para:
- Crear (o actualizar si ya existe) el release asociado al tag en GitHub.
- Generar las release notes automáticamente a partir de los commits desde el tag anterior.
- Adjuntar el ZIP como asset descargable.

El resultado visible en GitHub será algo como:

```
Release v1.0.0
  Assets:
    MiniJuego2D-v1.0.0-win64.zip   ← descarga para usuarios Windows
    Source code (zip)
    Source code (tar.gz)
```

---

## Cómo publicar una nueva versión (paso a paso)

1. Actualiza `<version>` en `pom.xml` y el parámetro `$AppVersion` en `scripts/package-win.ps1` con la nueva versión.
2. Haz commit y push a `main`.
3. Crea y sube el tag:
   ```bash
   git tag v1.1.0
   git push origin v1.1.0
   ```
4. El workflow **Release** se dispara automáticamente en GitHub Actions.
5. Comprueba el resultado en la pestaña **Actions** del repositorio.
6. Si todo es verde, el ZIP aparecerá en **Releases**.

---

## Preguntas frecuentes

**¿Por qué el CI corre en Linux y el Release en Windows?**  
El CI solo necesita compilar y testear — Linux es más barato y rápido. El Release empaqueta con `jpackage` para Windows, que requiere un runner Windows para generar un ejecutable nativo `.exe`.

**¿Puedo forzar el Release sin crear un tag?**  
Sí. Ve a Actions → Release → "Run workflow" (botón manual). Esto activa el disparador `workflow_dispatch`.

**El build falló en JaCoCo — ¿qué hago?**  
Ejecuta `mvn clean verify` en local. En `target/site/jacoco/index.html` verás qué líneas de `GameEngine` o `GameLogic` no están cubiertas. Añade tests que las ejerciten.

**El build falló en Checkstyle — ¿qué hago?**  
La salida de Maven indica el archivo y la línea exacta. Las reglas están en [`checkstyle.xml`](checkstyle.xml). Los errores más comunes son tabuladores en lugar de espacios y líneas que superan 140 caracteres.
