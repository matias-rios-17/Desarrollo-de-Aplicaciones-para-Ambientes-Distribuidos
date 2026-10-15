import java.io.*;
import java.nio.charset.StandardCharsets;

public class ParserMensajes {

    // --- FORMATO JSON (TEXTO) ---
    public static String aJson(Transaccion t) {
        return String.format(java.util.Locale.US, "{\"id\":%d,\"origen\":\"%s\",\"monto\":%.2f,\"timestamp\":%d}",
                t.getIdTransaccion(), t.getOrigen(), t.getMonto(), t.getTimestamp());
    }

    public static Transaccion desdeJson(String json) {
        // Parsing manual liviano de JSON
        json = json.replace("{", "").replace("}", "").replace("\"", "");
        String[] partes = json.split(",");
        
        int id = Integer.parseInt(partes[0].split(":")[1]);
        String origen = partes[1].split(":")[1];
        double monto = Double.parseDouble(partes[2].split(":")[1]);
        long timestamp = Long.parseLong(partes[3].split(":")[1]);

        return new Transaccion(id, origen, monto, timestamp);
    }

    // --- FORMATO BINARIO DIRECTO ---
    public static byte[] aBinario(Transaccion t) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(t.getIdTransaccion());
        dos.writeUTF(t.getOrigen()); // Escribe la cadena codificada en UTF-8
        dos.writeDouble(t.getMonto());
        dos.writeLong(t.getTimestamp());

        dos.flush();
        return baos.toByteArray();
    }

    public static Transaccion desdeBinario(byte[] bytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bais);

        int id = dis.readInt();
        String origen = dis.readUTF();
        double monto = dis.readDouble();
        long timestamp = dis.readLong();

        return new Transaccion(id, origen, monto, timestamp);
    }
}