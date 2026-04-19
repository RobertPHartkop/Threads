// MetricsWriter.java

import java.io.*;

public class MetricsWriter {

    private final String caminho;

    public MetricsWriter(String caminho) {
        this.caminho = caminho;
    }

    public void salvar(int tamanho, int threads, double tempoSegundos) throws Exception {
        File arquivo = new File(caminho);
        boolean existe = arquivo.exists();

        try (FileWriter fw = new FileWriter(arquivo, true);
             BufferedWriter bw = new BufferedWriter(fw)) {

            if (!existe) {
                bw.write("tamanho_matriz,threads,tempo_segundos");
                bw.newLine();
            }

            bw.write(tamanho + "," + threads + "," + String.format("%.6f", tempoSegundos));
            bw.newLine();
        }
    }
}