# Mini Juego 2D — Java

POC de videojuego 2D en Java Swing con enfoque de calidad: separación por capas, pruebas automatizadas y quality gates.

El juego consiste en mover un cuadrado verde por la pantalla esquivando un cuadrado rojo que se desplaza hacia la izquierda. Cada vez que el enemigo cruza el borde izquierdo sin colisionar, el jugador suma un punto.

---

## Requisitos

| Herramienta | Versión mínima |
|-------------|----------------|
| Java (JDK)  | 21 (LTS)       |
| Maven       | 3.9+           |
| jpackage    | incluido en JDK 21 — solo para empaquetado Windows |

---

## Arranque rápido

```bash
# Compilar y ejecutar el juego
mvn exec:java

# Ejecutar únicamente los tests
mvn test

# Verificación completa: tests + cobertura + estilo
mvn verify
```

**Controles en el juego:**

| Tecla | Acción |
|-------|--------|
| `W` / `↑` | Mover arriba |
| `S` / `↓` | Mover abajo |
| `A` / `←` | Mover izquierda |
| `D` / `→` | Mover derecha |
| `R`        | Reiniciar tras Game Over |

---

## Arquitectura del código

El proyecto aplica una **separación estricta entre lógica y presentación**. Ninguna clase de lógica de juego importa clases de Swing ni depende de la pantalla.

```
┌─────────────────────────────────────────────────────────┐
│                        App.java                          │
│  Punto de entrada. Crea la ventana JFrame y el GamePanel │
│                                                         │
│  ┌──────────────────────────────────────────────────┐   │
│  │  GamePanel (clase interna privada de App)         │   │
│  │                                                  │   │
│  │  • Gestiona el bucle de juego (javax.swing.Timer) │   │
│  │  • Captura teclado (KeyAdapter)                  │   │
│  │  • Dibuja en pantalla (paintComponent)           │   │
│  │  • Delega TODA la lógica en GameEngine           │   │
│  └──────────────────┬───────────────────────────────┘   │
└─────────────────────│───────────────────────────────────┘
                      │ llama a tick() cada 16 ms
                      ▼
┌─────────────────────────────────────────────────────────┐
│                    GameEngine.java                       │
│  Motor del juego. Sin ninguna dependencia de UI.         │
│                                                         │
│  • Mantiene el estado: posición del jugador, enemigo,   │
│    puntuación y flag running                            │
│  • tick() avanza un frame: mueve jugador y enemigo,     │
│    detecta colisión, actualiza puntuación               │
│  • Devuelve copias defensivas de los rectángulos        │
│    (getPlayer/getEnemy) para que la UI no mute el estado│
│  • Valida todos los parámetros en el constructor        │
│                                                         │
│  Delega cálculos sin estado en:                         │
│  ┌──────────────────────────────────────────────────┐   │
│  │  GameLogic.java — funciones estáticas puras       │   │
│  │                                                  │   │
│  │  • clampPlayerToBounds  — mantiene al jugador     │   │
│  │    dentro del panel                              │   │
│  │  • updateVerticalEnemySpeed — rebote arriba/abajo │   │
│  │  • randomSignedSpeed — velocidad aleatoria ±      │   │
│  │  • hasCollision — intersección de rectángulos     │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

### Por qué esta separación

`GameLogic` contiene funciones puras (mismo input → mismo output, sin efectos secundarios). Al aislarlas se pueden testear de forma exhaustiva sin instanciar el motor ni la UI. `GameEngine` encapsula el estado mutable y es testeable de forma unitaria con cualquier `Random` determinista. `GamePanel` es la única capa que toca Swing y no necesita tests (la lógica que importa ya está probada en las capas inferiores).

---

## Estructura del proyecto

```
├── src/
│   ├── main/java/com/example/
│   │   ├── App.java          ← ventana, bucle gráfico, teclado
│   │   ├── GameEngine.java   ← estado del juego, lógica de frames
│   │   └── GameLogic.java    ← funciones puras sin estado
│   └── test/java/com/example/
│       ├── AppTest.java                  ← tests unitarios de GameLogic y GameEngine
│       └── GameEngineIntegrationTest.java ← tests de comportamiento frame a frame
├── scripts/
│   └── package-win.ps1   ← empaqueta el JAR como .exe con jpackage
├── checkstyle.xml        ← reglas de estilo de código
├── pom.xml               ← dependencias, plugins y quality gates
├── docs/
│   └── cicd.md           ← documentación de los pipelines CI/CD
└── CHANGELOG.md
```

---

## Tests

El proyecto tiene **10 tests** divididos en dos clases:

### `AppTest` — tests unitarios

Ejercitan `GameLogic` y el constructor de `GameEngine` directamente:

| Test | Qué verifica |
|------|-------------|
| `clampPlayerToBounds_keepsPlayerInsideBoard` | El jugador no puede salir del área de juego |
| `updateVerticalEnemySpeed_invertsOnTopAndBottomBorders` | El enemigo rebota en los bordes superior e inferior |
| `hasCollision_returnsTrueOnlyWhenRectanglesOverlap` | Detección de colisión correcta |
| `randomSignedSpeed_staysInExpectedAbsoluteRange` | La velocidad aleatoria siempre queda en el rango configurado |
| `gameEngine_constructorRejectsInvalidSpeedRange` | El constructor lanza `IllegalArgumentException` con rango de velocidad inválido |
| `gameEngine_constructorRejectsNullRandom` | El constructor lanza `NullPointerException` si `Random` es null |

### `GameEngineIntegrationTest` — tests de integración

Verifican el comportamiento del motor a lo largo de frames reales:

| Test | Qué verifica |
|------|-------------|
| `tick_movesEnemyLeftOnEachFrame` | El enemigo avanza hacia la izquierda cada frame |
| `tick_whenEnemyLeavesScreen_addsPointAndRespawns` | Al cruzar el borde se suma un punto y el enemigo reaparece |
| `tick_whenCollisionHappens_stopsGameAndNextTicksDoNotAdvance` | La colisión detiene el juego; los ticks posteriores no mueven nada |
| `getPlayer_returnsDefensiveCopy` | Modificar el rectángulo devuelto no altera el estado interno del motor |

---

## Quality gates (`mvn verify`)

`mvn verify` ejecuta tres validaciones en orden. Si cualquiera falla, el build se detiene:

1. **Tests** — maven-surefire-plugin ejecuta todos los tests JUnit 5. Fallo de un solo test detiene el build.

2. **Cobertura de código** — JaCoCo mide las líneas cubiertas de `GameEngine` y `GameLogic` (la UI está excluida intencionalmente). El umbral mínimo es **70%**. El informe HTML se genera en `target/site/jacoco/index.html`.

3. **Estilo de código** — Checkstyle aplica las reglas de `checkstyle.xml` tanto al código de producción como al de tests:
   - Sin tabuladores (usar espacios)
   - Máximo 140 caracteres por línea
   - Llaves obligatorias en todos los bloques `if`/`for`/`while`
   - Sin imports con wildcard (`import java.util.*`)
   - Sin bloques vacíos

---

## Empaquetado para Windows

```powershell
./scripts/package-win.ps1
```

El script:
1. Ejecuta `mvn clean verify` (todos los quality gates deben pasar)
2. Usa `jpackage` para generar una **app-image** en `dist/MiniJuego2D/` — un ejecutable `.exe` con el runtime de Java embebido, sin que el usuario final necesite tener Java instalado

---

## Versionado y release

- **Versión actual:** `0.1.1`
- **Formato:** [Semantic Versioning](https://semver.org) — `MAJOR.MINOR.PATCH`
- **Historial:** [CHANGELOG.md](CHANGELOG.md)

Para publicar una nueva versión:

1. Actualizar `<version>` en `pom.xml` y `$AppVersion` en `scripts/package-win.ps1`
2. Añadir entrada en `CHANGELOG.md`
3. Commit y push a `main`
4. Crear y subir el tag: `git tag vX.Y.Z && git push origin vX.Y.Z`
5. El pipeline **Release** de GitHub Actions genera el ZIP y lo publica automáticamente

Ver [docs/cicd.md](docs/cicd.md) para la documentación detallada de los pipelines.

---

## Explicación interactiva del juego

Abre [explicacion-juego.html](explicacion-juego.html) en el navegador para una guía visual del código.