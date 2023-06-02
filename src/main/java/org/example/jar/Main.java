package org.example.jar;


import org.example.models.*;
import org.example.services.*;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        KillProcessosService killProcessosService = new KillProcessosService();
        MaquinaService maquinaService = new MaquinaService();
        LoginService loginService = new LoginService();
        MonitorarService monitorarService = new MonitorarService();

        GerarLog gerarLog = new GerarLog();

        Scanner scanner = new Scanner(System.in);
        Scanner scannerSenha = new Scanner(System.in);

        String verde = "\033[0;32m";
        String vermelho = "\033[0;31m";
        String azul = "\033[0;34m";
        String roxo = "\033[0;35m";
        String ciano = "\033[0;36m";
        String amarelo = "\033[0;33m";

        String ANSI_RESET = "\u001B[0m";

        String usuario;
        String senha;
        List<Usuario> usuariosRetornado;

        System.out.println(verde + "Seja muito bem vindo a..." + ciano + "CashTech!" + ANSI_RESET);

        System.out.println(verde + "Versão específica de..." + ANSI_RESET);
        System.out.println(roxo + ".___  ___.  __    __  .______       __   __        ______    __      _______.   .___________. _______   ______  __    __  \n" +
                "|   \\/   | |  |  |  | |   _  \\     |  | |  |      /  __  \\  (_ )    /       |   |           ||   ____| /      ||  |  |  | \n" +
                "|  \\  /  | |  |  |  | |  |_)  |    |  | |  |     |  |  |  |  |/    |   (----`   `---|  |----`|  |__   |  ,----'|  |__|  | \n" +
                "|  |\\/|  | |  |  |  | |      /     |  | |  |     |  |  |  |         \\   \\           |  |     |   __|  |  |     |   __   | \n" +
                "|  |  |  | |  `--'  | |  |\\  \\----.|  | |  `----.|  `--'  |     .----)   |          |  |     |  |____ |  `----.|  |  |  | \n" +
                "|__|  |__|  \\______/  | _| `._____||__| |_______| \\______/      |_______/           |__|     |_______| \\______||__|  |__| \n" +
                "                                                                                                                          " + ANSI_RESET);

        do {

            System.out.printf("Digite seu%s usuário %s%n", azul, ANSI_RESET);
            usuario = scanner.nextLine();

            System.out.printf("Digite sua%s senha %s\n", amarelo, ANSI_RESET);
            senha = scannerSenha.nextLine();

            System.out.println("Verificando login...\n");

            usuariosRetornado = loginService.verificarLogin(usuario, senha);

            //Importar a classe File para poder criar um arquivo
            if (usuario.isEmpty() || senha.isEmpty()) {
                System.out.println(vermelho + "Complete todos os campos!\n" + ANSI_RESET);
            } else if (usuariosRetornado.size() == 0) {
                System.out.println(vermelho + "Usuário não encontrado!\n" + ANSI_RESET);
                gerarLog.login(usuario, true);
            }
        } while (usuariosRetornado.size() == 0 || usuario.isEmpty() || senha.isEmpty());

        System.out.println(ciano + "Login efetuado com sucesso!" + ANSI_RESET);

        gerarLog.login(usuario, false);

        Usuario usuarioVerificado = usuariosRetornado.get(0);
        System.out.println(amarelo +
                "  ____                                        _               _         \n" +
                " |  _ \\                                      (_)             | |        \n" +
                " | |_) |   ___   _ __ ___    ______  __   __  _   _ __     __| |   ___  \n" +
                " |  _ <   / _ \\ | '_ ` _ \\  |______| \\ \\ / / | | | '_ \\   / _` |  / _ \\ \n" +
                " | |_) | |  __/ | | | | | |           \\ V /  | | | | | | | (_| | | (_) |\n" +
                " |____/   \\___| |_| |_| |_|            \\_/   |_| |_| |_|  \\__,_|  \\___/ "
        + ANSI_RESET) ;


        String linha = azul + "=".repeat(50) + ANSI_RESET;
        System.out.println("\n\n" + linha );
        System.out.println(azul + "Informações sobre o usuário: " + ANSI_RESET );
        System.out.println( azul +  "Nome: " + ANSI_RESET + usuarioVerificado.getNome());
        System.out.println(azul + "Permissão: " + ANSI_RESET + usuarioVerificado.getTipo_usuario());
        System.out.println(linha);

        Integer idEmpresaUsuario = usuarioVerificado.getEmpresa_id();

        System.out.println("\n\nVerificando se a máquina já está cadastrada...");

        Integer idEmpresa = usuarioVerificado.getEmpresa_id();
        if (!loginService.hasMaquina()) {
            System.out.println(amarelo + "Máquina não cadastrada, cadastrando..." + ANSI_RESET);
            maquinaService.executarCadastro(idEmpresa);
        }

        System.out.println(amarelo + "Identificando máquina..." + ANSI_RESET);
//             Identificar máquina e começar a monitorar o processo
        Integer idAtm = maquinaService.identificarMaquina();

        System.out.println("\n" + roxo + "Iniciando monitoramento..." + ANSI_RESET);
        killProcessosService.monitorar(idAtm, idEmpresa);
        monitorarService.monitorarHardware(idAtm, idEmpresaUsuario);
    }
}
