import java.io.*;
import java.util.Locale;

public class MetricsWriter {
    private final String caminho;

    public MetricsWriter(String caminho) {
        this.caminho = caminho;
    }

    public void salvar(int tamanho, int threads, double tempoSegundos) throws Exception {
        File arquivo = new File(caminho);

        // GARANTIA: Cria as pastas pai se não existirem
        File pasta = arquivo.getParentFile();
        if (pasta != null && !pasta.exists()) {
            pasta.mkdirs();
        }

        boolean existe = arquivo.exists();

        try (FileWriter fw = new FileWriter(arquivo, true);
             BufferedWriter bw = new BufferedWriter(fw)) {

            if (!existe) {
                bw.write("tamanho_matriz,threads,tempo_segundos");
                bw.newLine();
            }

            // Usando Locale.US para forçar o ponto no lugar da vírgula decimal
            bw.write(tamanho + "," + threads + "," + String.format(Locale.US, "%.6f", tempoSegundos));
            bw.newLine();
        }
    }
}