# Changelog

Este proyecto sigue [Semantic Versioning](https://semver.org/lang/es/) y el formato de [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/).

## [0.1.0] - 2026-06-04
### Added
- Juego 2D base en Swing con movimiento, colisiones, puntaje y reinicio.
- Separacion entre UI (`App`), motor (`GameEngine`) y reglas (`GameLogic`).
- Suite de pruebas unitarias e integracion con JUnit 5.
- Quality gates en Maven: JaCoCo y Checkstyle.
- CI inicial en GitHub Actions (`mvn -B verify`).
- Guia interactiva en HTML (`explicacion-juego.html`).
- Script de empaquetado local con `jpackage`.
