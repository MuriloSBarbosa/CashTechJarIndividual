/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.example.services;

import com.github.britooo.looca.api.core.Looca;
import com.github.britooo.looca.api.group.discos.DiscoGrupo;
import com.github.britooo.looca.api.group.discos.Volume;
import com.github.britooo.looca.api.group.memoria.Memoria;
import com.github.britooo.looca.api.group.processador.Processador;
import com.github.britooo.looca.api.group.processos.Processo;
import com.github.britooo.looca.api.group.processos.ProcessoGrupo;
import com.github.britooo.looca.api.group.rede.Rede;
import com.github.britooo.looca.api.group.rede.RedeInterface;
import com.github.britooo.looca.api.group.rede.RedeInterfaceGroup;
import com.github.britooo.looca.api.group.sistema.Sistema;
import com.github.britooo.looca.api.util.Conversor;
import org.example.Util.ComparadorUsoProcesso;
import org.example.configuration.Slack;
import org.example.configuration.SlackRepository;
import org.example.models.Parametrizacao;
import org.example.repositories.*;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * @author murilo
 */
public class MonitorarService {

    MonitorarRepository monitorarRepository = new MonitorarRepository();
    ProcessosRepository processosRepository = new ProcessosRepository();
    MaquinaRepository maquinaRepository = new MaquinaRepository();

    AnaliseMetricaService analiseMetricaService = new AnaliseMetricaService();
    private Integer countSeconds = 6;

    public void monitorarHardware(Integer idAtm, Integer idEmpresaUsuario) {

        Integer idMemoria = monitorarRepository.verIdComponente(idAtm, "memoria").get(0);
        Integer idProcessador = monitorarRepository.verIdComponente(idAtm, "processador").get(0);
        Integer idRede = monitorarRepository.verIdRede(idAtm).get(0);
        Integer idSistema = monitorarRepository.verIdSistema(idAtm).get(0);
        List<Map<String, Object>> idsVolume = monitorarRepository.verIdComponenteVolume(idAtm);
        String identificador = maquinaRepository.buscarIdentificador(idAtm).get(0);
        SlackRepository slackRepository = new SlackRepository();
        slackRepository.pegarUrl();

        String verde = "\033[0;32m";
        String vermelho = "\033[0;31m";
        String azul = "\033[0;34m";
        String roxo = "\033[0;35m";
        String ciano = "\033[0;36m";
        String amarelo = "\033[0;33m";

        String ANSI_RESET = "\u001B[0m";

        new Timer().scheduleAtFixedRate(new TimerTask() {
            Long bytesRecebidosAntigo = null;
            Long bytesEnviadosAntigo = null;

            @Override
            public void run() {
                Looca looca = new Looca();
                Sistema sistema = looca.getSistema();
                Memoria memoria = looca.getMemoria();
                Processador processador = looca.getProcessador();
                DiscoGrupo grupoDeDiscos = looca.getGrupoDeDiscos();
                List<Volume> volumes = grupoDeDiscos.getVolumes();
                Rede rede = looca.getRede();
                ProcessoGrupo processoGrupo = looca.getGrupoDeProcessos();
                List<Processo> processos = processoGrupo.getProcessos();
                RedeInterfaceGroup redeInterfaceGroup = rede.getGrupoDeInterfaces();
                List<RedeInterface> redeInterfaces = redeInterfaceGroup.getInterfaces();

                ZonedDateTime horarioBrasilia = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"));
                LocalDateTime dtMetrica = horarioBrasilia.toLocalDateTime();

                monitorarRepository.enviarSistema(idAtm, sistema, dtMetrica, idSistema);

                monitorarRepository.enviarMetrica(idMemoria, dtMetrica, memoria.getEmUso());
                monitorarRepository.enviarMetrica(idProcessador, dtMetrica, processador.getUso());

                Volume volumeMonitorado = null;
                for (Map<String, Object> volume : idsVolume) {
                    Integer idVolume = (Integer) volume.get("id");
                    String pontoMontagemTodo = (String) volume.get("ponto_montagem");
                    String pontoMontagem = pontoMontagemTodo.endsWith("\\") ? pontoMontagemTodo.substring(0, pontoMontagemTodo.lastIndexOf("\\")) : pontoMontagemTodo;

                    Optional<Volume> volumeOptional = volumes.stream().filter(v -> v.getPontoDeMontagem().equals(pontoMontagem)).findFirst();
                    volumeMonitorado = volumeOptional.get();
                    monitorarRepository.enviarMetrica(idVolume, dtMetrica, volumeMonitorado.getDisponivel());
                }

                Optional<RedeInterface> optRedeInterface = redeInterfaces.stream().filter(
                        r -> r.getBytesEnviados() > 0 || r.getBytesRecebidos() > 0).findFirst();

                RedeInterface redeInterface = optRedeInterface.get();

                processos.sort(new ComparadorUsoProcesso());

                List<Processo> topVinteProcessos = new ArrayList<>();

                for (int i = 0; i < processos.size(); i++) {
                    if (i < 20) {
                        topVinteProcessos.add(processos.get(i));
                    } else {
                        break;
                    }
                }


                String linha = "=".repeat(50);
                System.out.println("\n" + linha);
                System.out.println(roxo + "Métricas atuais " + amarelo + "(" + dtMetrica.getHour() + ":" + dtMetrica.getMinute() + ":" + dtMetrica.getSecond()
                        + "):");

                System.out.println(azul + "Memória: " + ANSI_RESET + Conversor.formatarBytes(memoria.getEmUso()));
                System.out.println(String.format(azul + "Processador: " + ANSI_RESET + "%.2f%%", processador.getUso()));
                System.out.println(azul + "Rede: " + ANSI_RESET + Conversor.formatarBytes(redeInterface.getBytesRecebidos()) + " bytes recebidos");
                System.out.println(azul + "Rede: " + ANSI_RESET + Conversor.formatarBytes(redeInterface.getBytesEnviados()) + " bytes enviados");
                for (Volume volume : volumes) {
                    System.out.println(azul + "Volume (" + volume.getNome() + "): " + ANSI_RESET + Conversor.formatarBytes(volume.getDisponivel()));
                }
                System.out.println(linha);

                Long bytesRecebidos = bytesRecebidosAntigo == null ? 0 : redeInterface.getBytesRecebidos() - bytesRecebidosAntigo;
                Long bytesEnviados = bytesEnviadosAntigo == null ? 0 : redeInterface.getBytesEnviados() - bytesEnviadosAntigo;

                processosRepository.cadastrarProcessosAgora(idAtm, topVinteProcessos, dtMetrica);
                monitorarRepository.enviarMetricaRede(idRede,
                        bytesRecebidos,
                        bytesEnviados, dtMetrica);

                if (isTwentySeconds()) {
                    analiseMetricaService.verificarMetricas(memoria, processador, volumeMonitorado,
                            bytesRecebidos, bytesEnviados, dtMetrica, idEmpresaUsuario, identificador);
                }

                bytesRecebidosAntigo = redeInterface.getBytesRecebidos();
                bytesEnviadosAntigo = redeInterface.getBytesEnviados();
            }

        }, 0, 3000);
    }


    public Boolean isTwentySeconds() {
        countSeconds++;
        if (countSeconds == 7) {
            countSeconds = 0;
            return true;
        }
        return false;
    }
}
