"""
TP10 - Prueba de estrés (punto de quiebre) con una rampa de usuarios.

Usa el mismo usuario virtual de locustfile.py y una forma de carga
(LoadTestShape) que sube de 50 a 250 usuarios en escalones:

    Escalón | Desde (s) | Hasta (s) | Usuarios
    --------+-----------+-----------+---------
       1    |     0     |    30     |    50
       2    |    30     |    60     |   100
       3    |    60     |    90     |   150
       4    |    90     |   120     |   200
       5    |   120     |   180     |   250   (último escalón dura el doble)

Ejecutar (sin -u, -r ni -t: los define la forma de carga):
    python -m locust -f locustfile_estres.py --headless --csv resultados\\estres --html resultados\\estres.html

Para probar rápido se puede acortar cada escalón con la variable LOCUST_PASO_SEG
(PowerShell):  $env:LOCUST_PASO_SEG = "10"
y cambiar los escalones con LOCUST_ESCALONES:  $env:LOCUST_ESCALONES = "50,100,200,400,800"
(para volver a los valores por defecto: Remove-Item Env:LOCUST_PASO_SEG, Env:LOCUST_ESCALONES)
"""
import os

from locust import LoadTestShape

from locustfile import UsuarioCatalogo  # noqa: F401  (Locust detecta la clase al importarla)

PASO_SEG = int(os.environ.get("LOCUST_PASO_SEG", "30"))
# Se pueden cambiar los escalones con LOCUST_ESCALONES (ej.: "50,100,200,400,800"),
# útil para buscar el nuevo punto de quiebre de la versión optimizada.
ESCALONES_USUARIOS = [int(x) for x in os.environ.get("LOCUST_ESCALONES", "50,100,150,200,250").split(",")]
VELOCIDAD_ALTA = 10  # usuarios nuevos por segundo


# Rampa automática desactivada: así la interfaz web permite cargar a mano
# la cantidad de usuarios y el spawn rate antes de tocar Start.
# class RampaEstres(LoadTestShape):
#     """Escalones de 50 -> 250 usuarios; el último dura dos pasos (~3 min en total)."""
#
#     def tick(self):
#         t = self.get_run_time()
#         duracion_total = PASO_SEG * (len(ESCALONES_USUARIOS) + 1)
#         if t >= duracion_total:
#             return None  # fin de la prueba
#         indice = min(int(t // PASO_SEG), len(ESCALONES_USUARIOS) - 1)
#         return ESCALONES_USUARIOS[indice], VELOCIDAD_ALTA
