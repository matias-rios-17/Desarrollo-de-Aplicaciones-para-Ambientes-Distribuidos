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
<img width="1042" height="110" alt="Captura de pantalla 2026-08-20 215815" src="https://github.com/user-attachments/assets/1771f26c-f65c-4b0f-988b-82aaf04e2852" />

<img width="1007" height="107" alt="Captura de pantalla 2026-08-20 215726" src="https://github.com/user-attachments/assets/f18fa1f7-96a9-48de-8a71-12ef5470c5cd" />

<img width="1024" height="307" alt="Captura de pantalla 2026-08-20 215231" src="https://github.com/user-attachments/assets/fd92bbe8-48fd-41fc-93dd-532c93074710" />
