package org.example.services;

import com.github.britooo.looca.api.group.discos.Volume;
import com.github.britooo.looca.api.group.memoria.Memoria;
import com.github.britooo.looca.api.group.processador.Processador;
import com.github.britooo.looca.api.util.Conversor;
import org.example.configuration.Slack;
import org.example.models.Parametrizacao;
import org.example.repositories.NotificacaoRepository;
import org.example.repositories.ParametrizarRepository;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.List;

public class AnaliseMetricaService {

    ParametrizarRepository parametrizarRepository = new ParametrizarRepository();
    NotificacaoRepository notificacaoRepository = new NotificacaoRepository();

    public void verificarMetricas(Memoria memoria, Processador processador,
                                  Volume volume, Long bytesRecebidos, Long bytesEnviados, LocalDateTime dtNotificacao,
                                  Integer idEmpresaUsuario, String identificador) {

        List<Parametrizacao> parametrizacao
                = parametrizarRepository.verParametrizacao(idEmpresaUsuario);

        Parametrizacao usuario = parametrizacao.get(0);

        Boolean isAlerta = false;
        Boolean hasNotificacao = false;
        String frase = "";

        String memoriaConvertida = Conversor.formatarBytes(memoria.getDisponivel());
        String volumeConvertido = Conversor.formatarBytes(volume.getDisponivel());
        String bytesRecebidosConvertido = Conversor.formatarBytes(bytesRecebidos);
        String bytesEnviadosConvertido = Conversor.formatarBytes(bytesEnviados);

        String verde = "\033[0;32m";
        String vermelho = "\033[0;31m";
        String azul = "\033[0;34m";
        String roxo = "\033[0;35m";
        String ciano = "\033[0;36m";
        String amarelo = "\033[0;33m";
        String ANSI_RESET = "\u001B[0m";

        //Verificando métricas de Memória
        if (memoria.getDisponivel() >= (usuario.getQtd_memoria_max() * 0.75)) {
            hasNotificacao = true;
            isAlerta = true;
            frase += ("\n:small_orange_diamond: Uso de memória atingindo o limite! Disponível: " + memoriaConvertida);
        } else if (memoria.getDisponivel() >= (usuario.getQtd_memoria_max() * 0.50)) {
            hasNotificacao = true;
            frase += ("\n:small_blue_diamond: Uso de memória na metade da capacidade total! Disponível: " + memoriaConvertida);
        }

        //Verificando métricas de CPU
        if (processador.getUso() >= (usuario.getQtd_cpu_max() * 0.75)) {
            hasNotificacao = true;
            isAlerta = true;
            frase += ("\n:small_orange_diamond: Uso de processador atingindo o limite! Disponível: " + processador.getUso());
        } else if (processador.getUso() >= (usuario.getQtd_cpu_max() * 0.5)) {
            hasNotificacao = true;
            frase += ("\n:small_blue_diamond: Uso de processador na metade da capacidade total! Disponível: " + processador.getUso());
        }

        //Verificando métricas de Disco/Volume
        if (volume.getDisponivel() >= (usuario.getQtd_disco_max() * 0.75)) {
            hasNotificacao = true;
            isAlerta = true;
            frase += ("\n:small_orange_diamond: Uso de disco/volume atingindo o limite! Disponível: " + volumeConvertido);
        } else if (volume.getDisponivel() >= (usuario.getQtd_disco_max() * 0.5)) {
            hasNotificacao = true;
            frase += ("\n:small_blue_diamond: Uso de disco/volume na metade da capacidade total! Disponível: " + volumeConvertido);
        }

        //Verificando métricas de bytes enviados de Rede
        if (bytesEnviados >= (usuario.getQtd_bytes_enviado_max() * 0.75)) {
            hasNotificacao = true;
            isAlerta = true;
            frase += ("\n:small_orange_diamond: Uso de bytes enviados atingindo o limite! Disponível: " + bytesEnviadosConvertido);
        } else if (bytesEnviados >= (usuario.getQtd_bytes_enviado_max() * 0.5)) {
            hasNotificacao = true;
            frase += ("\n:small_blue_diamond: Uso de bytes enviados na metade da capacidade total! Disponível: " + bytesEnviadosConvertido);
        }

        //Verificando métricas de bytes recebidos da Rede
        if (bytesRecebidos >= (usuario.getQtd_bytes_recebido_max() * 0.75)) {
            hasNotificacao = true;
            isAlerta = true;
            frase += ("\n:small_orange_diamond: Uso de bytes recebidos atingindo o limite! " + bytesRecebidosConvertido);
        } else if (bytesRecebidos >= (usuario.getQtd_bytes_recebido_max() * 0.5)) {
            hasNotificacao = true;
            frase += ("\n:small_blue_diamond: Uso de bytes recebidos na metade da capacidade total! " + bytesRecebidosConvertido);
        }

        if (hasNotificacao) {
            // Regex para tirar :small_blue_diamond: e :small_orange_diamond: da frase
            String regex = "(:small_blue_diamond:|:small_orange_diamond:)";
            String alertaAscii = """
                         _      _                 _             _\s
                        / \\    | |   ___   _ __  | |_    __ _  | |
                       / _ \\   | |  / _ \\ | '__| | __|  / _` | | |
                      / ___ \\  | | |  __/ | |    | |_  | (_| | |_|
                     /_/   \\_\\ |_|  \\___| |_|     \\__|  \\__,_| (_)
                    """;

            String icon = amarelo + "Alerta: ";
            String printFrase = frase.replaceAll(regex, (icon + ANSI_RESET));
            // $1 signfica que vai pegar o primeiro grupo de captura, que no caso é o :small_blue_diamond: ou :small_orange_diamond:
            String setas = amarelo + "=".repeat(67) + ANSI_RESET;

            System.out.println(setas + vermelho + alertaAscii + ANSI_RESET + printFrase + "\n" + setas);

            notificacaoRepository.enviarNotificacao(frase, idEmpresaUsuario, dtNotificacao);


            if (isAlerta) {
                //adicionar texto antes da frase
                frase = "\n\n:warning::alphabet-yellow-a::alphabet-yellow-l::alphabet-yellow-e::alphabet-yellow-r::alphabet-yellow-t::alphabet-yellow-a::warning:\n" + frase;
            }

            String setasSlack = "-".repeat(80);
            String identificadorMsg = ":large_blue_diamond: :mechanic: Caixa eletrônico: " + identificador + "\n";
            frase = setasSlack + "\n" + identificadorMsg + frase + "\n" + setasSlack;

            try {
                JSONObject json = new JSONObject();
                json.put("text", frase);
                Slack.sendMessage(json);
            } catch (Exception e) {
                System.out.println("Erro ao enviar mensagem para o Slack");
            }
        }


    }
}
