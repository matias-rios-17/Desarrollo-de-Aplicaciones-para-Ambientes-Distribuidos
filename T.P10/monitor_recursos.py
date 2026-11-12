"""
TP10 - Monitor de recursos (CPU y RAM) durante las pruebas de carga.

Cada segundo registra:
  - CPU % y RAM % de toda la máquina
  - CPU % y memoria RSS (MB) del proceso java del servidor (servidor.ServidorREST)

Ejemplos (en una terminal aparte, con el servidor ya levantado):
    python monitor_recursos.py --tag carga_normal --duracion 150
    python monitor_recursos.py --tag estres --duracion 200
    python monitor_recursos.py --pid 12345 --tag estres
    python monitor_recursos.py --nombre java --tag estres

Genera resultados\\recursos_<tag>.csv y muestra un resumen (promedio / máximo).
Si --duracion es 0 corre hasta que se apriete Ctrl + C.
"""
import argparse
import csv
import os
import sys
import time
from datetime import datetime

try:
    import psutil
except ImportError:
    print("Falta psutil. Instalarlo con:  python -m pip install psutil")
    sys.exit(1)

CLASE_SERVIDOR = "servidor.ServidorREST"


def buscar_proceso(pid=None, nombre="java"):
    """Devuelve el proceso del servidor: por PID, o el java cuyo cmdline contiene servidor.ServidorREST."""
    if pid:
        return psutil.Process(pid)
    candidatos = []
    for p in psutil.process_iter(["pid", "name", "cmdline"]):
        try:
            nom = (p.info["name"] or "").lower()
            cmd = " ".join(p.info["cmdline"] or [])
        except (psutil.NoSuchProcess, psutil.AccessDenied):
            continue
        if nombre.lower() in nom or nombre.lower() in cmd.lower():
            if CLASE_SERVIDOR in cmd:
                return p
            candidatos.append(p)
    # Si no hay ninguno con servidor.ServidorREST, se usa el primero que coincida con el nombre
    return candidatos[0] if candidatos else None


def main():
    ap = argparse.ArgumentParser(description="Monitor de CPU/RAM para el TP10")
    ap.add_argument("--pid", type=int, help="PID del proceso del servidor")
    ap.add_argument("--nombre", default="java", help="nombre del proceso a buscar (por defecto: java)")
    ap.add_argument("--duracion", type=float, default=0, help="segundos a monitorear (0 = hasta Ctrl+C)")
    ap.add_argument("--intervalo", type=float, default=1.0, help="segundos entre muestras (por defecto 1)")
    ap.add_argument("--tag", default="prueba", help="sufijo del CSV: resultados\\recursos_<tag>.csv")
    args = ap.parse_args()

    proc = buscar_proceso(args.pid, args.nombre)
    if proc is None:
        print(f"No encontré ningún proceso '{args.nombre}' con {CLASE_SERVIDOR}. ¿Está levantado el servidor?")
        sys.exit(1)

    os.makedirs("resultados", exist_ok=True)
    ruta_csv = os.path.join("resultados", f"recursos_{args.tag}.csv")
    nucleos = psutil.cpu_count(logical=True) or 1

    print(f"Monitoreando PID {proc.pid} ({proc.name()}) cada {args.intervalo} s -> {ruta_csv}")
    print(f"Núcleos lógicos: {nucleos}. CPU del proceso expresada como % del total de la máquina.")
    print("Ctrl + C para terminar antes.\n")
    print(f"{'hora':8} | {'CPU sist %':>10} | {'RAM sist %':>10} | {'CPU java %':>10} | {'RAM java MB':>11} | {'hilos':>5}")

    # Primera llamada para inicializar los contadores de CPU
    psutil.cpu_percent(None)
    proc.cpu_percent(None)

    muestras = []
    inicio = time.time()
    try:
        with open(ruta_csv, "w", newline="", encoding="utf-8") as f:
            w = csv.writer(f)
            w.writerow(["timestamp", "segundos", "cpu_sistema_pct", "ram_sistema_pct",
                        "ram_sistema_usada_mb", "cpu_java_pct", "ram_java_mb", "hilos_java"])
            while args.duracion <= 0 or time.time() - inicio < args.duracion:
                time.sleep(args.intervalo)
                try:
                    cpu_sis = psutil.cpu_percent(None)
                    mem = psutil.virtual_memory()
                    # cpu_percent del proceso puede superar 100 (suma de núcleos):
                    # se divide por la cantidad de núcleos para compararlo con el total.
                    cpu_java = proc.cpu_percent(None) / nucleos
                    rss_mb = proc.memory_info().rss / (1024 * 1024)
                    hilos = proc.num_threads()
                except psutil.NoSuchProcess:
                    print("El proceso del servidor terminó. Fin del monitoreo.")
                    break
                fila = [datetime.now().strftime("%H:%M:%S"), round(time.time() - inicio, 1),
                        round(cpu_sis, 1), round(mem.percent, 1), round(mem.used / (1024 * 1024)),
                        round(cpu_java, 1), round(rss_mb, 1), hilos]
                w.writerow(fila)
                f.flush()
                muestras.append(fila)
                print(f"{fila[0]:8} | {fila[2]:>10} | {fila[3]:>10} | {fila[5]:>10} | {fila[6]:>11} | {fila[7]:>5}")
    except KeyboardInterrupt:
        print("\nMonitoreo detenido por el usuario.")

    if not muestras:
        print("No se registraron muestras.")
        return

    def resumen(i):
        valores = [m[i] for m in muestras]
        return sum(valores) / len(valores), max(valores)

    print(f"\n=== Resumen de recursos ({len(muestras)} muestras) ===")
    for etiqueta, i, unidad in [("CPU sistema", 2, "%"), ("RAM sistema", 3, "%"),
                                ("CPU java", 5, "%"), ("RAM java (RSS)", 6, " MB"),
                                ("Hilos java", 7, "")]:
        prom, maximo = resumen(i)
        print(f"{etiqueta:15}: promedio {prom:8.1f}{unidad} | máximo {maximo:8.1f}{unidad}")
    print(f"CSV guardado en {ruta_csv}")


if __name__ == "__main__":
    main()
