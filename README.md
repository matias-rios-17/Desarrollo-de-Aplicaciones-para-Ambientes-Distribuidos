## Ejercicio 2: Análisis Teórico-Práctico

### 1. Servidor no disponible

Si el servidor no está ejecutándose, el cliente lanza la excepción `java.net.ConnectException` y muestra `Connection refused: connect`.

### 2. Bloqueo por evento de red

La línea:

```java
servidor.accept();
```

bloquea el servidor hasta que un cliente se conecta.

### 3. Cliente y servidor en diferentes notebooks

Se debe reemplazar `localhost` por la dirección IP de la notebook donde se ejecuta el servidor. Ambas computadoras deben estar conectadas a la misma red Wi-Fi y permitir el puerto **5500** en el firewall.

## Ejecución

Servidor:

```bash
javac Servidor.java
java Servidor
```

Cliente, en otra terminal:

```bash
javac Cliente.java
java Cliente
```

## Capturas

