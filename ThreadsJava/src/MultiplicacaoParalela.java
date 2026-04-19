import java.util.concurrent.*;

public class MultiplicacaoParalela {

    private final int[] A;
    private final int[] B;
    private final int n;
    private final int numThreads;
    private long[] C;
    private double tempoSegundos;

    public MultiplicacaoParalela(int[] A, int[] B, int n, int numThreads) {
        this.A = A;
        this.B = B;
        this.n = n;
        this.numThreads = numThreads;
    }

    public void executar() throws Exception {
        C = new long[n * n];

        CyclicBarrier barrier = new CyclicBarrier(numThreads, () ->
                System.out.println("Todas as threads concluíram suas fatias.")
        );

        Thread[] threads = new Thread[numThreads];
        int linhasPorThread = n / numThreads;

        long inicio = System.nanoTime();

        for (int t = 0; t < numThreads; t++) {
            int linhaInicio = t * linhasPorThread;
            int linhaFim = (t == numThreads - 1) ? n : linhaInicio + linhasPorThread;

            threads[t] = new Thread(() -> {
                for (int i = linhaInicio; i < linhaFim; i++) {
                    for (int j = 0; j < n; j++) {
                        long soma = 0;
                        for (int k = 0; k < n; k++) {
                            soma += (long) A[i * n + k] * B[k * n + j];
                        }
                        C[i * n + j] = soma;
                    }
                }

                try {
                    barrier.await();
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            });

            threads[t].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        long fim = System.nanoTime();
        tempoSegundos = (fim - inicio) / 1_000_000_000.0;
    }

    public double getTempoSegundos() {
        return tempoSegundos;
    }

    public long[] getResultado() {
        return C;
    }
}