import java.util.Scanner;

public class Menu {

    private final Scanner scanner = new Scanner(System.in);
    private final MetricsWriter metricsWriter = new MetricsWriter("resultados.csv");

    public void executar () {
        while (true) {
            System.out.println("\n=== Menu Principal ===");
            System.out.println("1. Multiplicação Sequencial");
            System.out.println("2. Multiplicação Paralela");
            System.out.println("0. Sair");
            System.out.print("Escolha: ");

            int opcao = scanner.nextInt();
            scanner.nextLine();

            switch (opcao) {
                case 1 -> executarSequencial();
                case 2 -> executarParalela();
                case 0 -> {
                    System.out.println("Encerrando...");
                    return;
                }
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private String lerArquivo (String label){
        System.out.print("Caminho da matriz " + label + ": ");
        return scanner.nextLine().trim();
    }

    private void executarSequencial () {
        try {
            String caminhoA = lerArquivo("A");
            String caminhoB = lerArquivo("B");

            System.out.println("Lendo matrizes...");
            MatrizReader readerA = new MatrizReader(caminhoA);
            MatrizReader readerB = new MatrizReader(caminhoB);

            int n = readerA.getN();

            System.out.printf("Iniciando multiplicação sequencial %d×%d...%n", n, n);
            MultiplicacaoSequencial seq = new MultiplicacaoSequencial(
                    readerA.getDados(), readerB.getDados(), n
            );
            seq.executar();

            double tempo = seq.getTempoSegundos();
            System.out.printf("Concluído em %.6f segundos.%n", tempo);

            metricsWriter.salvar(n, 1, tempo);
            System.out.println("Métricas salvas em resultados.csv");

        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void executarParalela () {
        try {
            String caminhoA = lerArquivo("A");
            String caminhoB = lerArquivo("B");

            System.out.print("Número de threads: ");
            int numThreads = scanner.nextInt();
            scanner.nextLine();

            System.out.println("Lendo matrizes...");
            MatrizReader readerA = new MatrizReader(caminhoA);
            MatrizReader readerB = new MatrizReader(caminhoB);

            int n = readerA.getN();

            System.out.printf("Iniciando multiplicação paralela %d×%d com %d threads...%n", n, n, numThreads);
            MultiplicacaoParalela par = new MultiplicacaoParalela(
                    readerA.getDados(), readerB.getDados(), n, numThreads
            );
            par.executar();

            double tempo = par.getTempoSegundos();
            System.out.printf("Concluído em %.6f segundos.%n", tempo);

            metricsWriter.salvar(n, numThreads, tempo);
            System.out.println("Métricas salvas em resultados.csv");

        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
}