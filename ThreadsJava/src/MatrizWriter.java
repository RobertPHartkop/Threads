import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class MatrizWriter {

    public static void salvar(String caminho, int n, long[] dados) {
        try (FileOutputStream fos = new FileOutputStream(caminho);
             FileChannel channel = fos.getChannel()) {

            // Escreve o cabeçalho (n, n) em Little Endian (8 bytes totais)
            ByteBuffer header = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            header.putInt(n);
            header.putInt(n);
            header.flip();
            channel.write(header);

            // Usa um buffer para não estourar a memória com matrizes grandes (10000x10000)
            int bufferSize = 8192;
            ByteBuffer dataBuffer = ByteBuffer.allocate(bufferSize * 8).order(ByteOrder.LITTLE_ENDIAN);

            for (long val : dados) {
                if (!dataBuffer.hasRemaining()) {
                    dataBuffer.flip();
                    channel.write(dataBuffer);
                    dataBuffer.clear();
                }
                dataBuffer.putLong(val);
            }
            // Escreve o que sobrou no buffer
            if (dataBuffer.position() > 0) {
                dataBuffer.flip();
                channel.write(dataBuffer);
            }

            // Apenas suprime a mensagem se for o resultado padronizado do aut, 
            // mas avisa caso contrário
            if(caminho.contains("manual")) {
                System.out.println("  Matriz resultante exportada para: " + caminho);
            }
        } catch (Exception e) {
            System.out.println("  [ERRO] Nao foi possivel exportar para " + caminho);
        }
    }
}