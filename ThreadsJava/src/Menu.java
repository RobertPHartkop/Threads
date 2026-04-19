import java.util.Scanner;

public class Menu {

    private final Scanner scanner = new Scanner(System.in);

    // Configuração dos paths para os arquivos de métricas
    private final MetricsWriter writerManual = new MetricsWriter("output/java/resultados_manual.csv");
    private final MetricsWriter writerAut = new MetricsWriter("output/java/resultados.csv");

    public void executar () {
        while (true) {
            System.out.println("\n=== Menu Interativo: Multiplicacao de Matrizes (Java) ===");
            System.out.println("1. Multiplicacao Sequencial");
            System.out.println("2. Multiplicacao Paralela");
            System.out.println("3. Bateria de Testes Automatizados");
            System.out.println("0. Sair");
            System.out.print("Escolha: ");

            int opcao = -1;
            try {
                opcao = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("  [ERRO] Entrada invalida.");
                continue;
            }

            switch (opcao) {
                case 1 -> executarSequencial();
                case 2 -> executarParalela();
                case 3 -> executarAutomatizado();
                case 0 -> {
                    System.out.println("Saindo...");
                    System.out.println("Os dados da execucao manual estao em output/java/resultados_manual.csv");
                    return;
                }
                default -> System.out.println("  [ERRO] Opcao invalida.");
            }
        }
    }

    private void executarSequencial () {
        System.out.print("Digite a dimensao da matriz (ex: 1000, 5000, 10000): ");
        int n = Integer.parseInt(scanner.nextLine().trim());

        String caminhoA = "input/Matriz(" + n + ")1.bin";
        String caminhoB = "input/Matriz(" + n + ")2.bin";

        try {
            System.out.println("-> Buscando matrizes na pasta input");
            MatrizReader readerA = new MatrizReader(caminhoA);
            MatrizReader readerB = new MatrizReader(caminhoB);

            System.out.printf("  Iniciando multiplicacao sequencial %dx%d...%n", n, n);
            MultiplicacaoSequencial seq = new MultiplicacaoSequencial(readerA.getDados(), readerB.getDados(), n);
            seq.executar();

            double tempo = seq.getTempoSegundos();
            System.out.printf("  [Seq] Concluido em %.6f segundos.%n", tempo);

            writerManual.salvar(n, 1, tempo);
            String outPath = "output/java/resultado_" + n + "_manual.bin";
            MatrizWriter.salvar(outPath, n, seq.getResultado());

        } catch (Exception e) {
            System.out.println("  [ERRO] Falha ao carregar matrizes de " + n + "x" + n);
            System.out.println("  Verifique se os arquivos " + caminhoA + " e " + caminhoB + " existem!");
        }
    }

    private void executarParalela () {
        System.out.print("Digite a dimensao da matriz (ex: 1000, 5000, 10000): ");
        int n = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Numero de threads: ");
        int numThreads = Integer.parseInt(scanner.nextLine().trim());

        String caminhoA = "input/Matriz(" + n + ")1.bin";
        String caminhoB = "input/Matriz(" + n + ")2.bin";

        try {
            System.out.println("-> Buscando matrizes na pasta input");
            MatrizReader readerA = new MatrizReader(caminhoA);
            MatrizReader readerB = new MatrizReader(caminhoB);

            System.out.printf("  Iniciando multiplicacao paralela %dx%d com %d threads...%n", n, n, numThreads);
            MultiplicacaoParalela par = new MultiplicacaoParalela(readerA.getDados(), readerB.getDados(), n, numThreads);
            par.executar();

            double tempo = par.getTempoSegundos();
            System.out.printf("  [Par - %02d threads] Concluido em %.6f segundos.%n", numThreads, tempo);

            writerManual.salvar(n, numThreads, tempo);
            String outPath = "output/java/resultado_" + n + "_manual.bin";
            MatrizWriter.salvar(outPath, n, par.getResultado());

        } catch (Exception e) {
            System.out.println("  [ERRO] Falha ao carregar matrizes de " + n + "x" + n);
            System.out.println("  Verifique se os arquivos " + caminhoA + " e " + caminhoB + " existem!");
        }
    }

    private void executarAutomatizado() {
        int[] tamanhos = {1000, 5000, 10000};
        int[] threads = {1, 2, 3, 4, 5, 6, 8, 10};
        int repeticoes = 3;

        System.out.println("=== Iniciando Bateria Completa de Testes Automatizados ===\n");

        for (int n : tamanhos) {
            String caminhoA = "input/Matriz(" + n + ")1.bin";
            String caminhoB = "input/Matriz(" + n + ")2.bin";

            System.out.println("-> Carregando matrizes " + n + "x" + n);
            try {
                MatrizReader readerA = new MatrizReader(caminhoA);
                MatrizReader readerB = new MatrizReader(caminhoB);

                for (int numThreads : threads) {
                    for (int r = 1; r <= repeticoes; r++) {
                        System.out.printf("  Testando: Matriz %dx%d | Threads: %02d | Repeticao: %d/%d...%n", n, n, numThreads, r, repeticoes);

                        if (numThreads == 1) {
                            MultiplicacaoSequencial seq = new MultiplicacaoSequencial(readerA.getDados(), readerB.getDados(), n);
                            seq.executar();
                            double tempo = seq.getTempoSegundos();
                            System.out.printf("    [Seq] Concluido em %.6f segundos.%n", tempo);

                            writerAut.salvar(n, 1, tempo);
                            MatrizWriter.salvar("output/java/resultado_" + n + ".bin", n, seq.getResultado());
                        } else {
                            MultiplicacaoParalela par = new MultiplicacaoParalela(readerA.getDados(), readerB.getDados(), n, numThreads);
                            par.executar();
                            double tempo = par.getTempoSegundos();
                            System.out.printf("    [Par - %02d threads] Concluido em %.6f segundos.%n", numThreads, tempo);

                            writerAut.salvar(n, numThreads, tempo);
                            MatrizWriter.salvar("output/java/resultado_" + n + ".bin", n, par.getResultado());
                        }
                    }
                }
                System.out.printf("%nConcluido testes para dimensao %d.%n", n);
                System.out.println("--------------------------------------------------\n");

            } catch (Exception e) {
                System.out.println("  [ERRO] Falha ao carregar " + caminhoA + " ou " + caminhoB + ".\n");
            }
        }

        System.out.println("=== Bateria de testes concluida! ===");
        System.out.println("Os dados estao em output/java/resultados.csv");
    }
}