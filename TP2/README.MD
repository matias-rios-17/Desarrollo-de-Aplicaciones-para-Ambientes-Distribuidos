# TP2 Ejercicio 3: Análisis Teórico

### 1. ¿Qué problema genera en un servidor saturado que todos los clientes reintenten sus peticiones exactamente al mismo tiempo y con intervalos fijos (sin Jitter)?

#### El Efecto Estampida (Thundering Herd Problem) y Picos Sincronizados

Cuando un servidor sufre una caída transitoria o una sobrecarga alta, todos los clientes activos experimentan fallos al mismo tiempo. Si la estrategia de reintento utiliza un intervalo fijo o incluso un *Exponential Backoff* puro **sin variabilidad (Jitter)**:

* **Sincronización Periódica:** Todos los clientes reintentarán exactamente en los mismos milisegundos ($t = 1s, t = 2s, t = 4s$, etc.).
* **Oleadas Repetitivas (Spikes):** El servidor recibe cero peticiones durante el tiempo de espera y, repentinamente, un pico brutal de peticiones simultáneas.
* **Incapacidad de Recuperación:** Estas oleadas periódicas vuelven a tumbar al servidor en cuanto intenta recuperarse, impidiendo que procese la cola de trabajo de forma escalonada y estabilice sus recursos (CPU, RAM, descriptores de sockets).

#### La Solución mediante **Jitter**:

Al sumar un valor aleatorio (*Jitter*) a cada intervalo de espera, la distribución de los reintentos se de-sincroniza en el tiempo. La carga llega al servidor distribuida en una curva suave en lugar de picos agrupados, permitiendo que el servidor atienda solicitudes gradualmente a medida que libera recursos.

### 2. Thundering Herd Problem o Efecto Estampida

El **Thundering Herd Problem** o **Efecto Estampida** ocurre cuando muchos clientes o procesos intentan acceder nuevamente a un servidor al mismo tiempo.

En una arquitectura distribuida, si el servidor se encuentra saturado y todos los clientes realizan reintentos con intervalos fijos y exactamente iguales, los reintentos pueden quedar sincronizados. Esto provoca que una gran cantidad de solicitudes lleguen simultáneamente al servidor.

Como consecuencia, el servidor puede recibir una nueva sobrecarga justo cuando todavía está intentando recuperarse del problema original. Esto puede aumentar los tiempos de respuesta, generar nuevos errores y, en casos extremos, provocar una caída del servicio.

El uso de **Jitter** ayuda a solucionar este problema porque agrega un componente aleatorio al tiempo de espera de cada cliente. De esta manera, los reintentos se distribuyen en diferentes momentos y se reduce la cantidad de solicitudes simultáneas.

### 3. Diferencia entre fallo transitorio y fallo permanente

Un **fallo transitorio** es un problema temporal que puede desaparecer después de un determinado período de tiempo. En estos casos, realizar un reintento puede permitir que la operación se complete correctamente.

**Ejemplo:** un servidor está momentáneamente saturado y rechaza una solicitud. Después de unos segundos, el servidor vuelve a estar disponible y un nuevo intento puede tener éxito.

Un **fallo permanente** es un problema que no se soluciona simplemente esperando o reintentando la operación. Requiere corregir la causa del problema.

**Ejemplo:** un cliente intenta conectarse a un servidor utilizando una dirección IP o un puerto incorrecto. Realizar nuevos intentos con la misma configuración no solucionará el problema.

Por lo tanto, los mecanismos de reintento son especialmente útiles frente a fallos transitorios, mientras que ante fallos permanentes es necesario corregir la causa del error.

CAPTURAS 


