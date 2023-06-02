/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.example.repositories;

import org.example.jar.DataBase;
import org.example.models.Usuario;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

/**
 *
 * @author murilo
 */
public class LoginRepository {

    DataBase conexao = new DataBase();
    public JdbcTemplate con = conexao.getConnection();

    
    public List<Usuario> verificarExisteUsuario(String usuario, String senha) {
        List<Usuario> lista = con.query("select * from Usuario where login = ? and senha = ?",
                new BeanPropertyRowMapper(Usuario.class), usuario, senha);

        
        return lista;
    }
   
    public List<Map<String, Object>> verificarMaquina(String hostName) {
        return con.queryForList("select * from CaixaEletronico where identificador = ?", hostName);
    }
}
