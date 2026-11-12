"""
TP10 - Plan de pruebas de carga con Locust sobre la API REST del TP7.

Prueba de carga normal (50 usuarios, 2 minutos):
    python -m locust -f locustfile.py --headless -u 50 -r 10 -t 2m --csv resultados\\carga_normal --html resultados\\carga_normal.html

Modo con interfaz web (abrir http://localhost:8089):
    python -m locust -f locustfile.py
"""
import random

from locust import HttpUser, between, task

# Ids que el servidor carga al arrancar (RepositorioProductos), siempre existen.
IDS_EXISTENTES = [1, 2, 3, 4, 5]

# Tiempo máximo de espera por petición. Si el servidor tarda más, Locust la
# cuenta como fallo (timeout), que es lo que buscamos en la prueba de estrés.
TIMEOUT_SEG = 5


class UsuarioCatalogo(HttpUser):
    """Usuario virtual que navega el catálogo de productos."""

    host = "http://localhost:8080"
    # Cada usuario "piensa" entre 0,5 y 1,5 segundos entre una petición y otra.
    wait_time = between(0.5, 1.5)

    def _verificar(self, resp, esperado):
        """Marca como fallo cualquier código distinto al esperado (incluye timeouts)."""
        if resp.status_code == esperado:
            resp.success()
        elif resp.status_code == 0:
            # status 0 = no hubo respuesta HTTP (timeout, conexión rechazada, etc.)
            resp.failure(f"Sin respuesta (timeout o conexión rechazada): {resp.error}")
        else:
            resp.failure(f"HTTP {resp.status_code} (se esperaba {esperado})")

    @task(5)
    def listar_productos(self):
        with self.client.get("/productos", name="GET /productos",
                             timeout=TIMEOUT_SEG, catch_response=True) as resp:
            self._verificar(resp, 200)

    @task(3)
    def ver_producto(self):
        id_producto = random.choice(IDS_EXISTENTES)
        with self.client.get(f"/productos/{id_producto}", name="GET /productos/{id}",
                             timeout=TIMEOUT_SEG, catch_response=True) as resp:
            self._verificar(resp, 200)

    @task(1)
    def crear_producto(self):
        datos = {"nombre": f"Producto carga {random.randint(1, 99999)}",
                 "precio": round(random.uniform(1000, 99999), 2)}
        with self.client.post("/productos", json=datos, name="POST /productos",
                              timeout=TIMEOUT_SEG, catch_response=True) as resp:
            self._verificar(resp, 201)
