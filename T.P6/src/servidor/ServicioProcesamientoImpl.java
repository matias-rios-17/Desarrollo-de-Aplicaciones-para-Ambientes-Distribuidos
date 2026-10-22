package servidor;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementación de la lógica de negocio y métodos remotos.
 */
public class ServicioProcesamientoImpl extends UnicastRemoteObject implements ServicioProcesamiento {

    public ServicioProcesamientoImpl() throws RemoteException {
        super();
    }

    @Override
    public Estadisticas calcularEstadisticas(List<Double> numeros) throws RemoteException {
        if (numeros == null || numeros.isEmpty()) {
            throw new IllegalArgumentException("La lista de números no puede estar vacía.");
        }

        System.out.println("[AUDITORÍA] Operación ejecutada: calcularEstadisticas en " + numeros.size() + " elementos.");

        double suma = 0;
        double max = Collections.max(numeros);
        double min = Collections.min(numeros);

        for (double num : numeros) {
            suma += num;
        }

        double promedio = suma / numeros.size();

        double sumaDiferenciasCuadrado = 0;
        for (double num : numeros) {
            sumaDiferenciasCuadrado += Math.pow(num - promedio, 2);
        }
        double desviacionEstandar = Math.sqrt(sumaDiferenciasCuadrado / numeros.size());

        return new Estadisticas(promedio, max, min, desviacionEstandar);
    }

    @Override
    public boolean validarCuit(String cuit) throws RemoteException {
        System.out.println("[AUDITORÍA] Operación ejecutada: validarCuit -> " + cuit);

        if (cuit == null || !cuit.matches("\\d{11}")) {
            return false;
        }

        int[] factores = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};
        int suma = 0;
        for (int i = 0; i < 10; i++) {
            suma += Character.getNumericValue(cuit.charAt(i)) * factores[i];
        }

        int verificadorCalculado = 11 - (suma % 11);
        if (verificadorCalculado == 11) verificadorCalculado = 0;
        if (verificadorCalculado == 10) verificadorCalculado = 9;

        int verificadorReal = Character.getNumericValue(cuit.charAt(10));
        return verificadorCalculado == verificadorReal;
    }

    @Override
    public List<String> filtrarCadenas(List<String> cadenas, String patron) throws RemoteException {
        System.out.println("[AUDITORÍA] Operación ejecutada: filtrarCadenas con patrón -> '" + patron + "'");

        List<String> resultado = new ArrayList<>();
        if (cadenas != null && patron != null) {
            for (String s : cadenas) {
                if (s.contains(patron)) {
                    resultado.add(s);
                }
            }
        }
        return resultado;
    }
}