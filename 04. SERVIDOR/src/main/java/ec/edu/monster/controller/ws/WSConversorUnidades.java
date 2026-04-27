package ec.edu.monster.controller.ws;

import ec.edu.monster.model.utilidades.enums.UnidadTemperatura;
import ec.edu.monster.model.utilidades.enums.UnidadMasa;
import ec.edu.monster.model.utilidades.enums.UnidadLongitud;
import ec.edu.monster.model.utilidades.ConversorTemperatura;
import ec.edu.monster.model.utilidades.ConversorMasa;
import ec.edu.monster.model.utilidades.ConversorLongitud;
import ec.edu.monster.model.servicios.ServicioConversor;
import ec.edu.monster.model.utilidades.mapeadores.UnidadMapper;
import ec.edu.monster.seguridad.AdministradorCredenciales;
import ec.edu.monster.seguridad.AdministradorTokens;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/conversor-unidades")
public class WSConversorUnidades {

    private final ServicioConversor<UnidadLongitud> servicioLongitud =
            new ServicioConversor<>(new ConversorLongitud());

    private final ServicioConversor<UnidadMasa> servicioMasa =
            new ServicioConversor<>(new ConversorMasa());

    private final ServicioConversor<UnidadTemperatura> servicioTemperatura =
            new ServicioConversor<>(new ConversorTemperatura());

    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(
            @QueryParam("usuario") String usuario,
            @QueryParam("contrasenia") String contrasenia) {

        if (usuario == null || usuario.isBlank() || contrasenia == null || contrasenia.isBlank()) {
            return construirRespuestaError(Response.Status.BAD_REQUEST, "Usuario y contrasenia son obligatorios");
        }

        if (AdministradorCredenciales.validarCredenciales(usuario, contrasenia)) {
            String token = AdministradorTokens.generarToken();
            return construirRespuestaOk("token", token);
        }

        return construirRespuestaError(Response.Status.UNAUTHORIZED, "Credenciales incorrectas");
    }

    @PUT
    @Path("/cambiarContrasenia")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cambiarContrasenia(
            @QueryParam("contraseniaActual") String contraseniaActual,
            @QueryParam("contraseniaNueva") String contraseniaNueva) {
        try {
            AdministradorCredenciales.cambiarContrasenia(contraseniaActual, contraseniaNueva);
            return construirRespuestaOk("mensaje", "Contrasenia actualizada correctamente");
        } catch (RuntimeException e) {
            return construirRespuestaError(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    @GET
    @Path("/convertirLongitud")
    @Produces(MediaType.APPLICATION_JSON)
    public Response convertirLongitud(
            @QueryParam("valor") Double valor,
            @QueryParam("unidadInicial") String unidadInicial,
            @QueryParam("unidadFinal") String unidadFinal) {
        try {
            validarDatosConversion(valor, unidadInicial, unidadFinal);
            UnidadLongitud origen = UnidadMapper.toLongitud(unidadInicial);
            UnidadLongitud destino = UnidadMapper.toLongitud(unidadFinal);

            double resultado = servicioLongitud.convertir(valor, origen, destino);
            return construirRespuestaOk("resultado", resultado);
        } catch (RuntimeException e) {
            return construirRespuestaError(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    @GET
    @Path("/convertirMasa")
    @Produces(MediaType.APPLICATION_JSON)
    public Response convertirMasa(
            @QueryParam("valor") Double valor,
            @QueryParam("unidadInicial") String unidadInicial,
            @QueryParam("unidadFinal") String unidadFinal) {
        try {
            validarDatosConversion(valor, unidadInicial, unidadFinal);
            UnidadMasa origen = UnidadMapper.toMasa(unidadInicial);
            UnidadMasa destino = UnidadMapper.toMasa(unidadFinal);

            double resultado = servicioMasa.convertir(valor, origen, destino);
            return construirRespuestaOk("resultado", resultado);
        } catch (RuntimeException e) {
            return construirRespuestaError(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    @GET
    @Path("/convertirTemperatura")
    @Produces(MediaType.APPLICATION_JSON)
    public Response convertirTemperatura(
            @QueryParam("valor") Double valor,
            @QueryParam("unidadInicial") String unidadInicial,
            @QueryParam("unidadFinal") String unidadFinal) {
        try {
            validarDatosConversion(valor, unidadInicial, unidadFinal);
            UnidadTemperatura origen = UnidadMapper.toTemperatura(unidadInicial);
            UnidadTemperatura destino = UnidadMapper.toTemperatura(unidadFinal);

            double resultado = servicioTemperatura.convertir(valor, origen, destino);
            return construirRespuestaOk("resultado", resultado);
        } catch (RuntimeException e) {
            return construirRespuestaError(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    private static void validarDatosConversion(Double valor, String unidadInicial, String unidadFinal) {
        if (valor == null || unidadInicial == null || unidadInicial.isBlank() || unidadFinal == null || unidadFinal.isBlank()) {
            throw new RuntimeException("Valor y unidades son obligatorios");
        }
    }

    private static Response construirRespuestaOk(String clave, String valor) {
        String json = "{\"" + escaparJson(clave) + "\":\"" + escaparJson(valor) + "\"}";
        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }

    private static Response construirRespuestaOk(String clave, double valor) {
        String json = "{\"" + escaparJson(clave) + "\":" + valor + "}";
        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }

    private static Response construirRespuestaError(Response.Status estado, String mensaje) {
        String json = "{\"mensaje\":\"" + escaparJson(mensaje) + "\"}";
        return Response.status(estado).entity(json).type(MediaType.APPLICATION_JSON).build();
    }

    private static String escaparJson(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
