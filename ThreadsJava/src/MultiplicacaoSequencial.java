// MultiplicacaoSequencial.java
public class MultiplicacaoSequencial {

    private final int[] A;
    private final int[] B;
    private final int n;
    private long[] C;
    private double tempoSegundos;

    public MultiplicacaoSequencial(int[] A, int[] B, int n) {
        this.A = A;
        this.B = B;
        this.n = n;
    }

    public void executar() {
        C = new long[n * n];

        long inicio = System.nanoTime();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                long soma = 0;
                for (int k = 0; k < n; k++) {
                    soma += (long) A[i * n + k] * B[k * n + j];
                }
                C[i * n + j] = soma;
            }
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