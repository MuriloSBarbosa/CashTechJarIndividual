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
import com.github.britooo.looca.api.group.rede.RedeParametros;
import com.github.britooo.looca.api.group.sistema.Sistema;
import org.example.repositories.MaquinaRepository;
import org.example.repositories.ProcessosRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author murilo
 */
public class MaquinaService {

    Looca looca = new Looca();
    Sistema sistema = looca.getSistema();
    Memoria memoria = looca.getMemoria();
    Processador processador = looca.getProcessador();
    DiscoGrupo grupoDeDiscos = looca.getGrupoDeDiscos();
    List<Volume> volumes = grupoDeDiscos.getVolumes();
    Rede rede = looca.getRede();
    ProcessoGrupo grupoDeProcessos = looca.getGrupoDeProcessos();
    RedeParametros parametros = rede.getParametros();
    RedeInterfaceGroup redeInterfaceGroup = rede.getGrupoDeInterfaces();
    List<RedeInterface> redeInterfaces = redeInterfaceGroup.getInterfaces();

    MaquinaRepository maquinaRepository = new MaquinaRepository();
    ProcessosRepository processoRepository = new ProcessosRepository();



    public void executarCadastro(Integer empresaId) {
        maquinaRepository.cadastrarSistema(sistema);
        maquinaRepository.cadastrarEndereco();
        maquinaRepository.cadastrarMaquina(parametros, empresaId);

        maquinaRepository.cadastrarProcessador(processador.getNome(), processador.getId(),processador.getFrequencia(),
                processador.getNumeroCpusFisicas(), processador.getNumeroCpusLogicas());

        maquinaRepository.cadastrarMemoria(memoria.getTotal());

        maquinaRepository.cadastrarDisco(volumes);

        Optional<RedeInterface> optRedeInterface = redeInterfaces.stream().max(Comparator.comparingLong(RedeInterface::getBytesEnviados));

        RedeInterface redeInterface = optRedeInterface.get();

        maquinaRepository.cadastrarInterfaceRede(redeInterface);

        // =============== Cadastrar Processos permitidos ================
        // Lista de processos permitidos são os primeiros processos que carrega
        // Passar esses processos para apenas o nome
        List<String> nomeProcessosPermitidos = grupoDeProcessos.getProcessos().stream()
                .map(Processo::getNome)
                .distinct().toList();

        // Verificar se já está cadastrado:
        List<String> processosPermitidosBanco = processoRepository.processosPermitidos(empresaId);

        List<String> processosFiltrados = nomeProcessosPermitidos.stream()
                .filter(processo -> !processosPermitidosBanco.contains(processo))
                .collect(Collectors.toList());

        if (!processosFiltrados.isEmpty()) {
            processoRepository.cadastrarProcessosPermitidosPadrao(processosFiltrados, empresaId);
        }
        // ==================================================================
    }

    public Integer identificarMaquina() {
        List<Integer> listaID = maquinaRepository.buscarIdMaquina(rede.getParametros().getHostName());
        return listaID.get(0);
    }
}
