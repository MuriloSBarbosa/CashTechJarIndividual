/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.example.jar;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author murilo
 */
public class DataBaseDocker {

    private String porta = "3307";

    private String container = "localhost";

    private String bancoDeDados = "cashtech";

    private String login = "root";

    private String senha = "root";

    private JdbcTemplate connection;

    public DataBaseDocker() {
        BasicDataSource dataSource = new BasicDataSource();

        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");

        dataSource.setUrl(String.format("jdbc:mysql://%s:%s/%s?useTimezone=true&serverTimezone=UTC", container, porta, bancoDeDados));

        dataSource.setUsername(login);
        dataSource.setPassword(senha);

        this.connection = new JdbcTemplate(dataSource);

    }

    public JdbcTemplate getConnection() {

        return connection;

    }
}
