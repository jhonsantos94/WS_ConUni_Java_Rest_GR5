/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package ec.edu.monster;

import ec.edu.monster.controlador.ControladorAplicacion;
import ec.edu.monster.modelos.ModeloClienteRest;
import ec.edu.monster.vistas.LoginVista;

/**
 *
 * @author ariel
 */
public class Clicon {

    public static void main(String[] args) {
        
        ControladorAplicacion controladorCentral = new ControladorAplicacion();
        
        LoginVista vistaLogin = new LoginVista(controladorCentral);
        
        vistaLogin.iniciar();
    }
}
