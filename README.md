## Mini Juego 2D - Java

POC de videojuego 2D en Java Swing con enfoque de calidad: separacion por capas, pruebas automatizadas y quality gates.

## Requisitos

- Java 21
- Maven 3.9+
- (Opcional para empaquetado) `jpackage` disponible en el JDK

## Ejecutar en desarrollo

```bash
mvn exec:java
```

## Ejecutar pruebas

```bash
mvn test
```

## Pipeline local de calidad

```bash
mvn verify
```

Esto valida:
- pruebas unitarias e integracion
- cobertura minima de codigo con JaCoCo
- reglas de estilo con Checkstyle

## Empaquetado para Windows (app-image)

```powershell
./scripts/package-win.ps1
```

El ejecutable empaquetado queda en `dist/`.

## Versionado y release

- Version actual: `0.1.0`
- Formato: Semantic Versioning
- Historial de cambios: [CHANGELOG.md](CHANGELOG.md)

Flujo sugerido para nueva version:
1. Actualizar version en `pom.xml`.
2. Agregar entrada en `CHANGELOG.md`.
3. Ejecutar `mvn verify`.
4. Empaquetar con `./scripts/package-win.ps1`.
5. Etiquetar release en Git (`vX.Y.Z`).

## Explicacion interactiva del juego

Abre el archivo [explicacion-juego.html](explicacion-juego.html) en tu navegador para ver una guia interactiva del codigo del videojuego 2D.