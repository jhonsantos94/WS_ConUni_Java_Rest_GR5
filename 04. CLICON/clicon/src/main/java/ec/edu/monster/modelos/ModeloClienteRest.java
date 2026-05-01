package ec.edu.monster.modelos;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModeloClienteRest {

    private static final String URL_BASE = "http://localhost:8080/04.SERVIDOR/api";
    private final HttpClient clienteHttp;
    private String tokenSesion;

    public ModeloClienteRest() {
        this.clienteHttp = HttpClient.newHttpClient();
    }

    public boolean estaAutenticado() {
        return tokenSesion != null;
    }

    public void cerrarSesion() {
        this.tokenSesion = null;
    }

    public void iniciarSesion(String usuario, String contrasenia) throws Exception {
        // Respetamos @JsonbProperty de tu API
        String jsonPeticion = String.format("{\"Usuario\": \"%s\", \"Contrasena\": \"%s\"}", usuario, contrasenia);

        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create(URL_BASE + "/seguridad/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPeticion))
                .build();

        HttpResponse<String> respuesta = clienteHttp.send(peticion, HttpResponse.BodyHandlers.ofString());

        if (respuesta.statusCode() == 200) {
            this.tokenSesion = extraerValorJson(respuesta.body(), "token", false);
        } else {
            throw new Exception("Credenciales incorrectas o error de servidor (" + respuesta.statusCode() + ")");
        }
    }

    public void cambiarContrasenia(String contraseniaActual, String contraseniaNueva) throws Exception {
        String jsonPeticion = String.format("{\"ContraseniaActual\": \"%s\", \"NuevaContrasenia\": \"%s\"}", contraseniaActual, contraseniaNueva);

        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create(URL_BASE + "/seguridad/cambiar-contrasena"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + this.tokenSesion)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPeticion))
                .build();

        HttpResponse<String> respuesta = clienteHttp.send(peticion, HttpResponse.BodyHandlers.ofString());

        if (respuesta.statusCode() != 200) {
            throw new Exception("No se pudo cambiar la contraseña (" + respuesta.statusCode() + ")");
        }
    }

    public double convertirMagnitud(String tipoMagnitud, double valor, String unidadOrigen, String unidadDestino) throws Exception {
        String jsonPeticion = String.format(
            "{\"Valor\": %s, \"UnidadOrigen\": \"%s\", \"UnidadDestino\": \"%s\"}", 
            String.valueOf(valor).replace(",", "."), unidadOrigen, unidadDestino
        );

        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create(URL_BASE + "/conversiones/" + tipoMagnitud))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + this.tokenSesion)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPeticion))
                .build();

        HttpResponse<String> respuesta = clienteHttp.send(peticion, HttpResponse.BodyHandlers.ofString());

        if (respuesta.statusCode() == 200) {
            String resultadoStr = extraerValorJson(respuesta.body(), "valorConvertido", true);
            return Double.parseDouble(resultadoStr);
        } else {
            throw new Exception("Error en la conversión: Verifica las unidades ingresadas.");
        }
    }

    // Utilidad interna para parsear JSON sin librerías pesadas
    private String extraerValorJson(String json, String clave, boolean esNumero) {
        String patron = esNumero 
            ? "\"" + clave + "\"\\s*:\\s*([0-9\\.]+)" 
            : "\"" + clave + "\"\\s*:\\s*\"([^\"]+)\"";
        Matcher buscador = Pattern.compile(patron, Pattern.CASE_INSENSITIVE).matcher(json);
        return buscador.find() ? buscador.group(1) : "0";
    }
}