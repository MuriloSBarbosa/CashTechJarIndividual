/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.example.repositories;

import org.example.jar.DataBase;
import org.example.jar.DataBaseDocker;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;

/**
 *
 * @author PC
 */
public class NotificacaoRepository {

    DataBase dbAzure = new DataBase();
    DataBaseDocker dbDocker = new DataBaseDocker();

    JdbcTemplate conAzure = dbAzure.getConnection();
    JdbcTemplate conDocker = dbDocker.getConnection();

    public void enviarNotificacao(String frase, Integer empresaId, LocalDateTime dtNotificacao) {

        String script = "insert into Notificacao (descricao, dt_notificacao,empresa_id) values (?, ? ,?)";

        conAzure.update(script,
                frase, dtNotificacao, empresaId);

        conDocker.update(script,
                frase, dtNotificacao, empresaId);

    }

}
