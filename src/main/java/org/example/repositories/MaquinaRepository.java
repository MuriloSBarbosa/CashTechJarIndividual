/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.example.repositories;

import com.github.britooo.looca.api.group.discos.DiscoGrupo;
import com.github.britooo.looca.api.group.discos.Volume;
import com.github.britooo.looca.api.group.memoria.Memoria;
import com.github.britooo.looca.api.group.processador.Processador;
import com.github.britooo.looca.api.group.rede.RedeInterface;
import com.github.britooo.looca.api.group.rede.RedeParametros;
import com.github.britooo.looca.api.group.sistema.Sistema;
import org.example.jar.DataBase;
import org.example.jar.DataBaseDocker;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import java.util.List;

/**
 * @author murilo
 */
public class MaquinaRepository {

    DataBase conexao = new DataBase();
    RedeParametros redeParametros;

    DataBaseDocker conexaoDocker = new DataBaseDocker();

    public JdbcTemplate conDock = conexaoDocker.getConnection();

    public JdbcTemplate con = conexao.getConnection();

    public void cadastrarSistema(Sistema sistema) {
        String script;
            // Query do SQL
            script = "INSERT INTO Sistema (nome,fabricante,arquitetura) VALUES"
                    + "(?,?,?)";

        conDock.update(script, sistema.getSistemaOperacional(), sistema.getFabricante(), sistema.getArquitetura());

        con.update(script,
                sistema.getSistemaOperacional(), sistema.getFabricante(), sistema.getArquitetura());
    }

    public void cadastrarEndereco() {
        String script = "INSERT INTO Endereco (rua, bairro, numero, cep) VALUES (NULL, NULL, NULL, NULL)";

        con.update(script);
    }

    public void cadastrarInterfaceRede(RedeInterface redeDado) {
        String script = "INSERT INTO NetworkInterface (nome,nome_exibicao,ipv4,ipv6,mac,caixa_eletronico_id) " +
                "VALUES (?,?,?,?,?,(SELECT TOP 1 id FROM CaixaEletronico ORDER BY id DESC))";
        con.update(
                script,
                redeDado.getNome(),
                redeDado.getNomeExibicao(),
                redeDado.getEnderecoIpv4().get(0),
                redeDado.getEnderecoIpv6().get(0),
                redeDado.getEnderecoMac()
        );

        String scriptDocker = "INSERT INTO NetworkInterface (nome,nome_exibicao,ipv4,ipv6,mac,caixa_eletronico_id)" +
                " VALUES (?,?,?,?,?,(SELECT id FROM CaixaEletronico ORDER BY id DESC LIMIT 1))";

        conDock.update(scriptDocker,
                redeDado.getNome(),
                redeDado.getNomeExibicao(),
                redeDado.getEnderecoIpv4().get(0),
                redeDado.getEnderecoIpv6().get(0),
                redeDado.getEnderecoMac());
    }

    public void cadastrarMaquina(RedeParametros parametros, Integer empresa_id) {
        // Query do SQL
        String script = "INSERT INTO CaixaEletronico (identificador, situacao, empresa_id, endereco_id, sistema_id)"
                + "VALUES (?, 'ativo', ?, (SELECT TOP 1 id FROM endereco ORDER BY id DESC),"
                + "(SELECT TOP 1 id FROM Sistema ORDER BY id DESC))";

        String scriptDocker = "INSERT INTO CaixaEletronico "
                + "(id, identificador, situacao, empresa_id, endereco_id, sistema_id) "
                + "VALUES (NULL,?,'ativo', ?, null,"
                + "(select id from Sistema order by id desc limit 1));";

        con.update(script,
                parametros.getHostName(), empresa_id);

        conDock.update(scriptDocker, parametros.getHostName(), empresa_id);


    }

    public List<Integer> buscarIdMaquina(String nomeMaquina) {
        return con.query("select id from CaixaEletronico where identificador = ?",
                new SingleColumnRowMapper(Integer.class), nomeMaquina);
    }

    public void cadastrarComponente(Processador processador, Memoria memoria, DiscoGrupo discoGrupo, String tipo) {

        String scriptSelect = "(SELECT TOP 1 id FROM CaixaEletronico ORDER BY id DESC)";
        String script = "INSERT INTO Componente"
                + "(tipo,nome,modelo,serie,frequencia,qtd_cpu_logica,"
                + "qtd_cpu_fisica,qtd_maxima,qtd_disponivel,ponto_montagem,sistema_arquivos,caixa_eletronico_id)"
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?,(SELECT TOP 1 id FROM CaixaEletronico ORDER BY id DESC))";


        String scriptSelectDocker = "(SELECT id FROM CaixaEletronico ORDER BY id DESC LIMIT 1)";
        String scriptDocker = "INSERT INTO Componente"
                + "(tipo,nome,modelo,serie,frequencia,qtd_cpu_logica,"
                + "qtd_cpu_fisica,qtd_maxima,qtd_disponivel,ponto_montagem,sistema_arquivos,caixa_eletronico_id)"
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?,(SELECT id FROM CaixaEletronico ORDER BY id DESC LIMIT 1))";

        switch (tipo) {

            case "processador":
                con.update(script,
                        tipo,// tipo
                        processador.getNome(), //nome
                        null,// modelo
                        processador.getId(),//serie
                        processador.getFrequencia(),//frequencia
                        processador.getNumeroCpusLogicas(),// qtd_cpu_logica
                        processador.getNumeroCpusFisicas(),// qtd_cpu_fisica
                        null,// qtd_maxima
                        null,//qtd_disponivel
                        null,//ponto_montagem
                        null);// sistemas_arquivos;

                conDock.update(scriptDocker,
                        tipo,// tipo
                        processador.getNome(), //nome
                        null,// modelo
                        processador.getId(),//serie
                        processador.getFrequencia(),//frequencia
                        processador.getNumeroCpusLogicas(),// qtd_cpu_logica
                        processador.getNumeroCpusFisicas(),// qtd_cpu_fisica
                        null,// qtd_maxima
                        null,//qtd_disponivel
                        null,//ponto_montagem
                        null);// sistemas_arquivos;
                break;

            case "memoria":
                con.update(script,
                        tipo,// tipo
                        null,// nome
                        null,// modelo
                        null,// serie
                        null,// frequencia
                        null,// q
                        null,
                        memoria.getTotal(),
                        null,
                        null,
                        null
                );
                conDock.update(scriptDocker,
                        tipo,// tipo
                        null,// nome
                        null,// modelo
                        null,// serie
                        null,// frequencia
                        null,// q
                        null,
                        memoria.getTotal(),
                        null,
                        null,
                        null
                );
                break;

            case "disco":
                String values = "";
                String valuesDocker = "";

                List<Volume> volumes = discoGrupo.getVolumes();
                for (int i = 0; i < volumes.size(); i++) {
                    if (i == volumes.size() - 1) {
                        values += String.format("('%s','%s',null,null,null,null,null,%d,%d,'%s','%s',%s)",
                                tipo, // tipo   
                                volumes.get(i).getNome(), //nome
                                // modelo
                                // serie
                                // frequencia
                                // qtd_cpu_logica
                                // qtd_cpu_fisica
                                volumes.get(i).getTotal(),//qtd_maxima
                                volumes.get(i).getDisponivel(),// qtd_disponivel
                                volumes.get(i).getPontoDeMontagem().endsWith("\\") ? volumes.get(i).getPontoDeMontagem() + "\\" : volumes.get(i).getPontoDeMontagem(), // ponto_montagem
                                volumes.get(i).getTipo(),
                                scriptSelect); // sistema_arquivos;

                    } else {
                        values += String.format("('%s','%s',null,null,null,null,null,%d,%d,'%s','%s',%s), ",
                                tipo, // tipo
                                volumes.get(i).getNome(), //nome
                                // modelo
                                // serie
                                // frequencia
                                // qtd_cpu_logica
                                // qtd_cpu_fisica
                                volumes.get(i).getTotal(),//qtd_maxima
                                volumes.get(i).getDisponivel(),// qtd_disponivel
                                volumes.get(i).getPontoDeMontagem().endsWith("\\") ? volumes.get(i).getPontoDeMontagem() + "\\" : volumes.get(i).getPontoDeMontagem(), // ponto_montagem
                                volumes.get(i).getTipo(),
                                scriptSelect);
                    }
                }

                for (int i = 0; i < volumes.size(); i++) {
                    if (i == volumes.size() - 1) {
                        valuesDocker += String.format("('%s','%s',null,null,null,null,null,%d,%d,'%s','%s',%s)",
                                tipo, // tipo
                                volumes.get(i).getNome(), //nome
                                // modelo
                                // serie
                                // frequencia
                                // qtd_cpu_logica
                                // qtd_cpu_fisica
                                volumes.get(i).getTotal(),//qtd_maxima
                                volumes.get(i).getDisponivel(),// qtd_disponivel
                                volumes.get(i).getPontoDeMontagem().endsWith("\\") ? volumes.get(i).getPontoDeMontagem() + "\\" : volumes.get(i).getPontoDeMontagem(), // ponto_montagem
                                volumes.get(i).getTipo(),
                                scriptSelectDocker); // sistema_arquivos;

                    } else {
                        valuesDocker += String.format("('%s','%s',null,null,null,null,null,%d,%d,'%s','%s',%s), ",
                                tipo, // tipo
                                volumes.get(i).getNome(), //nome
                                // modelo
                                // serie
                                // frequencia
                                // qtd_cpu_logica
                                // qtd_cpu_fisica
                                volumes.get(i).getTotal(),//qtd_maxima
                                volumes.get(i).getDisponivel(),// qtd_disponivel
                                volumes.get(i).getPontoDeMontagem().endsWith("\\") ? volumes.get(i).getPontoDeMontagem() + "\\" : volumes.get(i).getPontoDeMontagem(), // ponto_montagem
                                volumes.get(i).getTipo(),
                                scriptSelectDocker);
                    }
                }

                String scriptDisco = String.format("INSERT INTO Componente"
                        + " (tipo,nome,modelo,serie,frequencia,qtd_cpu_logica,"
                        + " qtd_cpu_fisica,qtd_maxima,qtd_disponivel,ponto_montagem,sistema_arquivos,caixa_eletronico_id)"
                        + " VALUES %s;", values);

                String scriptDiscoDocker = String.format("INSERT INTO Componente"
                        + " (tipo,nome,modelo,serie,frequencia,qtd_cpu_logica,"
                        + " qtd_cpu_fisica,qtd_maxima,qtd_disponivel,ponto_montagem,sistema_arquivos,caixa_eletronico_id)"
                        + " VALUES %s;", valuesDocker);


                conDock.update(scriptDiscoDocker);
                con.update(scriptDisco);

                break;

            default:
                System.out.println("Componente Inválido");
                System.out.println(tipo);
                break;
        }
    }
    public List<String> buscarIdentificador(Integer idAtm) {
        return con.query("SELECT identificador FROM CaixaEletronico WHERE id = " + idAtm, new SingleColumnRowMapper<>(String.class));
    }

}
