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
    DataBaseDocker conexaoDocker = new DataBaseDocker();
    public JdbcTemplate conDock = conexaoDocker.getConnection();

    public JdbcTemplate con = conexao.getConnection();

    public void cadastrarSistema(Sistema sistema) {
        String script = String.format("INSERT INTO Sistema (nome,fabricante,arquitetura) VALUES "
                + "('%s','%s','%d')", sistema.getSistemaOperacional(), sistema.getFabricante(), sistema.getArquitetura());

        conDock.update(script);
        con.update(script);
    }

    public void cadastrarEndereco() {
        String script = "INSERT INTO Endereco (rua, bairro, numero, cep) VALUES (NULL, NULL, NULL, NULL)";

        con.update(script);
    }

    public void cadastrarInterfaceRede(RedeInterface redeDado) {
        String script = String.format("INSERT INTO NetworkInterface (nome,nome_exibicao,ipv4,ipv6,mac,caixa_eletronico_id) " +
                        "VALUES ('%s','%s','%s','%s','%s',",
                redeDado.getNome(),
                redeDado.getNomeExibicao(),
                redeDado.getEnderecoIpv4().get(0),
                redeDado.getEnderecoIpv6().get(0),
                redeDado.getEnderecoMac());

        con.update(script + "(SELECT TOP 1 id FROM CaixaEletronico ORDER BY id DESC))");
        conDock.update(script + "(SELECT id FROM CaixaEletronico ORDER BY id DESC LIMIT 1))");
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

    public String scriptSelectCaixaEletronico(String type) {
        if (type.equals("docker")) {
            return "(SELECT id FROM CaixaEletronico ORDER BY id DESC LIMIT 1)";
        } else {
            return "(SELECT TOP 1 id FROM CaixaEletronico ORDER BY id DESC)";
        }
    }

    public void cadastrarProcessador(String nome, String id, Long frequencia, Integer qtdCpuLogica, Integer qtdCpuFisica) {
        String script = String.format(
                "INSERT INTO Componente (tipo,nome,serie,frequencia,qtd_cpu_logica,qtd_cpu_fisica,caixa_eletronico_id) " +
                        "VALUES ('processador','%s','%s',%d,%d,%d,",
                nome, id, frequencia, qtdCpuLogica, qtdCpuFisica);

        con.update(script + scriptSelectCaixaEletronico("") + ")");
        conDock.update(script + scriptSelectCaixaEletronico("docker") + ")");
    }

    public void cadastrarMemoria(Long qtdMaxima) {

        String script = String.format(
                "INSERT INTO Componente (tipo,qtd_maxima,caixa_eletronico_id) " +
                        "VALUES ('memoria',%d,", qtdMaxima);

        con.update(script + scriptSelectCaixaEletronico("") + ")");
        conDock.update(script + scriptSelectCaixaEletronico("docker") + ")");
    }

    public void cadastrarDisco(List<Volume> volumes) {

        String scriptSelect = scriptSelectCaixaEletronico("");
        String scriptSelectDocker = scriptSelectCaixaEletronico("docker");

        StringBuilder values = new StringBuilder();
        StringBuilder valuesDocker = new StringBuilder();

        for (int i = 0; i < volumes.size(); i++) {
            values.append(String.format("('%s','%s',%d,%d,'%s','%s',%s)",
                    "disco", // tipo
                    volumes.get(i).getNome(), //nome
                    volumes.get(i).getTotal(),//qtd_maxima
                    volumes.get(i).getDisponivel(),// qtd_disponivel
                    volumes.get(i).getPontoDeMontagem().endsWith("\\") ? volumes.get(i).getPontoDeMontagem() + "\\" : volumes.get(i).getPontoDeMontagem(), // ponto_montagem
                    volumes.get(i).getTipo(),// sistema_arquivos;
                    i == volumes.size() - 1 ? scriptSelect : scriptSelect + ", " // caixa_eletronico_id
            ));

            valuesDocker.append(String.format("('%s','%s',%d,%d,'%s','%s',%s)",
                    "disco", // tipo
                    volumes.get(i).getNome(), //nome
                    volumes.get(i).getTotal(),//qtd_maxima
                    volumes.get(i).getDisponivel(),// qtd_disponivel
                    volumes.get(i).getPontoDeMontagem().endsWith("\\") ? volumes.get(i).getPontoDeMontagem() + "\\" : volumes.get(i).getPontoDeMontagem(), // ponto_montagem
                    volumes.get(i).getTipo(),// sistema_arquivos;
                    i == volumes.size() - 1 ? scriptSelectDocker : scriptSelectDocker + ", " // caixa_eletronico_id
            ));
        }

        String scriptDisco = "INSERT INTO Componente"
                + " (tipo,nome,qtd_maxima, qtd_disponivel, ponto_montagem,sistema_arquivos,caixa_eletronico_id)"
                + " VALUES ";

        con.update(scriptDisco + values);
        conDock.update(scriptDisco + valuesDocker);
    }

    public List<String> buscarIdentificador(Integer idAtm) {
        return con.query("SELECT identificador FROM CaixaEletronico WHERE id = " + idAtm, new SingleColumnRowMapper<>(String.class));
    }
}
