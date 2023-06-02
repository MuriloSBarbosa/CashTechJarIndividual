/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.example.services;


import com.github.britooo.looca.api.core.Looca;
import com.github.britooo.looca.api.group.rede.Rede;
import com.github.britooo.looca.api.group.rede.RedeParametros;
import org.example.models.Usuario;
import org.example.repositories.LoginRepository;

import java.util.List;
import java.util.Map;

/**
 *
 * @author murilo
 */
public class LoginService {

    Looca looca = new Looca();
    Rede rede = looca.getRede();
    RedeParametros parametros = rede.getParametros();
    
    String hostName = rede.getParametros().getHostName();

    LoginRepository executarLogin = new LoginRepository();

    public List<Usuario> verificarLogin(String usuario, String senha) {
        return executarLogin.verificarExisteUsuario(usuario, senha);
    }

    public Boolean hasMaquina() {
        List<Map<String, Object>> listaMaquina = executarLogin.verificarMaquina(hostName);
        return !listaMaquina.isEmpty();
    }
}
