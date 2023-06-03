/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.example.services;



import org.example.jar.Main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author PC
 */
public class GerarLog {

    public void login(String usuario, Boolean isErro) {

        File login = new File("log-login.txt");

        //throws IOException e try e catch 
        if (!login.exists()) {
            try {
                login.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        String frase = "Login realizado com sucesso do usuário: " + usuario;

        if (isErro) {
            frase = "Erro ao realizar login do usuário : "+ usuario;
        }

        ZonedDateTime horarioBrasilia = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"));
        LocalDateTime dtMetrica = horarioBrasilia.toLocalDateTime();


        List<String> linhas = new ArrayList();
        linhas.add(dtMetrica.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " - " + frase);

        try {
            Files.write(Paths.get(login.getPath()), linhas, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
