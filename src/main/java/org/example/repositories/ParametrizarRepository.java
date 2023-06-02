/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.example.repositories;

import org.example.jar.DataBase;
import org.example.models.Parametrizacao;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 *
 * @author kanaiama
 */
public class ParametrizarRepository {
    DataBase conexao = new DataBase();

    JdbcTemplate con = conexao.getConnection();

    public List<Parametrizacao> verParametrizacao(Integer empresaID) {
        return con.query(
                "select qtd_cpu_max, "
                        + "qtd_bytes_enviado_max, "
                        + "qtd_bytes_recebido_max, "
                        + "qtd_memoria_max, "
                        + "qtd_disco_max "
                        + "from Parametrizacao where empresa_id = ?",
                new BeanPropertyRowMapper<>(Parametrizacao.class), empresaID);
    }
}
