"""
TP10 - Análisis de los CSV que genera Locust (--csv resultados\\<tag>).

Lee:
  resultados\\<tag>_stats.csv          -> totales de la corrida (fila "Aggregated")
  resultados\\<tag>_stats_history.csv  -> una fila por segundo, con la cantidad de usuarios

Muestra:
  1. Estadísticas generales: peticiones, fallos, promedio, mínimo, máximo, p90, p95, RPS.
  2. Tabla en Markdown agrupada por cantidad de usuarios (ideal para la prueba de estrés):
     tiempo de respuesta promedio / p90 / p95 / máximo, RPS y % de fallos.
  3. Gráfico resultados\\grafico_<tag>.png (p95 y promedio vs usuarios) si está matplotlib.
  4. Si existe resultados\\recursos_<tag>.csv, el resumen de CPU/RAM.

Uso:
    python analizar_resultados.py carga_normal
    python analizar_resultados.py estres
    python analizar_resultados.py estres --guardar   (además guarda resultados\\tabla_<tag>.md)
"""
import argparse
import csv
import os
import sys
from collections import OrderedDict

CARPETA = "resultados"


def a_float(valor):
    try:
        return float(valor)
    except (TypeError, ValueError):
        return None  # Locust escribe "N/A" cuando no hay datos


def leer_csv(ruta):
    with open(ruta, newline="", encoding="utf-8") as f:
        return list(csv.DictReader(f))


def fmt(valor, decimales=0):
    return "-" if valor is None else f"{valor:.{decimales}f}"


def estadisticas_generales(tag):
    ruta = os.path.join(CARPETA, f"{tag}_stats.csv")
    if not os.path.exists(ruta):
        print(f"(No encontré {ruta}; salteo las estadísticas generales)")
        return None
    filas = leer_csv(ruta)
    print(f"\n=== Estadísticas generales ({ruta}) ===\n")
    print("| Endpoint | Peticiones | Fallos | % fallos | Prom. (ms) | Mín. (ms) | Máx. (ms) | p90 (ms) | p95 (ms) | RPS |")
    print("|---|---|---|---|---|---|---|---|---|---|")
    total = None
    for fila in filas:
        nombre = fila["Name"] if fila["Name"] == "Aggregated" else f'{fila["Type"]} {fila["Name"]}'.replace("GET GET", "GET").replace("POST POST", "POST")
        pet = int(fila["Request Count"])
        fallos = int(fila["Failure Count"])
        pct = 100.0 * fallos / pet if pet else 0.0
        linea = (f'| {"**Total**" if fila["Name"] == "Aggregated" else nombre} | {pet} | {fallos} | {pct:.2f} % | '
                 f'{fmt(a_float(fila["Average Response Time"]), 1)} | {fmt(a_float(fila["Min Response Time"]))} | '
                 f'{fmt(a_float(fila["Max Response Time"]))} | {fmt(a_float(fila["90%"]))} | {fmt(a_float(fila["95%"]))} | '
                 f'{fmt(a_float(fila["Requests/s"]), 1)} |')
        print(linea)
        if fila["Name"] == "Aggregated":
            total = fila
    if total:
        print("\nResumen (fila Aggregated):")
        print(f'  Peticiones: {total["Request Count"]} | Fallos: {total["Failure Count"]}')
        print(f'  Promedio: {fmt(a_float(total["Average Response Time"]), 1)} ms | Mínimo: {fmt(a_float(total["Min Response Time"]))} ms | '
              f'Máximo: {fmt(a_float(total["Max Response Time"]))} ms')
        print(f'  p90: {fmt(a_float(total["90%"]))} ms | p95: {fmt(a_float(total["95%"]))} ms | RPS: {fmt(a_float(total["Requests/s"]), 1)}')
    return total


def agrupar_por_usuarios(tag, min_muestras):
    ruta = os.path.join(CARPETA, f"{tag}_stats_history.csv")
    if not os.path.exists(ruta):
        print(f"(No encontré {ruta}; salteo la tabla por usuarios)")
        return []
    filas = [f for f in leer_csv(ruta) if f["Name"] == "Aggregated"]
    grupos = OrderedDict()
    anterior = None  # fila anterior, para calcular diferencias de los acumulados
    for f in filas:
        usuarios = int(f["User Count"])
        cnt = int(f["Total Request Count"])
        fallos = int(f["Total Failure Count"])
        prom_total = a_float(f["Total Average Response Time"]) or 0.0
        g = grupos.setdefault(usuarios, {"rps": [], "fps": [], "p90": [], "p95": [], "max": [],
                                         "pet": 0, "fallos": 0, "suma_ms": 0.0, "muestras": 0})
        g["muestras"] += 1
        for clave, col in (("rps", "Requests/s"), ("fps", "Failures/s"), ("p90", "90%"), ("p95", "95%"), ("max", "100%")):
            v = a_float(f[col])
            if v is not None:
                g[clave].append(v)
        if anterior is not None:
            d_cnt = cnt - anterior[0]
            if d_cnt > 0:
                g["pet"] += d_cnt
                g["fallos"] += max(0, fallos - anterior[1])
                # promedio del tramo = (suma acumulada actual - suma acumulada anterior) / peticiones nuevas
                g["suma_ms"] += cnt * prom_total - anterior[0] * anterior[2]
        anterior = (cnt, fallos, prom_total)

    resultado = []
    for usuarios, g in grupos.items():
        if usuarios == 0 or g["muestras"] < min_muestras:
            continue  # se descartan los segundos de transición de la rampa
        prom = lambda lst: sum(lst) / len(lst) if lst else None
        resultado.append({
            "usuarios": usuarios,
            "muestras": g["muestras"],
            "prom_ms": g["suma_ms"] / g["pet"] if g["pet"] else None,
            "p90_ms": prom(g["p90"]),
            "p95_ms": prom(g["p95"]),
            "max_ms": max(g["max"]) if g["max"] else None,
            "rps": prom(g["rps"]),
            "rps_ok": (prom(g["rps"]) or 0) - (prom(g["fps"]) or 0),
            "pet": g["pet"],
            "fallos": g["fallos"],
            "pct_fallos": 100.0 * g["fallos"] / g["pet"] if g["pet"] else 0.0,
        })
    return resultado


def tabla_markdown(filas):
    lineas = ["| Usuarios | Segundos | Prom. (ms) | p90 (ms) | p95 (ms) | Máx. (ms) | RPS total | RPS exitosas | Peticiones | Fallos | % fallos |",
              "|---|---|---|---|---|---|---|---|---|---|---|"]
    for r in filas:
        lineas.append(f'| {r["usuarios"]} | {r["muestras"]} | {fmt(r["prom_ms"], 1)} | {fmt(r["p90_ms"])} | '
                      f'{fmt(r["p95_ms"])} | {fmt(r["max_ms"])} | {fmt(r["rps"], 1)} | {fmt(r["rps_ok"], 1)} | {r["pet"]} | {r["fallos"]} | '
                      f'{r["pct_fallos"]:.2f} % |')
    return "\n".join(lineas)


def graficar(tag, filas):
    try:
        import matplotlib
        matplotlib.use("Agg")
        import matplotlib.pyplot as plt
    except ImportError:
        print("\n(matplotlib no está instalado: no genero el gráfico. Instalar con:  python -m pip install matplotlib)")
        return
    if not filas:
        return
    x = [r["usuarios"] for r in filas]
    fig, ax1 = plt.subplots(figsize=(9, 5))
    ax1.plot(x, [r["p95_ms"] or 0 for r in filas], "o-", color="tab:red", label="p95 (ms)")
    ax1.plot(x, [r["prom_ms"] or 0 for r in filas], "s-", color="tab:blue", label="Promedio (ms)")
    ax1.set_xlabel("Usuarios concurrentes")
    ax1.set_ylabel("Tiempo de respuesta (ms)")
    ax1.grid(True, alpha=0.3)
    ax2 = ax1.twinx()
    ax2.plot(x, [r["rps_ok"] or 0 for r in filas], "^--", color="tab:green", label="RPS exitosas")
    ax2.set_ylabel("Peticiones por segundo (RPS)")
    ax2.set_ylim(bottom=0)
    ax1.set_ylim(bottom=0)
    lineas = ax1.get_legend_handles_labels()
    lineas2 = ax2.get_legend_handles_labels()
    ax1.legend(lineas[0] + lineas2[0], lineas[1] + lineas2[1], loc="upper left")
    plt.title(f"Tiempo de respuesta y RPS vs usuarios ({tag})")
    fig.tight_layout()
    ruta = os.path.join(CARPETA, f"grafico_{tag}.png")
    fig.savefig(ruta, dpi=120)
    print(f"\nGráfico guardado en {ruta}")


def resumen_recursos(tag):
    ruta = os.path.join(CARPETA, f"recursos_{tag}.csv")
    if not os.path.exists(ruta):
        return
    filas = leer_csv(ruta)
    if not filas:
        return
    print(f"\n=== Recursos ({ruta}, {len(filas)} muestras) ===\n")
    print("| Métrica | Promedio | Máximo |")
    print("|---|---|---|")
    for etiqueta, col, unidad in [("CPU sistema", "cpu_sistema_pct", " %"), ("RAM sistema", "ram_sistema_pct", " %"),
                                  ("CPU proceso java", "cpu_java_pct", " %"), ("RAM proceso java (RSS)", "ram_java_mb", " MB"),
                                  ("Hilos del proceso java", "hilos_java", "")]:
        valores = [a_float(f[col]) for f in filas if a_float(f[col]) is not None]
        if valores:
            print(f"| {etiqueta} | {sum(valores) / len(valores):.1f}{unidad} | {max(valores):.1f}{unidad} |")


def main():
    ap = argparse.ArgumentParser(description="Analiza los CSV de Locust del TP10")
    ap.add_argument("tag", help="prefijo usado en --csv (ej.: carga_normal, estres, estres_optimizado)")
    ap.add_argument("--min-muestras", type=int, default=3,
                    help="segundos mínimos con la misma cantidad de usuarios para incluirla en la tabla (por defecto 3)")
    ap.add_argument("--guardar", action="store_true", help="guarda la tabla en resultados\\tabla_<tag>.md")
    args = ap.parse_args()

    if not os.path.isdir(CARPETA):
        print("No existe la carpeta 'resultados'. Ejecutá este script desde la carpeta T.P10.")
        sys.exit(1)

    estadisticas_generales(args.tag)
    filas = agrupar_por_usuarios(args.tag, args.min_muestras)
    if filas:
        tabla = tabla_markdown(filas)
        print(f"\n=== Tiempo de respuesta vs usuarios ({args.tag}) ===\n")
        print(tabla)
        print("\n(p90/p95/RPS: promedio de las ventanas de cada segundo con esa cantidad de usuarios; "
              "Prom. y % fallos: calculados sobre las peticiones de ese tramo;\n"
              " RPS total incluye las peticiones que terminaron en timeout; RPS exitosas = RPS total - fallos/s)")
        if args.guardar:
            ruta = os.path.join(CARPETA, f"tabla_{args.tag}.md")
            with open(ruta, "w", encoding="utf-8") as f:
                f.write(tabla + "\n")
            print(f"Tabla guardada en {ruta}")
        graficar(args.tag, filas)
    resumen_recursos(args.tag)


if __name__ == "__main__":
    main()
